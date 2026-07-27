package com.karol.readingsapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    }
}
