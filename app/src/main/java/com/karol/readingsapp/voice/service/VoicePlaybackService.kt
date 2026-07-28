package com.karol.readingsapp.voice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.karol.readingsapp.MainActivity
import com.karol.readingsapp.voice.data.AndroidVoiceManager
import com.karol.readingsapp.voice.domain.TTSState
import com.karol.readingsapp.voice.domain.VoiceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VoicePlaybackService : Service() {

    private val binder = LocalBinder()
    private lateinit var voiceManager: AndroidVoiceManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "voice_playback_channel"
        private const val ACTION_STOP = "com.karol.readingsapp.ACTION_STOP"
        private const val ACTION_PAUSE = "com.karol.readingsapp.ACTION_PAUSE"
        private const val ACTION_RESUME = "com.karol.readingsapp.ACTION_RESUME"
    }

    inner class LocalBinder : Binder() {
        fun getService(): VoicePlaybackService = this@VoicePlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        voiceManager = AndroidVoiceManager(this)
        createNotificationChannel()
        observeTTSState()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("VoicePlaybackService", "onStartCommand action: ${intent?.action}")

        // Ensure foreground status immediately on start to satisfy Android 12+ requirements
        showForegroundNotification(isPlaying = false)

        when (intent?.action) {
            ACTION_STOP -> stopPlayback()
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
        }
        return START_NOT_STICKY
    }

    private fun showForegroundNotification(isPlaying: Boolean) {
        val notification = createNotification(isPlaying)
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }

    private fun observeTTSState() {
        voiceManager.ttsState.onEach { state ->
            Log.d("VoicePlaybackService", "TTS State changed: $state")
            when (state) {
                is TTSState.Speaking -> {
                    showForegroundNotification(isPlaying = true)
                }

                TTSState.Paused -> {
                    // Update notification and stay foreground to keep controls visible
                    showForegroundNotification(isPlaying = false)
                }

                TTSState.Idle -> {
                    Log.d("VoicePlaybackService", "Idle state, stopping foreground and service")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                is TTSState.Error -> {
                    Log.e("VoicePlaybackService", "Error state: ${state.message}")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                else -> {}
            }
        }.launchIn(serviceScope)
    }

    private fun createNotification(isPlaying: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, VoicePlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = Intent(this, VoicePlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_RESUME
        }
        val playPausePendingIntent = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Bible Reading")
            .setContentText(if (isPlaying) "Speaking..." else "Paused")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setContentIntent(pendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Resume",
                playPausePendingIntent,
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setStyle(
                @Suppress("DEPRECATION")
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1),
            )

        return builder.build()
    }

    private fun createNotificationChannel() {
        val name = "Voice Playback"
        val descriptionText = "Controls for Bible text-to-speech"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setShowBadge(false)
            setSound(null, null) // Explicitly disable sound for this channel
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun getVoiceService(): VoiceService = voiceManager

    private fun stopPlayback() {
        voiceManager.stop()
    }

    private fun pausePlayback() {
        voiceManager.pause()
    }

    private fun resumePlayback() {
        voiceManager.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.shutdown()
        }
        serviceScope.cancel()
    }
}
