package com.karol.readingsapp.voice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.karol.readingsapp.voice.data.AndroidVoiceManager
import com.karol.readingsapp.voice.domain.VoiceService

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val voiceService: VoiceService = AndroidVoiceManager(application)

    fun onPlayClicked(textToRead: String, locale: java.util.Locale = java.util.Locale.getDefault()) {
        if (textToRead.isNotBlank()) {
            voiceService.speak(textToRead, locale)
        }
    }

    fun onStopClicked() {
        voiceService.stop()
    }

    override fun onCleared() {
        voiceService.shutdown()
    }
}
