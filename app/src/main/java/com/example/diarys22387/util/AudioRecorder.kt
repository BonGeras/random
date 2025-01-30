package com.example.diarys22387.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var audioRecord: AudioRecord? = null
    private var currentFile: File? = null
    private val visualizer = AudioVisualizer()
    
    val amplitudes = visualizer.amplitudes

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Throws(SecurityException::class)
    fun startRecording(): Uri {
        if (!hasRecordPermission()) {
            throw SecurityException("Recording permission not granted")
        }

        val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
        currentFile = audioFile

        recorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            prepare()
            start()
        }
        
        val minBufferSize = AudioRecord.getMinBufferSize(
            44100,
            android.media.AudioFormat.CHANNEL_IN_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        )
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            android.media.AudioFormat.CHANNEL_IN_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )

        audioRecord?.startRecording()
        visualizer.start(audioRecord!!)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            audioFile
        )
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    fun stopRecording() {
        try {
            visualizer.stop()
            audioRecord?.apply {
                stop()
                release()
            }
            audioRecord = null
            
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    fun deleteRecording() {
        currentFile?.delete()
        currentFile = null
    }

    fun release() {
        stopRecording()
        visualizer.stop()
        deleteRecording()
    }
} 