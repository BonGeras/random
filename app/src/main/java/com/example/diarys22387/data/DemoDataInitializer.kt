package com.example.diarys22387.data

import com.example.diarys22387.data.model.Note
import com.example.diarys22387.data.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataInitializer @Inject constructor(
    private val noteRepository: NoteRepository
) {
    private var loaded = false

    suspend fun loadDemoDataIfNeeded() {
        if (!loaded) {
            loadDemo()
            loaded = true
        }
    }

    private suspend fun loadDemo() {
        val demoNotes = listOf(
            Note(title = "Demo Note 1", content = "Hello from note #1", latitude=54.70, longitude=20.50),
            Note(title = "Demo Note 2", content = "Sample with no location"),
            Note(title = "Demo Note 3", content = "Has location as well", latitude=54.69, longitude=20.46)
        )
        demoNotes.forEach { noteRepository.addNote(it) }
    }
}
