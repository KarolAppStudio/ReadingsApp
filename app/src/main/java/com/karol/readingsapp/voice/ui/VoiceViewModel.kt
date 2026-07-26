package com.karol.readingsapp.voice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.karol.readingsapp.voice.data.AndroidVoiceManager
import com.karol.readingsapp.voice.domain.VoiceService

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val voiceService: VoiceService = AndroidVoiceManager(application)
    val availableVoices = voiceService.availableVoices
    val selectedVoice = voiceService.selectedVoice

    fun onPlayClicked(textToRead: String, locale: java.util.Locale = java.util.Locale.getDefault()) {
        if (textToRead.isNotBlank()) {
            voiceService.speak(textToRead, locale)
        }
    }

    fun onStopClicked() {
        voiceService.stop()
    }

    fun onPauseClicked() {
        voiceService.pause()
    }

    fun onResumeClicked() {
        voiceService.resume()
    }

    fun onVoiceSelected(voice: com.karol.readingsapp.voice.domain.VoiceInfo) {
        voiceService.setVoice(voice)
    }

    override fun onCleared() {
        voiceService.shutdown()
    }
}
