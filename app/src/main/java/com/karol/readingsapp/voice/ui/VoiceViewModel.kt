package com.karol.readingsapp.voice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.voice.data.AndroidVoiceManager
import com.karol.readingsapp.voice.domain.VoiceInfo
import com.karol.readingsapp.voice.domain.VoiceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val voiceService: VoiceService = AndroidVoiceManager(application)
    val availableVoices = voiceService.availableVoices
    val selectedVoice = voiceService.selectedVoice

    private val filterLocale = MutableStateFlow<Locale?>(null)

    val filteredVoices: StateFlow<List<VoiceInfo>> = combine(availableVoices, filterLocale) { voices, locale ->
        if (locale == null) {
            voices
        } else {
            voices.filter { it.locale.language == locale.language }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterVoices(locale: Locale, autoSelect: Boolean = false) {
        filterLocale.value = locale
        if (autoSelect) {
            viewModelScope.launch {
                // Wait for voices to be available if they are not
                availableVoices.first { it.isNotEmpty() }
                val voices = availableVoices.value
                val firstVoice = voices.find { it.locale.language == locale.language }
                firstVoice?.let { onVoiceSelected(it) }
            }
        }
    }

    fun onPlayClicked(textToRead: String, locale: Locale = Locale.getDefault()) {
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
