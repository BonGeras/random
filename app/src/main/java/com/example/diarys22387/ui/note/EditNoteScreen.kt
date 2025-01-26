package com.example.diarys22387.ui.note

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.ui.components.WaveformVisualizer
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.repository.MediaRepository
import com.example.diarys22387.data.repository.NoteRepository
import com.example.diarys22387.service.GeofenceManager
import com.example.diarys22387.util.LocationManager
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    navController: NavHostController,
    viewModel: EditNoteViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    when (val state = uiState) {
        is EditNoteUiState.Initial -> {
        }
        is EditNoteUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EditNoteUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                },
                viewModel = viewModel
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
    onSaveClicked: () -> Unit,
    viewModel: EditNoteViewModel
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    val isRecording by viewModel.isRecording
    val isPlaying by viewModel.isPlaying
    
    val context = LocalContext.current
    val recordPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

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

        // Аудио секция
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Audio Note",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(Modifier.height(8.dp))
                
                if (note.audioUrl != null) {
                    val progress by viewModel.audioProgress.collectAsStateWithLifecycle()
                    val duration by viewModel.audioDuration.collectAsStateWithLifecycle()
                    val position by viewModel.audioPosition.collectAsStateWithLifecycle()
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleAudioPlayback() }
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop" else "Play",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(Modifier.width(8.dp))
                            
                            Slider(
                                value = progress,
                                onValueChange = { viewModel.seekTo(it) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(Modifier.width(8.dp))
                            
                            Text(
                                "${viewModel.formatTime(position)} / ${viewModel.formatTime(duration)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (isRecording) {
                    val amplitudes by viewModel.recordingAmplitudes.collectAsStateWithLifecycle()
                    WaveformVisualizer(
                        amplitudes = amplitudes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        Text(
                            "Recording...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        IconButton(
                            onClick = { viewModel.cancelRecording() }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.stopRecording() }
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop recording",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        if (viewModel.hasPermission()) {
                            IconButton(
                                onClick = { viewModel.startRecording() }
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Start recording",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { recordPermissionState.launchPermissionRequest() }
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Request recording permission",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

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
                Icon(Icons.Default.Edit, contentDescription = "Draw")
                Spacer(Modifier.width(4.dp))
                Text("Draw")
            }

            Button(
                onClick = onSaveClicked,
                enabled = !isRecording
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }
        }
    }
}

sealed class EditNoteUiState {
    object Initial : EditNoteUiState()
    object Loading : EditNoteUiState()
    data class Success(val note: Note) : EditNoteUiState()
    data class Error(val message: String) : EditNoteUiState()
}
