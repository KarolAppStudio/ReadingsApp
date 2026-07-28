package com.karol.readingsapp.voice.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karol.readingsapp.voice.domain.TTSState

@Composable
fun VoiceControlBar(
    viewModel: VoiceViewModel,
    textToRead: String,
    modifier: Modifier = Modifier,
    locale: java.util.Locale = java.util.Locale.getDefault(),
) {
    val ttsState by viewModel.voiceService.ttsState.collectAsStateWithLifecycle()
    val isOfflineAvailable by viewModel.voiceService.isOfflineAvailable.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val targetWidth = remember(density) { with(density) { 600.toDp() } }

    var lastPlayedText by remember { mutableStateOf("") }

    // Disable system sound effects (beeps) when interacting with this bar
    val view = LocalView.current
    DisposableEffect(view) {
        val original = view.isSoundEffectsEnabled
        view.isSoundEffectsEnabled = false
        onDispose {
            view.isSoundEffectsEnabled = original
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .width(targetWidth)
                .height(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Stop Button
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        viewModel.onStopClicked()
                        lastPlayedText = ""
                    },
                    enabled = (ttsState is TTSState.Speaking) || (ttsState is TTSState.Paused),
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(24.dp),
                        tint = if ((ttsState is TTSState.Speaking) || (ttsState is TTSState.Paused)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Text
                Column(
                    modifier = Modifier
                        .clickable(enabled = ttsState is TTSState.Error) {
                            if (ttsState is TTSState.Error) {
                                viewModel.voiceService.checkAndInstallVoices()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val statusText = when (val state = ttsState) {
                        is TTSState.Speaking -> "Reading..."
                        is TTSState.Paused -> "Paused"
                        is TTSState.Initializing -> "Initializing..."
                        is TTSState.Error -> state.message
                        else -> ""
                    }
                    if (statusText.isNotEmpty()) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ttsState is TTSState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (isOfflineAvailable && (ttsState is TTSState.Idle)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape,
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play/Pause Button
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        when (ttsState) {
                            is TTSState.Speaking -> viewModel.onPauseClicked()

                            is TTSState.Paused -> {
                                if (textToRead == lastPlayedText) {
                                    viewModel.onResumeClicked()
                                } else {
                                    lastPlayedText = textToRead
                                    viewModel.onPlayClicked(textToRead, locale)
                                }
                            }

                            else -> {
                                lastPlayedText = textToRead
                                viewModel.onPlayClicked(textToRead, locale)
                            }
                        }
                    },
                ) {
                    val icon = if (ttsState is TTSState.Speaking) Icons.Default.Pause else Icons.Default.PlayArrow
                    Icon(
                        imageVector = icon,
                        contentDescription = if (ttsState is TTSState.Speaking) "Pause" else "Play",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
