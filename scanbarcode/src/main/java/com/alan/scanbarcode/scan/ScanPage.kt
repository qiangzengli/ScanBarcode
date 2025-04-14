package com.alan.scanbarcode.scan

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.alan.scanbarcode.ScanActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ScanPage() {
    val context = LocalContext.current
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 检查设备是否支持震动
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }

    // 简单震动
    fun vibrate(durationMillis: Long) {
        if (!hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    var offset by remember { mutableStateOf(0f) }
    // 无限重复动画
    val infiniteTransition = rememberInfiniteTransition()
    offset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        )
    ).value
    var currentTime by remember { mutableStateOf(0L) }
    var lastTime by remember { mutableStateOf(0L) }
    // 是否扫描成功
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val processor by remember {
        mutableStateOf(
            BarcodeScannerProcessor(
                onSuccess = {
                    if (isSuccess) return@BarcodeScannerProcessor
                    if (it.isNotEmpty()) {
                        isSuccess = true
                        vibrate(200L)
                        (context as ScanActivity).apply {
                            setResult(RESULT_OK, Intent().apply {
                                putExtra("barcode", it.firstOrNull()?.rawValue)
                            })
                            finish()
                        }
                    }

                },
                onFailed = {

                }

            ))
    }
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CameraView(
                onAnalyze = {
                    scope.launch(Dispatchers.IO) {
                        Log.d("ScanPage", "onAnalyze")
                        currentTime = System.currentTimeMillis()
                        if (currentTime - lastTime >= 200L) {
                            processor.processImageProxy(context, it)
                            lastTime = currentTime
                        } else {
                            it.close()
                        }

                    }

                },
            )

            // 绘制镂空框
            Canvas(modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)) {
                val frameWidthPx = 200.dp.toPx()

                // 绘制外部全屏背景
                drawRect(color = Color.Black.copy(0.4f))

                // 在中心绘制一个透明矩形实现镂空效果
                drawRect(
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                    topLeft = Offset((size.width - frameWidthPx) / 2, (size.height - frameWidthPx) / 2),
                    size = Size(
                        frameWidthPx,
                        frameWidthPx
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .zIndex(2f)
                    .border(1.dp, color = Color.White),
                contentAlignment = Alignment.Center,
            ) {
// 扫描线
                Divider(
                    color = Color.Blue,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 200.dp * offset - 100.dp)
                        .shadow(4.dp, shape = RectangleShape)
                )
            }

            Image(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "null",
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(start = 15.dp, top = 35.dp)
                    .clickable {
                        (context as ScanActivity).finish()
                    }
                    .zIndex(2f),
            )


        }
    }

}




