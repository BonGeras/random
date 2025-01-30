package com.example.diarys22387.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.diarys22387.util.AudioRecorder
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val audioRecorder: AudioRecorder,
    @ApplicationContext private val context: Context
) {
    private var isRecording = false
    private var currentAudioUri: Uri? = null

    fun startRecording(): Uri {
        if (!isRecording) {
            try {
                currentAudioUri = audioRecorder.startRecording()
                isRecording = true
                return currentAudioUri!!
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        return currentAudioUri!!
    }

    fun stopRecording() {
        if (isRecording) {
            audioRecorder.stopRecording()
            isRecording = false
        }
    }

    suspend fun uploadAudio(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open audio file")

            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "mp3"
            val fileName = "audio_${System.currentTimeMillis()}.$extension"
            val audioRef = storage.reference.child("audio/$fileName")

            withContext(Dispatchers.IO) {
                audioRef.putStream(inputStream).await()
            }
            audioRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to upload audio: ${e.message}")
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open image file")

            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "jpg"
            val fileName = "image_${System.currentTimeMillis()}.$extension"
            val imageRef = storage.reference.child("images/$fileName")

            withContext(Dispatchers.IO) {
                imageRef.putStream(inputStream).await()
            }
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    
    private suspend fun copyUriToCache(uri: Uri, prefix: String): File = withContext(Dispatchers.IO) {
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: ""
        
        val cacheDir = when {
            prefix.startsWith("image") -> File(context.cacheDir, "images").apply { mkdirs() }
            prefix.startsWith("audio") -> File(context.cacheDir, "audio").apply { mkdirs() }
            else -> context.cacheDir
        }
        
        val tempFile = File(cacheDir, "${prefix}_${System.currentTimeMillis()}.$extension")
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Cannot open input file")
        
        tempFile
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
        audioRecorder.release()
    }
}
