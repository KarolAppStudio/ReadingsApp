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
fun FluidBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val colors = listOf(
            Color(0xFF5A70D6), // Darker 647DEE
            Color(0xFF725176), // Darker 7F5A83
            Color(0xFFCB8679), // Darker E29587
            Color(0xFFBFC98B), // Darker D4E09B
        )

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
