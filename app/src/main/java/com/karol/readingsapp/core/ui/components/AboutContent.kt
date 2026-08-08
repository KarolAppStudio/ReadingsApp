package com.karol.readingsapp.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens

@Composable
fun AboutContent(strings: LocalizedStrings, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdaptiveDimens.paddingMedium),
    ) {
        // App Description Card
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

        // Developer Note Card
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
                AutoResizingText(
                    text = strings.developerNoteTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = strings.developerNoteContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = AdaptiveDimens.smallFontSize,
                    lineHeight = AdaptiveDimens.smallFontSize * 1.5f,
                )
            }
        }

        // Copyright Information Card
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
                AutoResizingText(
                    text = strings.copyrightTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = strings.copyrightNotice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = AdaptiveDimens.smallFontSize,
                    lineHeight = AdaptiveDimens.smallFontSize * 1.5f,
                )
            }
        }
    }
}
