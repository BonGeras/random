package com.example.diarys22387.data.repository

import com.example.diarys22387.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notesCollection = firestore.collection("notes")

    fun getAllNotes(): Flow<List<Note>> = notesCollection.snapshots()
        .map { snapshot -> 
            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    Note(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String,
                        audioUrl = data["audioUrl"] as? String,
                        latitude = (data["latitude"] as? Double),
                        longitude = (data["longitude"] as? Double),
                        address = data["address"] as? String,
                        timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                    )
                }
            }
        }

    suspend fun getNote(noteId: String): Note? {
        return try {
            val doc = notesCollection.document(noteId).get().await()
            doc.data?.let { data ->
                Note(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    content = data["content"] as? String ?: "",
                    imageUrl = data["imageUrl"] as? String,
                    audioUrl = data["audioUrl"] as? String,
                    latitude = (data["latitude"] as? Double),
                    longitude = (data["longitude"] as? Double),
                    address = data["address"] as? String,
                    timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addNote(note: Note) {
        notesCollection.add(note).await()
    }

    suspend fun updateNote(note: Note) {
        notesCollection.document(note.id).set(note).await()
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesCollection.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
