package com.example.diarys22387.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarys22387.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

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