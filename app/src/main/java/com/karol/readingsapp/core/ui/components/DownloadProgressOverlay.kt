package com.karol.readingsapp.core.ui.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.core.i18n.Localization
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.theme.ProvideWindowSizeClass
import com.karol.readingsapp.core.theme.ReadingsAppTheme

@Composable
fun DownloadProgressOverlay(progress: Float?, strings: LocalizedStrings) {
    AnimatedVisibility(
        visible = progress != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .width(280.dp)
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AutoResizingText(
                        text = strings.downloadingSelected,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = AdaptiveDimens.bodyFontSize,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        progress = { progress ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AutoResizingText(
                        text = "${((progress ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = AdaptiveDimens.smallFontSize,
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun DownloadProgressOverlayPreview() {
    val windowSizeClass = androidx.compose.material3.windowsizeclass.WindowSizeClass.calculateFromSize(
        DpSize(360.dp, 640.dp),
    )
    ProvideWindowSizeClass(windowSizeClass) {
        ReadingsAppTheme {
            DownloadProgressOverlay(
                progress = 0.45f,
                strings = Localization.getStrings("English"),
            )
        }
    }
}

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun DownloadProgressOverlayTamilPreview() {
    val windowSizeClass = androidx.compose.material3.windowsizeclass.WindowSizeClass.calculateFromSize(
        DpSize(360.dp, 640.dp),
    )
    ProvideWindowSizeClass(windowSizeClass) {
        ReadingsAppTheme {
            DownloadProgressOverlay(
                progress = 0.75f,
                strings = Localization.getStrings("Tamil"),
            )
        }
    }
}
