package com.example.diarys22387.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.diarys22387.data.model.Note
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val notes by viewModel.notes.collectAsState()

    val cameraPosState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(54.71, 20.51), 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPosState
    ) {
        notes.forEach { note ->
            if (note.hasLocation()) {
                Marker(
                    state = MarkerState(position = LatLng(note.latitude!!, note.longitude!!)),
                    title = note.title,
                    snippet = note.content
                )
            }
        }
    }
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    val notes = noteRepository.getAllNotes()
        .catch { e ->
            e.printStackTrace()
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
