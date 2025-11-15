package com.alan.scanbarcode

import android.os.Bundle
import android.view.Window.FEATURE_NO_TITLE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.alan.scanbarcode.scan.ScanPage

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
           // 隐藏ActionBar
        requestWindowFeature(FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        setContent {
            ScanPage()
        }
    }
}
