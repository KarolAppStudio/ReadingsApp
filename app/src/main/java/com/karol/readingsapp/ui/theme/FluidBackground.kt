package com.karol.readingsapp.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluidBackground(
    modifier: Modifier = Modifier,
    appTheme: AppTheme = AppTheme.DARK_FROSTED_GLASS,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val colors = when (appTheme) {
            AppTheme.DARK_FROSTED_GLASS -> listOf(
                Color(0xFF1A1A1A), // Near Black Grey
                Color(0xFF242424), // Very Dark Grey
                Color(0xFF2E2E2E), // Dark Grey
                Color(0xFF1A1A1A),
            )

            else -> listOf(
                Color(0xFF1A1A1A),
                Color(0xFF242424),
                Color(0xFF2E2E2E),
                Color(0xFF1A1A1A),
            )
        }

        val angle = 0f
        val startX = size.width * (0.5f + (0.5f * cos(angle)))
        val startY = size.height * (0.5f + (0.5f * sin(angle)))
        val endX = size.width * (0.5f - (0.5f * cos(angle)))
        val endY = size.height * (0.5f - (0.5f * sin(angle)))

        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
        )
        drawRect(brush)
    }
}
