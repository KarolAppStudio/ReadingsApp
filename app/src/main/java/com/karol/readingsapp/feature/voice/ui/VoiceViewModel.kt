package com.karol.readingsapp.feature.voice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.feature.voice.data.VoiceGender
import com.karol.readingsapp.feature.voice.data.VoiceInfo
import com.karol.readingsapp.feature.voice.data.VoiceService
import com.karol.readingsapp.feature.voice.data.VoiceServiceProxy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val voiceService: VoiceService = VoiceServiceProxy(application)
    val availableVoices = voiceService.availableVoices
    val selectedVoice = voiceService.selectedVoice

    private val filterLocale = MutableStateFlow<Locale?>(null)

    val filteredVoices: StateFlow<List<VoiceInfo>> = combine(availableVoices, filterLocale) { voices, locale ->
        val baseVoices = if (locale == null) {
            voices
        } else {
            voices.filter { it.locale.language == locale.language }
        }

        // Only keep 3 Voices per Translation: 1 male and 2 female TTS voices
        baseVoices.groupBy { it.locale.language }
            .flatMap { (_, langVoices) ->
                val males = langVoices.filter { it.gender == VoiceGender.MALE }.take(1)
                val females = langVoices.filter { it.gender == VoiceGender.FEMALE }.take(2)
                males + females
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterVoices(locale: Locale, autoSelect: Boolean = false) {
        filterLocale.value = locale
        voiceService.ensureLanguageInstalled(locale)
        if (autoSelect) {
            viewModelScope.launch {
                // Wait for voices to be available if they are not
                val allVoices = availableVoices.first { it.isNotEmpty() }

                // Only auto-select if current voice is null OR doesn't match the required language
                val current = selectedVoice.value
                if (current == null || current.locale.language != locale.language) {
                    // Pick the first voice from the allowed set (1 male, 2 female)
                    val langVoices = allVoices.filter { it.locale.language == locale.language }
                    val allowedVoice = langVoices.find { it.gender == VoiceGender.MALE }
                        ?: langVoices.find { it.gender == VoiceGender.FEMALE }
                        ?: langVoices.firstOrNull()

                    allowedVoice?.let { onVoiceSelected(it) }
                }
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

    fun onVoiceSelected(voice: VoiceInfo) {
        voiceService.setVoice(voice)
    }

    override fun onCleared() {
        voiceService.shutdown()
    }
}
