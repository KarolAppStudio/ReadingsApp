package com.karol.readingsapp.voice.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.karol.readingsapp.voice.domain.TTSState
import com.karol.readingsapp.voice.domain.VoiceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class AndroidVoiceManager(
    private val context: Context,
) : VoiceService {

    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _ttsState = MutableStateFlow<TTSState>(TTSState.Idle)
    override val ttsState = _ttsState.asStateFlow()

    private val _isOfflineAvailable = MutableStateFlow(value = false)
    override val isOfflineAvailable = _isOfflineAvailable.asStateFlow()

    private var currentPitch = 1.0f
    private var currentRate = 1.0f

    init {
        initializeTTS()
    }

    private fun checkOfflineSupport() {
        // For TTS, check if any voice is offline
        tts?.let {
            val hasOfflineVoice = it.voices?.any { voice -> !voice.isNetworkConnectionRequired } ?: false
            _isOfflineAvailable.value = hasOfflineVoice
        }
    }

    private fun initializeTTS() {
        _ttsState.value = TTSState.Initializing
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _ttsState.value = TTSState.Idle
                setupTTSListeners()
                checkOfflineSupport()
            } else {
                _ttsState.value = TTSState.Error("TTS Initialization Failed")
            }
        }
    }

    private fun setupTTSListeners() {
        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _ttsState.value = TTSState.Speaking("Reading...")
                }

                override fun onDone(utteranceId: String?) {
                    _ttsState.value = TTSState.Idle
                    abandonAudioFocus()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _ttsState.value = TTSState.Error("Playback Error")
                    abandonAudioFocus()
                }
            },
        )
    }

    private fun requestAudioFocus(): Boolean {
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(playbackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    -> stop()

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        tts?.setSpeechRate(currentRate * 0.5f) // Ducking
                    }

                    AudioManager.AUDIOFOCUS_GAIN -> {
                        tts?.setSpeechRate(currentRate)
                    }
                }
            }
            .build()

        return audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    override fun speak(text: String, language: Locale) {
        if (requestAudioFocus()) {
            tts?.let {
                it.language = language

                // Try to find and set an offline voice for the requested language
                try {
                    it.voices?.find { voice ->
                        (voice.locale.language == language.language) && !voice.isNetworkConnectionRequired
                    }?.let { offlineVoice ->
                        it.voice = offlineVoice
                    }
                } catch (_: Exception) {
                    // Fallback to default if voice selection fails
                }

                it.setPitch(currentPitch)
                it.setSpeechRate(currentRate)
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UUID.randomUUID().toString())
                }
                it.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
            }
        }
    }

    override fun pause() {
        tts?.stop()
        _ttsState.value = TTSState.Paused
        abandonAudioFocus()
    }

    override fun resume() {
        // Resume logic would require keeping track of where we stopped
    }

    override fun stop() {
        tts?.stop()
        _ttsState.value = TTSState.Idle
        abandonAudioFocus()
    }

    override fun setPitch(pitch: Float) {
        currentPitch = pitch
    }
    override fun setRate(rate: Float) {
        currentRate = rate
    }

    override fun shutdown() {
        tts?.shutdown()
        abandonAudioFocus()
    }
}
