/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alan.scanbarcode.scan

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.GuardedBy
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sanga.scan_plugin.vision.FrameMetadata
import com.sanga.scan_plugin.vision.ScopedExecutor
import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(VisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase(
    val onSuccessUnit: ((results: List<Barcode>) -> Unit)? = null,
    val onFailureUnit: ((e: Exception) -> Unit)? = null
) : VisionImageProcessor {

    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    // Whether this processor is already shut down
    private var isShutdown = false

    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalFrameMs = 0L
    private var maxFrameMs = 0L
    private var minFrameMs = Long.MAX_VALUE
    private var totalDetectorMs = 0L
    private var maxDetectorMs = 0L
    private var minDetectorMs = Long.MAX_VALUE

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    private fun processLatestImage() {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage!!, processingMetaData!!)
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
    ) {
        requestDetectInImage(
            InputImage.fromByteBuffer(
                data,
                frameMetadata.width,
                frameMetadata.height,
                frameMetadata.rotation,
                InputImage.IMAGE_FORMAT_NV21
            ),
        )
            .addOnSuccessListener(executor) { processLatestImage() }
    }


    override fun processImageProxy(image: ImageProxy) {
        if (isShutdown) {
            return
        }

        requestDetectInImage(
            InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
        )
            .addOnCompleteListener { image.close() }
    }

    fun processImageProxy(context: Context, image: ImageProxy) {
        Log.e("CropUtil", "开始裁剪:${System.currentTimeMillis()}")
        val bitmap = CropUtil.cropImageProxyCenter(context,image, 200)
        Log.e("CropUtil", "结束裁剪:${System.currentTimeMillis()}")
        requestDetectInImage(
//            InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
            InputImage.fromBitmap(bitmap, 0),
        )
            .addOnCompleteListener {
                bitmap.recycle()
                image.close()
            }
    }


    // Code for processing single still image
    override fun processBitmap(bitmap: Bitmap?) {
        if (isShutdown) {
            return
        }
        requestDetectInImage(
            InputImage.fromBitmap(bitmap!!, 0),
        ).addOnCompleteListener { bitmap.recycle() }
    }

    private fun requestDetectInImage(
        image: InputImage,
    ): Task<List<Barcode>> {
        return setUpListener(
            detectInImage(image),
        )
    }

    private fun setUpListener(
        task: Task<List<Barcode>>,
    ): Task<List<Barcode>> {

        return task
            .addOnSuccessListener(
                executor,
                OnSuccessListener { results: List<Barcode> ->
                    onSuccessUnit?.invoke(results)
                }
            )
            .addOnFailureListener(
                executor,
                OnFailureListener { e: Exception ->
                    e.printStackTrace()
                    onFailureUnit?.invoke(e)
                }
            )
    }

    override fun stop() {
        executor.shutdown()
        isShutdown = true
        resetLatencyStats()
    }

    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.MAX_VALUE
    }

    protected abstract fun detectInImage(image: InputImage): Task<List<Barcode>>


}
