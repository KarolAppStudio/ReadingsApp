package com.karol.readingsapp.voice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Play/Stop Button
            IconButton(
                onClick = {
                    if (ttsState is TTSState.Speaking) {
                        viewModel.onStopClicked()
                    } else {
                        viewModel.onPlayClicked(textToRead, locale)
                    }
                },
            ) {
                Icon(
                    imageVector = if (ttsState is TTSState.Speaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (ttsState is TTSState.Speaking) "Stop" else "Play",
                )
            }

            // Status Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (ttsState) {
                        is TTSState.Speaking -> "Reading..."
                        is TTSState.Initializing -> "Initializing..."
                        else -> "Voice Ready"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (isOfflineAvailable && (ttsState is TTSState.Idle)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape,
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Offline Mode Enabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}
