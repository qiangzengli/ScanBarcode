package com.alan.scanbarcode.scan

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor():ViewModel() {
    val frameFlow : MutableSharedFlow<ImageProxy> = MutableSharedFlow<ImageProxy>()
}