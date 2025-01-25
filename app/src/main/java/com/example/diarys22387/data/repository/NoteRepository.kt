package com.example.diarys22387.data.repository

import com.example.diarys22387.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
                        it.toObject<Note>()
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
        notesColl.document(id).get().await().toObject()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    suspend fun addNote(note: Note) = try {
        notesColl.document(note.id).set(note).await()
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }

    suspend fun updateNote(note: Note) = try {
        notesColl.document(note.id).set(note).await()
        Result.success(Unit)
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
