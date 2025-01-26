package com.example.diarys22387.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState

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

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = NotesUiState.Loading
            try {
                val result = noteRepository.deleteNote(noteId)
                result.fold(
                    onSuccess = { 
                        _uiState.value = NotesUiState.Success 
                    },
                    onFailure = { e ->
                        _uiState.value = NotesUiState.Error(e.message ?: "Failed to delete note")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = NotesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getNote(noteId: String): Flow<Note?> {
        return flow {
            emit(noteRepository.getNote(noteId))
        }.catch { e ->
            e.printStackTrace()
            emit(null)
        }.flowOn(Dispatchers.IO)
    }
}

sealed class NotesUiState {
    object Loading : NotesUiState()
    object Success : NotesUiState()
    data class Error(val message: String) : NotesUiState()
}
