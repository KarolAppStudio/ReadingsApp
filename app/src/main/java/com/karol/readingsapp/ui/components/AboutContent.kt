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
    isPleasant: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = strings.appDescription,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontSize = AdaptiveDimens.bodyFontSize,
            lineHeight = AdaptiveDimens.bodyFontSize * 1.5f,
            modifier = Modifier.padding(vertical = AdaptiveDimens.paddingMedium),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp),
        ) {
            Column(
                modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
            ) {
                Text(
                    text = strings.developerNoteTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPleasant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.developerNoteContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPleasant) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    lineHeight = AdaptiveDimens.smallFontSize * 1.4f,
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
