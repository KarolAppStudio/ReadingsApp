package com.karol.readingsapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassEffect(
    cornerRadius: Dp = 12.dp,
    borderWidth: Dp = 1.5.dp,
    alpha: Float = 0.2f,
): Modifier = this.then(
    Modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha + 0.1f),
                    Color.White.copy(alpha = alpha),
                ),
            ),
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.6f),
                    Color.White.copy(alpha = 0.1f),
                ),
            ),
            shape = RoundedCornerShape(cornerRadius),
        ),
)
