package com.example.diarys22387.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: MediaPlayer? = null
    private var currentUrl: String? = null
    private var progressTimer: java.util.Timer? = null
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition
    
    fun startPlaying(url: String, onComplete: () -> Unit) {
        if (currentUrl == url && player?.isPlaying == true) {
            stopPlaying()
            return
        }
        
        stopPlaying()
        
        try {
            player = MediaPlayer().apply {
                setDataSource(context, Uri.parse(url))
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration
                    startProgressUpdates()
                    start()
                }
                setOnCompletionListener {
                    stopPlaying()
                    onComplete()
                }
                prepareAsync()
            }
            currentUrl = url
        } catch (e: Exception) {
            e.printStackTrace()
            stopPlaying()
        }
    }
    
    fun stopPlaying() {
        progressTimer?.cancel()
        progressTimer = null
        
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
        currentUrl = null
        _progress.value = 0f
        _currentPosition.value = 0
        _duration.value = 0
    }
    
    fun seekTo(position: Float) {
        player?.let { mp ->
            val newPosition = (position * mp.duration).toInt()
            mp.seekTo(newPosition)
            _currentPosition.value = newPosition
            _progress.value = position
        }
    }
    
    private fun startProgressUpdates() {
        progressTimer?.cancel()
        
        progressTimer = fixedRateTimer(period = 100) {
            player?.let { mp ->
                if (mp.isPlaying) {
                    _currentPosition.value = mp.currentPosition
                    _progress.value = mp.currentPosition.toFloat() / mp.duration
                }
            }
        }
    }
    
    fun isPlaying(url: String): Boolean {
        return player?.isPlaying == true && currentUrl == url
    }
    
    fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun release() {
        stopPlaying()
        progressTimer?.cancel()
        progressTimer = null
    }
} 