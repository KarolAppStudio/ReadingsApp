package com.karol.readingsapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.ui.LocalizedStrings
import com.karol.readingsapp.ui.theme.AdaptiveDimens

@Composable
fun AboutContent(
    strings: LocalizedStrings,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Text(
                text = strings.appDescription,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontSize = AdaptiveDimens.bodyFontSize,
                lineHeight = AdaptiveDimens.bodyFontSize * 1.5f,
                modifier = Modifier
                    .padding(AdaptiveDimens.paddingMedium)
                    .fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(
                modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
            ) {
                Text(
                    text = strings.developerNoteTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.developerNoteContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    lineHeight = AdaptiveDimens.smallFontSize * 1.4f,
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(
                modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
            ) {
                Text(
                    text = "Copyright Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.copyrightNotice,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = AdaptiveDimens.smallFontSize,
                    lineHeight = AdaptiveDimens.smallFontSize * 1.4f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptiveDimens.paddingLarge))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = AdaptiveDimens.smallFontSize,
            )
            Text(
                text = strings.developedBy,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                fontSize = AdaptiveDimens.smallFontSize * 0.8f,
            )
        }
    }
}
