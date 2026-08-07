package com.karol.readingsapp.feature.voice.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

class VoiceServiceProxy(private val context: Context) : VoiceService {

    private var voiceService: VoicePlaybackService? = null
    private var isBound = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var collectionJob: Job? = null

    private val _ttsState = MutableStateFlow<TTSState>(TTSState.Idle)
    override val ttsState: StateFlow<TTSState> = _ttsState.asStateFlow()

    private val _isOfflineAvailable = MutableStateFlow(value = false)
    override val isOfflineAvailable: StateFlow<Boolean> = _isOfflineAvailable.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceInfo>>(value = emptyList())
    override val availableVoices: StateFlow<List<VoiceInfo>> = _availableVoices.asStateFlow()

    private val _selectedVoice = MutableStateFlow<VoiceInfo?>(value = null)
    override val selectedVoice: StateFlow<VoiceInfo?> = _selectedVoice.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as VoicePlaybackService.LocalBinder
            val actualService = binder.getService()
            voiceService = actualService
            isBound = true
            Log.d("VoiceServiceProxy", "Service connected")

            startCollecting(actualService.getVoiceService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            voiceService = null
            isBound = false
            stopCollecting()
            Log.d("VoiceServiceProxy", "Service disconnected")
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        Intent(context, VoicePlaybackService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun startCollecting(manager: VoiceService) {
        collectionJob?.cancel()
        collectionJob = Job()
        val jobScope = CoroutineScope(scope.coroutineContext + collectionJob!!)

        manager.ttsState.onEach { _ttsState.value = it }.launchIn(jobScope)
        manager.isOfflineAvailable.onEach { _isOfflineAvailable.value = it }.launchIn(jobScope)
        manager.availableVoices.onEach { _availableVoices.value = it }.launchIn(jobScope)
        manager.selectedVoice.onEach { _selectedVoice.value = it }.launchIn(jobScope)
    }

    private fun stopCollecting() {
        collectionJob?.cancel()
        collectionJob = null
    }

    private fun ensureServiceStarted() {
        val intent = Intent(context, VoicePlaybackService::class.java)
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e("VoiceServiceProxy", "Failed to start foreground service", e)
            context.startService(intent)
        }
    }

    override fun speak(text: String, language: Locale) {
        ensureServiceStarted()
        voiceService?.getVoiceService()?.speak(text, language)
    }

    override fun pause() {
        voiceService?.getVoiceService()?.pause()
    }

    override fun resume() {
        voiceService?.getVoiceService()?.resume()
    }

    override fun stop() {
        voiceService?.getVoiceService()?.stop()
    }

    override fun setPitch(pitch: Float) {
        voiceService?.getVoiceService()?.setPitch(pitch)
    }

    override fun setRate(rate: Float) {
        voiceService?.getVoiceService()?.setRate(rate)
    }

    override fun setVoice(voice: VoiceInfo) {
        voiceService?.getVoiceService()?.setVoice(voice)
    }

    override fun checkAndInstallVoices() {
        voiceService?.getVoiceService()?.checkAndInstallVoices()
    }

    override fun ensureLanguageInstalled(locale: Locale) {
        voiceService?.getVoiceService()?.ensureLanguageInstalled(locale)
    }

    override fun shutdown() {
        stopCollecting()
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}
