package com.alan.scan_example

import android.Manifest.permission.CAMERA
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.alan.scan_example.ui.theme.ScanBarcodeTheme
import com.alan.scanbarcode.scan.contract.ScanContract
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanBarcodeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Example()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Example() {
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // 权限请求对象
    val permissionState = rememberPermissionState(permission = CAMERA) {
        if (it) {
            showCamera = true
        }
    }
    LaunchedEffect(null) {
        if (permissionState.status.isGranted) {
            showCamera = true
        } else {
            permissionState.launchPermissionRequest()
        }
    }
    Scaffold { innerPadding ->
        val launcher = rememberLauncherForActivityResult(ScanContract()) { launchResult ->
            Toast.makeText(context, launchResult ?: "没有值", Toast.LENGTH_LONG).show()
        }
        Column(Modifier.padding(innerPadding)) {
            TextButton(onClick = {
                launcher.launch(null)
            }) {
                Text("调用扫码页面")
            }
        }

    }
}
