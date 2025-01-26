package com.example.diarys22387.ui.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.diarys22387.data.model.Note

@Composable
fun NotePreviewScreen(
    noteId: String,
    onEditClick: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val note by viewModel.getNote(noteId).collectAsStateWithLifecycle(initialValue = null)

    note?.let { currentNote ->
        NotePreviewContent(
            note = currentNote,
            onEditClick = onEditClick
        )
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotePreviewContent(
    note: Note,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!note.imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(note.imageUrl),
                contentDescription = "Note Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = note.content,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!note.address.isNullOrEmpty()) {
            Text(
                text = "Location: ${note.address}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FloatingActionButton(
            onClick = onEditClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Note")
        }
    }
} 