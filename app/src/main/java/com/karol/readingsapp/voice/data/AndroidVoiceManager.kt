package com.karol.readingsapp.voice.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
        Log.d("AndroidVoiceManager", "Initializing TTS...")
        _ttsState.value = TTSState.Initializing
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("AndroidVoiceManager", "TTS Initialization Success")
                _ttsState.value = TTSState.Idle
                setupTTSListeners()
                checkOfflineSupport()
            } else {
                Log.e("AndroidVoiceManager", "TTS Initialization Failed with status: $status")
                _ttsState.value = TTSState.Error("TTS Initialization Failed")
            }
        }
    }

    private var lastUtteranceId: String? = null

    private fun setupTTSListeners() {
        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("AndroidVoiceManager", "Utterance onStart: $utteranceId")
                    _ttsState.value = TTSState.Speaking("Reading...")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("AndroidVoiceManager", "Utterance onDone: $utteranceId")
                    if (utteranceId == lastUtteranceId) {
                        Log.d("AndroidVoiceManager", "Last chunk finished. Transitioning to Idle.")
                        _ttsState.value = TTSState.Idle
                        abandonAudioFocus()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e("AndroidVoiceManager", "Utterance onError: $utteranceId")
                    _ttsState.value = TTSState.Error("Playback Error")
                    abandonAudioFocus()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e("AndroidVoiceManager", "Utterance onError: $utteranceId, errorCode: $errorCode")
                    lastUtteranceId = null
                    val message = if (errorCode == TextToSpeech.ERROR_NOT_INSTALLED_YET) {
                        "Voice data missing. Tap to download."
                    } else {
                        "Playback Error: $errorCode"
                    }
                    _ttsState.value = TTSState.Error(message)
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

    private fun splitIntoChunks(text: String, maxLimit: Int): List<String> {
        if (text.length <= maxLimit) return listOf(text)

        val chunks = mutableListOf<String>()
        var remainingText = text

        while (remainingText.isNotEmpty()) {
            if (remainingText.length <= maxLimit) {
                chunks.add(remainingText)
                break
            }

            var splitIndex = remainingText.lastIndexOf(". ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf("? ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf("! ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf(", ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf(" ", maxLimit)

            if (splitIndex == -1 || splitIndex < maxLimit / 2) {
                // If no good split point found, just hard cut at maxLimit
                splitIndex = maxLimit
            } else {
                // Include the punctuation
                splitIndex += 1
            }

            chunks.add(remainingText.substring(0, splitIndex).trim())
            remainingText = remainingText.substring(splitIndex).trim()
        }
        return chunks
    }

    override fun speak(text: String, language: Locale) {
        val actualText = if (text.startsWith("DEBUG_TEST")) "This is a test of the Android Text to Speech system." else text
        val actualLanguage = if (text.startsWith("DEBUG_TEST")) Locale.US else language

        Log.d("AndroidVoiceManager", "speak() called with language: $actualLanguage, text length: ${actualText.length}")
        if (requestAudioFocus()) {
            Log.d("AndroidVoiceManager", "Audio focus granted")
            tts?.let { ttsEngine ->
                // Check language availability more thoroughly
                val availability = ttsEngine.isLanguageAvailable(actualLanguage)
                Log.d("AndroidVoiceManager", "Language availability for $actualLanguage: $availability")

                if (availability == TextToSpeech.LANG_MISSING_DATA) {
                    Log.w("AndroidVoiceManager", "Language data is missing for $actualLanguage. Attempting to trigger installation.")
                    // We can't easily trigger download without an Activity context here for the intent,
                    // but we can at least log it and maybe the engine will start it.
                }

                var langResult = ttsEngine.setLanguage(actualLanguage)
                Log.d("AndroidVoiceManager", "Initial setLanguage($actualLanguage) result: $langResult")

                if (langResult < TextToSpeech.LANG_AVAILABLE) {
                    val fallbackLocale = Locale(actualLanguage.language)
                    Log.w("AndroidVoiceManager", "Full locale not available, trying fallback: $fallbackLocale")
                    langResult = ttsEngine.setLanguage(fallbackLocale)
                }

                if (langResult < TextToSpeech.LANG_AVAILABLE) {
                    Log.e("AndroidVoiceManager", "Language $actualLanguage is not supported (result: $langResult)")
                    _ttsState.value = TTSState.Error("Language not supported")
                    return
                }
                Log.d("AndroidVoiceManager", "TTS language set to: ${ttsEngine.language}")

                // Try to find and set an offline voice for the requested language
                try {
                    val allVoices = ttsEngine.voices
                    Log.d("AndroidVoiceManager", "Total voices available: ${allVoices?.size ?: 0}")

                    val langVoices = allVoices?.filter { it.locale.language == actualLanguage.language }
                    Log.d("AndroidVoiceManager", "Voices for ${actualLanguage.language}: ${langVoices?.map { "${it.name} (offline: ${!it.isNetworkConnectionRequired})" }}")

                    val offlineVoice = langVoices?.find { !it.isNetworkConnectionRequired }

                    if (offlineVoice != null) {
                        Log.d("AndroidVoiceManager", "Setting voice to offline: ${offlineVoice.name}")
                        ttsEngine.voice = offlineVoice
                    } else {
                        Log.d("AndroidVoiceManager", "No offline voice found for $actualLanguage, using default for language")
                    }
                } catch (e: Exception) {
                    Log.e("AndroidVoiceManager", "Error selecting voice", e)
                }

                ttsEngine.setPitch(currentPitch)
                ttsEngine.setSpeechRate(currentRate)

                // Chunking text to handle 4000 character limit
                val maxLimit = 3900 // Slightly under 4000 for safety
                val chunks = splitIntoChunks(actualText, maxLimit)
                Log.d("AndroidVoiceManager", "Splitting text into ${chunks.size} chunks")

                chunks.forEachIndexed { index, chunk ->
                    val utteranceId = "${UUID.randomUUID()}_$index"
                    if (index == chunks.size - 1) {
                        lastUtteranceId = utteranceId
                    }
                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    }
                    val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    val result = ttsEngine.speak(chunk, queueMode, params, utteranceId)

                    if (result == TextToSpeech.SUCCESS) {
                        Log.d("AndroidVoiceManager", "Chunk $index successfully queued: $utteranceId")
                    } else {
                        Log.e("AndroidVoiceManager", "Chunk $index failed to queue with result: $result")
                    }
                }
            }
        } else {
            Log.w("AndroidVoiceManager", "Audio focus denied")
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
        lastUtteranceId = null
        _ttsState.value = TTSState.Idle
        abandonAudioFocus()
    }

    override fun setPitch(pitch: Float) {
        currentPitch = pitch
    }
    override fun setRate(rate: Float) {
        currentRate = rate
    }

    override fun checkAndInstallVoices() {
        try {
            val intent = android.content.Intent()
            intent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AndroidVoiceManager", "Could not open TTS install intent", e)
        }
    }

    override fun shutdown() {
        tts?.shutdown()
        abandonAudioFocus()
    }
}
