package com.karol.readingsapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.ui.LocalizedStrings
import com.karol.readingsapp.ui.theme.glassEffect

@Composable
fun DownloadProgressOverlay(
    progress: Float?,
    strings: LocalizedStrings,
    isGlass: Boolean = false,
) {
    AnimatedVisibility(
        visible = progress != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isGlass) 0.2f else 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isGlass) 0.dp else 4.dp,
                modifier = Modifier
                    .width(280.dp)
                    .padding(16.dp)
                    .then(if (isGlass) Modifier.glassEffect() else Modifier),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = strings.downloadAll,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        progress = { progress ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                        trackColor = if (isGlass) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${((progress ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isGlass) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
