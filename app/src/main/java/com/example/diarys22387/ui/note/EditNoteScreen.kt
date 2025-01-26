package com.example.diarys22387.ui.note

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.ui.components.WaveformVisualizer
import com.google.accompanist.permissions.*
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
import androidx.compose.material.icons.filled.MicNone

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    navController: NavHostController,
    viewModel: EditNoteViewModel = hiltViewModel()
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
private fun AudioSection(
    note: Note,
    isRecording: Boolean,
    isPlaying: Boolean,
    onRecordClick: () -> Unit,
    viewModel: EditNoteViewModel,
    recordPermissionState: PermissionState
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Audio Note",
                    style = MaterialTheme.typography.titleMedium
                )
                
                val recordingTime by viewModel.recordingTime.collectAsStateWithLifecycle()
                if (isRecording) {
                    Text(
                        text = viewModel.formatTime(recordingTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
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
            
            Spacer(Modifier.height(8.dp))
            
            val amplitudes by viewModel.recordingAmplitudes.collectAsStateWithLifecycle(initialValue = emptyList())
            
            AnimatedVisibility(
                visible = isRecording || amplitudes.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                WaveformVisualizer(
                    amplitudes = amplitudes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRecording) 1.2f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                IconButton(
                    onClick = {
                        if (recordPermissionState.status.isGranted) {
                            onRecordClick()
                        } else {
                            recordPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (isRecording) scale else 1f
                        scaleY = if (isRecording) scale else 1f
                    }
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
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
    
    val recordPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val address = note.address ?: "No address"
    val painter = rememberAsyncImagePainter(note.imageUrl)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(it) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onTitleChanged(it)
            },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = {
                content = it
                onContentChanged(it)
            },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            maxLines = Int.MAX_VALUE
        )

        Spacer(Modifier.height(8.dp))

        AudioSection(
            note = note,
            isRecording = isRecording,
            isPlaying = isPlaying,
            onRecordClick = {
                if (isRecording) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording()
                }
            },
            viewModel = viewModel,
            recordPermissionState = recordPermissionState
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
            IconButton(
                onClick = onUpdateLocation
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Update location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onDrawClicked
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Draw",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { photoPickerLauncher.launch("image/*") }
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onSaveClicked
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save note",
                    tint = MaterialTheme.colorScheme.primary
                )
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
