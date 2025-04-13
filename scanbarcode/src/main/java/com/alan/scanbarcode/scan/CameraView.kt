package com.alan.scanbarcode.scan

import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat

/**
 * 画面预览，拍照
 */
@Composable
fun CameraView(onAnalyze: ((ImageProxy) -> Unit) = {}) {
    // 上下文
    val context = LocalContext.current
    // 声明周期
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    // 协程域
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    // 线程池
    val executor = remember { ContextCompat.getMainExecutor(context) }
    // 拍照对象
    val imageCapture = remember {
        ImageCapture.Builder()
            // 设置拍照的Size
            .setTargetResolution(Size(1080, 1920))
            //设置照片质量
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) //设置照片质量
            .build()
    }
    // 图片分析
    val imageAnalyzers =
        ImageAnalysis
            .Builder()
            .setTargetResolution(Size(1080, 1920))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, object : ImageAnalysis.Analyzer {
                    override fun analyze(p0: ImageProxy) {
                        onAnalyze(p0)
                    }
                })
            }
    // View 系统CameraX 预览组件
    val previewView = remember { PreviewView(context) }
    // 预览
    val preview = remember {
        Preview
            .Builder()
            .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
    }
    // 摄像头选择
    val cameraSelector = remember {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }
    val cameraProvider = remember { cameraProviderFuture.get() }
    // 使用executor 去执行上面的内容

    LaunchedEffect(true) {
        cameraProviderFuture.addListener({
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzers,
                imageCapture
            )

        }, executor)
    }

    Box(modifier = Modifier.zIndex(1f)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
        )
    }
}