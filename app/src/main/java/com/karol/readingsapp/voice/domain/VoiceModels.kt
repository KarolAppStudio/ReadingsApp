package com.karol.readingsapp.voice.domain

import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

sealed interface TTSState {
    object Idle : TTSState
    object Initializing : TTSState
    data class Speaking(val text: String, val progress: Float = 0f) : TTSState
    object Paused : TTSState
    data class Error(val message: String) : TTSState
}

interface VoiceService {
    val ttsState: StateFlow<TTSState>
    val isOfflineAvailable: StateFlow<Boolean>

    fun speak(text: String, language: Locale = Locale.getDefault())
    fun pause()
    fun resume()
    fun stop()
    fun setPitch(pitch: Float)
    fun setRate(rate: Float)

    fun shutdown()
}
