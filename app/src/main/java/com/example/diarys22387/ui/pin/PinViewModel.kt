package com.example.diarys22387.ui.pin

import androidx.lifecycle.ViewModel
import com.example.diarys22387.data.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {
    
    private val _pinState = MutableStateFlow<String>("")
    val pinState: StateFlow<String> = _pinState.asStateFlow()
    
    fun validatePin(pin: String): Boolean {
        val savedPin = securePreferences.getPin()
        return if (savedPin == null) {
            // Если PIN еще не установлен, сохраняем первый введенный PIN
            securePreferences.savePin(pin)
            true
        } else {
            pin == savedPin
        }
    }
    
    fun updatePin(pin: String) {
        _pinState.value = pin
    }
} 