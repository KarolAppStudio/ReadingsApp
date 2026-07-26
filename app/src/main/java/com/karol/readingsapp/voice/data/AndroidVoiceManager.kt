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
import com.karol.readingsapp.voice.domain.VoiceGender
import com.karol.readingsapp.voice.domain.VoiceInfo
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
    private val prefs = context.getSharedPreferences("voice_prefs", Context.MODE_PRIVATE)

    private val _ttsState = MutableStateFlow<TTSState>(TTSState.Idle)
    override val ttsState = _ttsState.asStateFlow()

    private val _isOfflineAvailable = MutableStateFlow(value = false)
    override val isOfflineAvailable = _isOfflineAvailable.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceInfo>>(emptyList())
    override val availableVoices = _availableVoices.asStateFlow()

    private val _selectedVoice = MutableStateFlow<VoiceInfo?>(null)
    override val selectedVoice = _selectedVoice.asStateFlow()

    private var currentPitch = 1.0f
    private var currentRate = 1.0f

    private var lastSpokenText: String = ""
    private var lastSpokenLocale: Locale = Locale.getDefault()
    private var currentChunkIndex: Int = 0
    private var nextWordOffsetInCurrentChunk: Int = 0

    private val priorityLanguages = setOf("en", "hi", "bn", "kn", "ml", "ta", "te")

    init {
        initializeTTS()
    }

    private fun updateAvailableVoices() {
        tts?.let { engine ->
            try {
                val allVoices = engine.voices ?: emptyList()
                if (allVoices.isEmpty()) {
                    Log.w("AndroidVoiceManager", "No voices returned by TTS engine")
                    return
                }

                val filteredVoices = mutableListOf<VoiceInfo>()

                // Group by language - Filter only priority languages
                val languageGroups = allVoices
                    .filter { priorityLanguages.contains(it.locale.language) }
                    .groupBy { it.locale.language }

                languageGroups.forEach { (_, voicesInLang) ->
                    val limitPerGender = 5 // Use consistent limit for allowed languages

                    val detectedVoices = voicesInLang.map { voice ->
                        val gender = when {
                            voice.name.contains("female", ignoreCase = true) -> VoiceGender.FEMALE
                            voice.name.contains("male", ignoreCase = true) -> VoiceGender.MALE
                            voice.features?.contains("genderFemale") == true -> VoiceGender.FEMALE
                            voice.features?.contains("genderMale") == true -> VoiceGender.MALE
                            else -> VoiceGender.UNKNOWN
                        }

                        VoiceInfo(
                            name = voice.name,
                            locale = voice.locale,
                            isOffline = !voice.isNetworkConnectionRequired,
                            gender = gender,
                        )
                    }.sortedWith(
                        compareByDescending<VoiceInfo> { it.isOffline }
                            .thenBy { it.name },
                    )

                    // Limit per language to keep the list manageable but flexible
                    val females = detectedVoices.filter { it.gender == VoiceGender.FEMALE }.take(limitPerGender)
                    val males = detectedVoices.filter { it.gender == VoiceGender.MALE }.take(limitPerGender)

                    filteredVoices.addAll(females)
                    filteredVoices.addAll(males)

                    // Fallback: If no male/female detected for a language, take up to limit
                    if (females.isEmpty() && males.isEmpty()) {
                        filteredVoices.addAll(detectedVoices.take(limitPerGender))
                    }
                }

                _availableVoices.value = filteredVoices.sortedWith(
                    compareBy<VoiceInfo> { it.locale.language }
                        .thenBy { it.gender }
                        .thenBy { it.name },
                )

                // Restore last selected voice to the UI state
                val lastVoiceName = prefs.getString("last_selected_voice_name", null)
                val restoredVoice = filteredVoices.find { it.name == lastVoiceName }
                if (restoredVoice != null) {
                    _selectedVoice.value = restoredVoice
                } else if (_selectedVoice.value == null) {
                    // Pick a sensible default for current locale or English
                    val systemLang = Locale.getDefault().language
                    val defaultVoice = filteredVoices.find { it.locale.language == systemLang && it.isOffline }
                        ?: filteredVoices.find { it.locale.language == systemLang }
                        ?: filteredVoices.find { it.locale.language == "en" && it.isOffline }
                        ?: filteredVoices.firstOrNull()

                    _selectedVoice.value = defaultVoice
                }
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error updating voices", e)
            }
        }
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
                updateAvailableVoices()
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

                    // Extract chunk index from utteranceId if possible
                    utteranceId?.substringAfterLast("_")?.toIntOrNull()?.let { index ->
                        currentChunkIndex = index
                        nextWordOffsetInCurrentChunk = 0
                        Log.d("AndroidVoiceManager", "Updated currentChunkIndex to $index")
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    nextWordOffsetInCurrentChunk = end
                    Log.d("AndroidVoiceManager", "onRangeStart: start=$start, end=$end, utteranceId=$utteranceId")
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

    private fun splitIntoChunks(text: String, maxLimit: Int = 500): List<String> {
        if (text.length <= maxLimit) return listOf(text)

        val chunks = mutableListOf<String>()
        var remainingText = text

        while (remainingText.isNotEmpty()) {
            if (remainingText.length <= maxLimit) {
                chunks.add(remainingText)
                break
            }

            // Try to split at a sentence end
            var splitIndex = remainingText.lastIndexOf(". ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf("? ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf("! ", maxLimit)

            // If no sentence end, try semicolon or colon
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf("; ", maxLimit)
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf(": ", maxLimit)

            // If still no split point, try comma
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf(", ", maxLimit)

            // Fallback to space
            if (splitIndex == -1) splitIndex = remainingText.lastIndexOf(" ", maxLimit)

            if (splitIndex == -1 || (splitIndex < maxLimit / 4)) {
                // If no good split point found or it's too early, just hard cut at maxLimit
                splitIndex = maxLimit
            } else {
                // Include the punctuation/space
                splitIndex += 1
            }

            chunks.add(remainingText.substring(0, splitIndex).trim())
            remainingText = remainingText.substring(splitIndex).trim()
        }
        return chunks
    }

    override fun speak(text: String, language: Locale) {
        speakInternal(text, language, 0, 0)
    }

    private fun speakInternal(text: String, language: Locale, startIndex: Int, charOffset: Int) {
        val actualText = if (text.startsWith("DEBUG_TEST")) "This is a test of the Android Text to Speech system." else text
        val actualLanguage = if (text.startsWith("DEBUG_TEST")) Locale.US else language

        if (startIndex == 0 && charOffset == 0) {
            lastSpokenText = actualText
            lastSpokenLocale = actualLanguage
            currentChunkIndex = 0
            nextWordOffsetInCurrentChunk = 0
        }

        Log.d("AndroidVoiceManager", "speakInternal() called index: $startIndex, offset: $charOffset, text length: ${actualText.length}")
        if (requestAudioFocus()) {
            Log.d("AndroidVoiceManager", "Audio focus granted")
            tts?.let { ttsEngine ->
                // Check language availability more thoroughly
                val availability = ttsEngine.isLanguageAvailable(actualLanguage)
                Log.d("AndroidVoiceManager", "Language availability for $actualLanguage: $availability")

                if (availability == TextToSpeech.LANG_MISSING_DATA) {
                    Log.w("AndroidVoiceManager", "Language data is missing for $actualLanguage. Attempting to trigger installation.")
                }

                var langResult = ttsEngine.setLanguage(actualLanguage)
                Log.d("AndroidVoiceManager", "Initial setLanguage($actualLanguage) result: $langResult")

                if (langResult < TextToSpeech.LANG_AVAILABLE) {
                    val fallbackLocale = Locale.forLanguageTag(actualLanguage.language)
                    Log.w("AndroidVoiceManager", "Full locale not available, trying fallback: $fallbackLocale")
                    langResult = ttsEngine.setLanguage(fallbackLocale)
                }

                if (langResult == TextToSpeech.LANG_MISSING_DATA) {
                    _ttsState.value = TTSState.Error("Voice data missing. Tap to download.")
                    return
                }

                if (langResult < TextToSpeech.LANG_AVAILABLE) {
                    Log.e("AndroidVoiceManager", "Language $actualLanguage is not supported (result: $langResult)")
                    _ttsState.value = TTSState.Error("Language not supported")
                    return
                }
                Log.d("AndroidVoiceManager", "TTS language set to: ${ttsEngine.voice?.locale ?: ttsEngine.language}")

                // Try to find and set the voice
                try {
                    val allVoices = ttsEngine.voices

                    // 1. Try language-specific saved preference first
                    val savedVoiceName = prefs.getString("voice_lang_${actualLanguage.language}", null)
                    var preferredVoice = allVoices?.find { it.name == savedVoiceName }

                    // 2. If no language-specific saved voice, check the globally selected voice if it matches language
                    if (preferredVoice == null) {
                        preferredVoice = _selectedVoice.value?.let { selected ->
                            if (selected.locale.language == actualLanguage.language) {
                                allVoices?.find { it.name == selected.name }
                            } else {
                                null
                            }
                        }
                    }

                    if (preferredVoice != null) {
                        Log.d("AndroidVoiceManager", "Using preferred voice: ${preferredVoice.name}")
                        ttsEngine.voice = preferredVoice
                    } else {
                        // 3. Fallback to best available voice for this language
                        val langVoices = allVoices?.filter { it.locale.language == actualLanguage.language } ?: emptyList()

                        // Prioritize offline voices
                        val bestVoice = langVoices.sortedByDescending { !it.isNetworkConnectionRequired }
                            .firstOrNull()

                        if (bestVoice != null) {
                            Log.d("AndroidVoiceManager", "Setting voice to best fallback: ${bestVoice.name} (offline: ${!bestVoice.isNetworkConnectionRequired})")
                            ttsEngine.voice = bestVoice
                        } else {
                            Log.d("AndroidVoiceManager", "No suitable voice found for $actualLanguage, using engine default")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AndroidVoiceManager", "Error selecting voice", e)
                }

                ttsEngine.setPitch(currentPitch)
                ttsEngine.setSpeechRate(currentRate)

                // Chunking text to handle 4000 character limit
                // Using 500 for better resume granularity
                val allChunks = splitIntoChunks(actualText, 500)
                val chunksToSpeak = if (startIndex < allChunks.size) allChunks.drop(startIndex).toMutableList() else mutableListOf()

                if (chunksToSpeak.isNotEmpty() && charOffset > 0 && charOffset < chunksToSpeak[0].length) {
                    chunksToSpeak[0] = chunksToSpeak[0].substring(charOffset)
                }

                Log.d("AndroidVoiceManager", "Total chunks: ${allChunks.size}, remaining to speak: ${chunksToSpeak.size}")

                if (chunksToSpeak.isEmpty()) {
                    Log.d("AndroidVoiceManager", "No chunks to speak. Moving to Idle.")
                    _ttsState.value = TTSState.Idle
                    abandonAudioFocus()
                    return
                }

                chunksToSpeak.forEachIndexed { indexInRemaining, chunk ->
                    val absoluteIndex = startIndex + indexInRemaining
                    val utteranceId = "${UUID.randomUUID()}_$absoluteIndex"
                    if (absoluteIndex == allChunks.size - 1) {
                        lastUtteranceId = utteranceId
                    }
                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    }
                    val queueMode = if (indexInRemaining == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    val result = ttsEngine.speak(chunk, queueMode, params, utteranceId)

                    if (result == TextToSpeech.SUCCESS) {
                        Log.d("AndroidVoiceManager", "Chunk $absoluteIndex successfully queued: $utteranceId")
                    } else {
                        Log.e("AndroidVoiceManager", "Chunk $absoluteIndex failed to queue with result: $result")
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
        if (lastSpokenText.isNotEmpty()) {
            val chunks = splitIntoChunks(lastSpokenText, 500)
            var targetChunkIndex = currentChunkIndex
            var targetOffset = nextWordOffsetInCurrentChunk

            if (targetChunkIndex < chunks.size) {
                val currentChunk = chunks[targetChunkIndex]
                // Skip any leading whitespace at the new offset
                while (targetOffset < currentChunk.length && currentChunk[targetOffset].isWhitespace()) {
                    targetOffset++
                }
                // If we hit the end of the chunk, move to next chunk
                if (targetOffset >= currentChunk.length) {
                    targetChunkIndex++
                    targetOffset = 0
                }
            }
            speakInternal(lastSpokenText, lastSpokenLocale, targetChunkIndex, targetOffset)
        }
    }

    override fun stop() {
        tts?.stop()
        lastUtteranceId = null
        currentChunkIndex = 0
        nextWordOffsetInCurrentChunk = 0
        lastSpokenText = ""
        _ttsState.value = TTSState.Idle
        abandonAudioFocus()
    }

    override fun setPitch(pitch: Float) {
        currentPitch = pitch
    }
    override fun setRate(rate: Float) {
        currentRate = rate
    }

    override fun setVoice(voice: VoiceInfo) {
        _selectedVoice.value = voice
        prefs.edit().apply {
            putString("last_selected_voice_name", voice.name)
            putString("voice_lang_${voice.locale.language}", voice.name)
            apply()
        }
        tts?.let { engine ->
            engine.voices?.find { it.name == voice.name }?.let {
                engine.voice = it
                Log.d("AndroidVoiceManager", "Voice manually set to: ${it.name}")
            }
        }
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
