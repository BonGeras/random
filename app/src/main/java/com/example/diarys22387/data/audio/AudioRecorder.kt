package com.example.diarys22387.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): File {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3")
        
        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                
                prepare()
                start()
            }
            
            currentFile = file
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording", e)
            file.delete()
            release()
            throw e
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recording", e)
            throw e
        } finally {
            recorder = null
        }
    }

    fun release() {
        try {
            recorder?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error releasing recorder", e)
        } finally {
            recorder = null
            currentFile?.delete()
            currentFile = null
        }
    }

    fun getCurrentFile(): File? = currentFile
} 