package com.example.diarys22387.util

import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.fixedRateTimer
import kotlin.math.abs

class AudioVisualizer {
    private var audioRecord: AudioRecord? = null
    private var visualizerTimer: java.util.Timer? = null
    private val bufferSize = 2048
    private val buffer = ShortArray(bufferSize)
    
    private val _amplitudes = MutableStateFlow<List<Float>>(List(30) { 0f })
    val amplitudes: StateFlow<List<Float>> = _amplitudes
    
    fun start(audioRecord: AudioRecord) {
        this.audioRecord = audioRecord
        startVisualizerUpdates()
    }
    
    fun stop() {
        visualizerTimer?.cancel()
        visualizerTimer = null
        _amplitudes.value = List(30) { 0f }
    }
    
    private fun startVisualizerUpdates() {
        visualizerTimer?.cancel()
        
        visualizerTimer = fixedRateTimer(period = 50) {
            audioRecord?.let { recorder ->
                val read = recorder.read(buffer, 0, bufferSize)
                if (read > 0) {
                    // Вычисляем среднюю амплитуду для текущего буфера
                    val amplitude = buffer.take(read)
                        .map { abs(it.toFloat()) }
                        .average()
                        .toFloat()
                    
                    // Нормализуем значение от 0 до 1
                    val normalizedAmplitude = (amplitude / Short.MAX_VALUE).coerceIn(0f, 1f)
                    
                    // Добавляем новое значение и удаляем старое
                    _amplitudes.value = _amplitudes.value.drop(1) + normalizedAmplitude
                }
            }
        }
    }
} 