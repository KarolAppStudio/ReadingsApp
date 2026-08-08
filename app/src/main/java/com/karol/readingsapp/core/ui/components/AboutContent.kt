package com.karol.readingsapp.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens

@Composable
fun AboutContent(strings: LocalizedStrings, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdaptiveDimens.paddingMedium),
    ) {
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

        // The Team Card
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AutoResizingText(
                    text = strings.theTeam,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )

                ScrollingCredits()
            }
        }
    }
}

@Composable
private fun ScrollingCredits() {
    val credits = listOf(
        "Diana B - Kannada Linguistic QA",
        "Jayachandran M R - Malayalam Linguistic QA",
        "Mathews P. J - Malayalam Linguistic QA, UI",
        "Naomi B - Kannada Linguistic QA",
        "Prabhu Kiran - Telugu Linguistic QA",
        "Ratheesh Vas - Malayalam Linguistic QA",
        "Ruth Beverly - English Linguistic QA, UI/UX",
        "Sharmela P - Tamil Linguistic QA, UI/UX",
        "Subrata Ganguli - Bangla Linguistic QA",
    )

    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            while (true) {
                scrollState.animateScrollTo(
                    value = scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = scrollState.maxValue * 35,
                        easing = LinearEasing,
                    ),
                )
                scrollState.scrollTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clipToBounds(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState, enabled = false),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            credits.forEach { credit ->
                val annotatedString = buildAnnotatedString {
                    val dashIndex = credit.indexOf(" - ")
                    if (dashIndex != -1) {
                        append(credit.substring(0, dashIndex))
                        append(" ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append("-")
                        }
                        append(" ")
                        append(credit.substring(dashIndex + 3))
                    } else {
                        append(credit)
                    }
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
