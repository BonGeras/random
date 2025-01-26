package com.example.diarys22387.ui.note

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.NoteRepository
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.util.LocationManager
import com.example.diarys22387.util.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val mediaRepository: MediaRepository,
    private val locationManager: LocationManager,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddNoteUiState>(AddNoteUiState.Initial)
    val uiState: StateFlow<AddNoteUiState> = _uiState

    var isRecording by mutableStateOf(false)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    private var currentImageUri: Uri? = null
    private var currentAudioUri: Uri? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentAddress: String? = null

    fun prepareImageCapture(context: Context): Uri? {
        val imageFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            ).also { uri ->
                currentImageUri = uri
            }
        } catch (e: Exception) {
            _uiState.value = AddNoteUiState.Error("Failed to prepare camera: ${e.message}")
            null
        }
    }

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        try {
            currentAudioUri = mediaRepository.startRecording()
            isRecording = true
        } catch (e: Exception) {
            _uiState.value = AddNoteUiState.Error("Failed to start recording: ${e.message}")
        }
    }

    private fun stopRecording() {
        try {
            mediaRepository.stopRecording()
            isRecording = false
        } catch (e: Exception) {
            _uiState.value = AddNoteUiState.Error("Failed to stop recording: ${e.message}")
        }
    }

    fun toggleAudioPlayback() {
        if (isPlaying) {
            audioPlayer.stopPlaying()
            isPlaying = false
        } else {
            currentAudioUri?.let { uri ->
                audioPlayer.startPlaying(uri.toString()) {
                    isPlaying = false
                }
                isPlaying = true
            }
        }
    }

    fun setLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                if (!locationManager.hasLocationPermission()) {
                    _uiState.value = AddNoteUiState.Error("Location permission not granted")
                    return@launch
                }
                
                if (!locationManager.isLocationEnabled()) {
                    _uiState.value = AddNoteUiState.Error("Location services are disabled")
                    return@launch
                }

                currentLatitude = latitude
                currentLongitude = longitude
                currentAddress = locationManager.getAddressFromCoords(latitude, longitude)
            } catch (e: Exception) {
                _uiState.value = AddNoteUiState.Error("Failed to get address: ${e.message}")
            }
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            _uiState.value = AddNoteUiState.Loading
            try {
                // Upload media files if they exist
                val imageUrl = currentImageUri?.let { uri ->
                    mediaRepository.uploadImage(uri)
                }
                val audioUrl = currentAudioUri?.let { uri ->
                    mediaRepository.uploadAudio(uri)
                }

                // Create and save the note
                val note = Note(
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl,
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    address = currentAddress
                )
                
                noteRepository.addNote(note)
                _uiState.value = AddNoteUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddNoteUiState.Error(e.message ?: "Failed to save note")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRepository.release()
        audioPlayer.release()
    }
}

sealed class AddNoteUiState {
    object Initial : AddNoteUiState()
    object Loading : AddNoteUiState()
    object Success : AddNoteUiState()
    data class Error(val message: String) : AddNoteUiState()
} 