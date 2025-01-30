package com.example.diarys22387.ui.note

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddNoteScreen(
    onNoteSaved: () -> Unit,
    onOpenMap: () -> Unit,
    viewModel: AddNoteViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val recordPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AddNoteUiState.Success) {
            onNoteSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (recordPermissionState.status is PermissionStatus.Granted) {
                                viewModel.toggleRecording()
                            } else {
                                recordPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            if (viewModel.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (viewModel.isRecording) "Stop recording" else "Start recording",
                            tint = if (viewModel.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.toggleAudioPlayback() },
                        enabled = !viewModel.isRecording
                    ) {
                        Icon(
                            if (viewModel.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (viewModel.isPlaying) "Stop playback" else "Play recording",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                if (cameraPermissionState.status is PermissionStatus.Granted) {
                    viewModel.prepareImageCapture(context)?.let { uri ->
                        photoLauncher.launch(uri)
                    }
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Take photo")
            }
            
            IconButton(onClick = onOpenMap) {
                Icon(Icons.Default.LocationOn, contentDescription = "Add location")
            }
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
            else -> {}
        }

        Button(
            onClick = { viewModel.saveNote(title, content) },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && uiState !is AddNoteUiState.Loading
        ) {
            Text("Save Note")
        }
    }
}

