package org.nativescript.samplejs

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GPSViewModel : ViewModel() {
    val gpsSignal = mutableStateOf("Weak")
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    fun updateGPSSignal(signal: String) {
        gpsSignal.value = signal
    }

    fun setOfflineMode(offline: Boolean) {
        viewModelScope.launch {
            _isOffline.emit(offline)
        }
    }
}