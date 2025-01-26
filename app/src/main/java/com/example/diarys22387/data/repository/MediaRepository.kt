package com.example.diarys22387.data.repository

import android.net.Uri
import com.example.diarys22387.util.AudioRecorder
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val audioRecorder: AudioRecorder
) {
    private var isRecording = false
    private var currentAudioUri: Uri? = null

    fun startRecording(): Uri? {
        if (!isRecording) {
            try {
                currentAudioUri = audioRecorder.startRecording()
                isRecording = true
                return currentAudioUri
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        return currentAudioUri
    }

    fun stopRecording() {
        if (isRecording) {
            audioRecorder.stopRecording()
            isRecording = false
        }
    }

    suspend fun uploadAudio(uri: Uri): String {
        return try {
            val audioRef = storage.reference.child("audio/${UUID.randomUUID()}.mp3")
            audioRef.putFile(uri).await()
            audioRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to upload audio: ${e.message}")
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        return try {
            val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
        audioRecorder.release()
    }
}
