package com.example.diarys22387.ui.note

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.data.repository.NoteRepository
import com.example.diarys22387.service.GeofenceManager
import com.example.diarys22387.util.AudioPlayer
import com.example.diarys22387.util.AudioRecorder
import com.example.diarys22387.util.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val mediaRepository: MediaRepository,
    private val geofenceManager: GeofenceManager,
    private val locationManager: LocationManager,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = mutableStateOf<EditNoteUiState>(EditNoteUiState.Initial)
    val uiState: State<EditNoteUiState> = _uiState

    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    val audioProgress = audioPlayer.progress
    val audioDuration = audioPlayer.duration
    val audioPosition = audioPlayer.currentPosition

    private var currentNote: Note? = null
    private var currentAudioUri: Uri? = null

    val recordingAmplitudes = audioRecorder.amplitudes

    fun formatTime(millis: Int): String = audioPlayer.formatTime(millis)

    fun seekTo(position: Float) {
        audioPlayer.seekTo(position)
    }

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = EditNoteUiState.Loading
            val note = noteRepository.getNote(noteId)
            if (note == null) {
                _uiState.value = EditNoteUiState.Error("Note not found")
            } else {
                currentNote = note
                _uiState.value = EditNoteUiState.Success(note)
            }
        }
    }

    fun toggleAudioPlayback() {
        val audioUrl = currentNote?.audioUrl ?: return
        
        if (_isPlaying.value) {
            audioPlayer.stopPlaying()
            _isPlaying.value = false
        } else {
            audioPlayer.startPlaying(audioUrl) {
                _isPlaying.value = false
            }
            _isPlaying.value = true
        }
    }

    fun startRecording() {
        if (!_isRecording.value) {
            try {
                currentAudioUri = audioRecorder.startRecording()
                _isRecording.value = true
            } catch (e: SecurityException) {
                _uiState.value = EditNoteUiState.Error("Recording permission not granted")
            } catch (e: Exception) {
                _uiState.value = EditNoteUiState.Error("Failed to start recording: ${e.message}")
            }
        }
    }

    fun stopRecording() {
        if (_isRecording.value) {
            audioRecorder.stopRecording()
            _isRecording.value = false
            
            // Загружаем аудио в Firebase Storage
            currentAudioUri?.let { uri ->
                viewModelScope.launch {
                    try {
                        val audioUrl = mediaRepository.uploadAudio(uri)
                        currentNote = currentNote?.copy(audioUrl = audioUrl)
                        refreshUi()
                    } catch (e: Exception) {
                        _uiState.value = EditNoteUiState.Error("Failed to upload audio: ${e.message}")
                    }
                }
            }
        }
    }

    fun cancelRecording() {
        if (_isRecording.value) {
            audioRecorder.stopRecording()
            audioRecorder.deleteRecording()
            _isRecording.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelRecording()
        audioPlayer.stopPlaying()
    }

    fun updateTitle(newTitle: String) {
        currentNote = currentNote?.copy(title = newTitle)
        refreshUi()
    }

    fun updateContent(newContent: String) {
        currentNote = currentNote?.copy(content = newContent)
        refreshUi()
    }

    fun updateLocation() {
        viewModelScope.launch {
            if (!locationManager.hasLocationPermission()) {
                _uiState.value = EditNoteUiState.Error("No location permission!")
                return@launch
            }
            val note = currentNote ?: return@launch
            val loc = locationManager.getLastKnownLocation()
            if (loc == null) {
                _uiState.value = EditNoteUiState.Error("Could not get location")
            } else {
                val address = locationManager.getAddressFromCoords(loc.latitude, loc.longitude)
                currentNote = note.copy(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    address = address
                )
                refreshUi()
            }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            val note = currentNote ?: return@launch
            try {
                noteRepository.updateNote(note)
                if (note.hasLocation()) {
                    geofenceManager.addGeofenceForNote(note)
                } else {
                    geofenceManager.removeGeofence(note.id)
                }
                _uiState.value = EditNoteUiState.Success(note)
            } catch (e: Exception) {
                _uiState.value = EditNoteUiState.Error("Failed to save: ${e.message}")
            }
        }
    }

    private fun refreshUi() {
        currentNote?.let {
            _uiState.value = EditNoteUiState.Success(it)
        }
    }

    fun hasPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }
} 