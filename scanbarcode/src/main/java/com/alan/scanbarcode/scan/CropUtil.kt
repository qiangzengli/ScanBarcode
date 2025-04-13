package com.alan.scanbarcode.scan

import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageProxy
import com.alan.scanbarcode.scan.CropUtil.yuv420888ToNv21
import java.io.ByteArrayOutputStream

// DP转PX
fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun ImageProxy.toBitmap(): Bitmap {
    val image = this
    val nv21 = yuv420888ToNv21(image)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

object CropUtil {


    // 确保裁剪区域在图像范围内
    fun ensureCropRect(imageWidth: Int, imageHeight: Int, cropSize: Int): Rect {
        val centerX = imageWidth / 2
        val centerY = imageHeight / 2
        val halfSize = cropSize / 2

        return Rect(
            (centerX - halfSize).coerceAtLeast(0),
            (centerY - halfSize).coerceAtLeast(0),
            (centerX + halfSize).coerceAtMost(imageWidth),
            (centerY + halfSize).coerceAtMost(imageHeight)
        )
    }

    fun cropImageProxyCenter(context: Context,imageProxy: ImageProxy, sizeDp: Int): Bitmap {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        val bitmap = imageProxy.toBitmap()
        val newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val cropSizePx = context.dpToPx(sizeDp)
        val width = newBitmap.width
        val height = newBitmap.height
        val cropRect = ensureCropRect(width, height, cropSizePx)

        // 4. 执行裁剪
        return Bitmap.createBitmap(
            newBitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
        ).also {
            newBitmap.recycle()
            bitmap.recycle()
        }
    }


    fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        val chromaRowStride = image.planes[1].rowStride
        val chromaPixelStride = image.planes[1].pixelStride

        var offset = ySize
        for (row in 0 until image.height / 2) {
            for (col in 0 until image.width / 2) {
                val vuPos = row * chromaRowStride + col * chromaPixelStride
                nv21[offset++] = vBuffer.get(vuPos)
                nv21[offset++] = uBuffer.get(vuPos)
            }
        }

        return nv21
    }

}