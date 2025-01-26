package com.example.diarys22387.ui.note

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.NoteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.util.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AddNoteScreen(
    onNoteSaved: () -> Unit,
    viewModel: AddNoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AddNoteUiState.Success) {
            onNoteSaved()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.setLocationIfPossible { newLat, newLon, place ->
                    lat = newLat
                    lon = newLon
                    address = place
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set Current Location")
        }

        address?.let {
            Text(
                "Location: $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        when (uiState) {
            is AddNoteUiState.Error -> {
                Text(
                    (uiState as AddNoteUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is AddNoteUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> { /* no-op */ }
        }

        Button(
            onClick = {
                viewModel.addNote(title, content, lat, lon, address)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && uiState !is AddNoteUiState.Loading
        ) {
            Text("Save Note")
        }
    }
}

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddNoteUiState>(AddNoteUiState.Initial)
    val uiState: StateFlow<AddNoteUiState> = _uiState

    fun addNote(
        title: String,
        content: String,
        lat: Double?,
        lon: Double?,
        address: String?
    ) {
        viewModelScope.launch {
            _uiState.value = AddNoteUiState.Loading
            try {
                val newNote = Note(
                    title = title,
                    content = content,
                    latitude = lat,
                    longitude = lon,
                    address = address
                )
                val result = noteRepository.addNote(newNote)
                result.fold(
                    onSuccess = { 
                        _uiState.value = AddNoteUiState.Success 
                    },
                    onFailure = { e ->
                        _uiState.value = AddNoteUiState.Error(e.message ?: "Failed to add note")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AddNoteUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setLocationIfPossible(onResult: (Double?, Double?, String?) -> Unit) {
        viewModelScope.launch {
            if (!locationManager.hasLocationPermission()) {
                onResult(null, null, "No location permission!")
                return@launch
            }
            val loc = locationManager.getLastKnownLocation()
            if (loc != null) {
                val place = locationManager.getAddressFromCoords(loc.latitude, loc.longitude)
                onResult(loc.latitude, loc.longitude, place)
            } else {
                onResult(null, null, "Could not get location")
            }
        }
    }
}

sealed class AddNoteUiState {
    object Initial : AddNoteUiState()
    object Loading : AddNoteUiState()
    object Success : AddNoteUiState()
    data class Error(val message: String) : AddNoteUiState()
}

