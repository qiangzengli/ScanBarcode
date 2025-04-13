package com.alan.scanbarcode.scan

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alan.scanbarcode.ScanActivity
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


@Preview
@Composable
fun ScanPage(vm: ScanViewModel = viewModel()) {
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
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    val scope = rememberCoroutineScope()
    val processor by remember {
        mutableStateOf(
            BarcodeScannerProcessor(
                onSuccess = {
                    if (it.isNotEmpty()) {
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
    LaunchedEffect(null) {
        vm.frameFlow
            .debounce { 200L }
            .collect {
                processor.processImageProxy(context, it)
            }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) {


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CameraView(
                onAnalyze = {
                    scope.launch {
                        vm.frameFlow.emit(it)
                    }
                },
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .zIndex(1000f)
                    .border(1.dp, color = Color.Blue),
                contentAlignment = Alignment.Center,
            ) {
// 扫描线
                Divider(
                    color = Color.Green.copy(alpha = 0.7f),
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 200.dp * offset - 100.dp)
                        .shadow(4.dp, shape = RectangleShape)
                )
            }

        }
    }

}




