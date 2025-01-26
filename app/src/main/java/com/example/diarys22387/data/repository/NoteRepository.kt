package com.example.diarys22387.data.repository

import com.example.diarys22387.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notesColl = firestore.collection("notes")

    fun getAllNotes(): Flow<List<Note>> = callbackFlow {
        val listener = notesColl
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val notes = snapshot?.documents?.mapNotNull { 
                    try {
                        it.toObject<Note>()?.copy(id = it.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()
                
                trySend(notes)
            }
            
        awaitClose { 
            listener.remove() 
        }
    }.catch { e ->
        e.printStackTrace()
        emit(emptyList())
    }

    suspend fun getNote(id: String): Note? = try {
        notesColl.document(id).get().await().toObject<Note>()?.copy(id = id)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    suspend fun addNote(note: Note): Result<String> = try {
        val docRef = if (note.id.isEmpty()) {
            notesColl.document()
        } else {
            notesColl.document(note.id)
        }
        val noteWithId = note.copy(id = docRef.id)
        docRef.set(noteWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    suspend fun updateNote(note: Note) = try {
        if (note.id.isEmpty()) {
            Result.failure(IllegalArgumentException("Note ID cannot be empty for update"))
        } else {
            notesColl.document(note.id).set(note).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    suspend fun deleteNote(noteId: String) = try {
        notesColl.document(noteId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
