package com.example.diarys22387.ui.pin

import androidx.lifecycle.ViewModel
import com.example.diarys22387.data.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {
    
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
} 