package com.example.diarys22387.ui.note

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.diarys22387.data.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.data.repository.NoteRepository
import com.example.diarys22387.service.GeofenceManager
import com.example.diarys22387.util.LocationManager
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

@Composable
fun EditNoteScreen(
    noteId: String,
    navController: NavHostController,
    viewModel: EditNoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    when (val state = uiState) {
        is EditNoteUiState.Initial -> {
        }
        is EditNoteUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EditNoteUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is EditNoteUiState.Success -> {
            val note = state.note
            EditNoteContent(
                note = note,
                onTitleChanged = { newTitle -> viewModel.updateTitle(newTitle) },
                onContentChanged = { newContent -> viewModel.updateContent(newContent) },
                onUpdateLocation = { viewModel.updateLocation() },
                onDrawClicked = {
                    val imageUrl = note.imageUrl.orEmpty()
                    navController.navigate("drawing/$imageUrl")
                },
                onSaveClicked = {
                    viewModel.saveNote()
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun EditNoteContent(
    note: Note,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onUpdateLocation: () -> Unit,
    onDrawClicked: () -> Unit,
    onSaveClicked: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    val address = note.address ?: "No address"
    val painter = rememberAsyncImagePainter(note.imageUrl)

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onTitleChanged(it)
            },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = {
                content = it
                onContentChanged(it)
            },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        if (!note.imageUrl.isNullOrEmpty()) {
            Image(
                painter = painter,
                contentDescription = "Note Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { /* open fullscreen if needed */ },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(8.dp))

        Text("Location: $address", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onUpdateLocation) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location")
                Spacer(Modifier.width(4.dp))
                Text("Update location")
            }

            Button(onClick = onDrawClicked) {
                Icon(Icons.Default.Brush, contentDescription = "Draw")
                Spacer(Modifier.width(4.dp))
                Text("Draw on Image")
            }

            Button(onClick = onSaveClicked) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }
        }
    }
}

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val mediaRepository: MediaRepository,
    private val geofenceManager: GeofenceManager,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = mutableStateOf<EditNoteUiState>(EditNoteUiState.Initial)
    val uiState: State<EditNoteUiState> = _uiState

    private var currentNote: Note? = null

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
}

sealed class EditNoteUiState {
    object Initial : EditNoteUiState()
    object Loading : EditNoteUiState()
    data class Success(val note: Note) : EditNoteUiState()
    data class Error(val message: String) : EditNoteUiState()
}
