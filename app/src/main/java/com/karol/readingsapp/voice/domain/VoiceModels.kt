package com.karol.readingsapp.voice.domain

import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

enum class VoiceGender {
    MALE,
    FEMALE,
    UNKNOWN,
}

data class VoiceInfo(
    val name: String,
    val locale: Locale,
    val isOffline: Boolean,
    val gender: VoiceGender = VoiceGender.UNKNOWN,
)

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
    val availableVoices: StateFlow<List<VoiceInfo>>
    val selectedVoice: StateFlow<VoiceInfo?>

    fun speak(text: String, language: Locale = Locale.getDefault())
    fun pause()
    fun resume()
    fun stop()
    fun setPitch(pitch: Float)
    fun setRate(rate: Float)
    fun setVoice(voice: VoiceInfo)

    fun checkAndInstallVoices()
    fun shutdown()
}
