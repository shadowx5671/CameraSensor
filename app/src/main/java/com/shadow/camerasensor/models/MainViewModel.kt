package com.shadow.camerasensor.models

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel: ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }
}