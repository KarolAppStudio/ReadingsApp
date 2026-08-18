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

    val allProcessedVoices: StateFlow<List<VoiceInfo>> = availableVoices.map { voices ->
        // Only keep 3 OFFLINE Voices per Translation: 1 male and 2 female TTS voices
        voices.asSequence()
            .filter { it.isOffline }
            .groupBy {
                // Normalize language codes (e.g., Mizo/Lushai)
                when (it.locale.language) {
                    "miz" -> "lus"
                    else -> it.locale.language
                }
            }
            .flatMap { (_, langVoices) ->
                val males = langVoices.asSequence().filter { it.gender == VoiceGender.MALE }.take(1).toList()
                val females = langVoices.asSequence().filter { it.gender == VoiceGender.FEMALE }.take(2).toList()
                val selected = (males + females).toMutableList()

                // Fallback: If we don't have enough male/female voices, fill with others up to 3
                if (selected.size < 3) {
                    val others = langVoices.asSequence().filter { it !in selected }.take(3 - selected.size).toList()
                    selected.addAll(others)
                }
                selected
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredVoices: StateFlow<List<VoiceInfo>> = combine(allProcessedVoices, filterLocale) { voices, locale ->
        if (locale == null) {
            voices
        } else {
            val targetLang = when (locale.language) {
                "miz" -> "lus"
                else -> locale.language
            }
            voices.filter {
                val voiceLang = when (it.locale.language) {
                    "miz" -> "lus"
                    else -> it.locale.language
                }
                voiceLang == targetLang
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterVoices(locale: Locale, autoSelect: Boolean = false) {
        filterLocale.value = locale
        voiceService.ensureLanguageInstalled(locale)
        if (autoSelect) {
            viewModelScope.launch {
                // Wait for voices to be available if they are not
                val allVoices = allProcessedVoices.first { it.isNotEmpty() }

                // Only auto-select if current voice is null OR doesn't match the required language
                val current = selectedVoice.value
                val targetLang = when (locale.language) {
                    "miz" -> "lus"
                    else -> locale.language
                }

                val currentLang = current?.locale?.language?.let {
                    if (it == "miz") "lus" else it
                }

                if ((current == null) || (currentLang != targetLang)) {
                    // Pick the first voice from the allowed set (1 male, 2 female)
                    val langVoices = allVoices.filter {
                        val voiceLang = when (it.locale.language) {
                            "miz" -> "lus"
                            else -> it.locale.language
                        }
                        voiceLang == targetLang
                    }
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
