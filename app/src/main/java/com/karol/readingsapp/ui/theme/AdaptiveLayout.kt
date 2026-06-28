package com.karol.readingsapp.ui.theme

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

@Composable
fun ProvideWindowSizeClass(windowSizeClass: WindowSizeClass, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        content()
    }
}

object AdaptiveDimens {
    val windowSize: WindowSizeClass
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSizeClass.current

    val paddingSmall: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 8.dp
            WindowWidthSizeClass.Medium -> 12.dp
            else -> 16.dp
        }

    val paddingMedium: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 16.dp
            WindowWidthSizeClass.Medium -> 24.dp
            else -> 32.dp
        }

    val paddingLarge: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 24.dp
            WindowWidthSizeClass.Medium -> 36.dp
            else -> 48.dp
        }

    val fontScale: Float
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 1.0f
            WindowWidthSizeClass.Medium -> 1.15f
            else -> 1.3f
        }

    val titleFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (20 * fontScale).sp

    val bodyFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (16 * fontScale).sp

    val smallFontSize: TextUnit
        @Composable
        @ReadOnlyComposable
        get() = (14 * fontScale).sp

    val contentMaxWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> Dp.Unspecified
            WindowWidthSizeClass.Medium -> 720.dp
            else -> 1080.dp
        }
}
