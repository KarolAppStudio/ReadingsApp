package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.ui.components.AboutContent
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import com.karol.readingsapp.ui.theme.AppTheme
import com.karol.readingsapp.ui.theme.glassEffect

@Composable
fun AboutScreen(viewModel: ReadingViewModel, strings: LocalizedStrings, onHomeClick: () -> Unit) {
    val currentTheme by viewModel.appTheme.collectAsState()
    val isGlass = currentTheme == AppTheme.DARK_FROSTED_GLASS

    Scaffold(
        topBar = {
            AboutTopBar(strings = strings, onHomeClick = onHomeClick, isGlass = isGlass)
        },
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            AboutContent(
                strings = strings,
                isGlass = isGlass,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(AdaptiveDimens.paddingLarge)
                    .then(if (isGlass) Modifier.glassEffect() else Modifier),
            )
        }
    }
}

@Composable
fun AboutTopBar(strings: LocalizedStrings, onHomeClick: () -> Unit, isGlass: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(40.dp),
        color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = strings.home,
                    tint = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 40.dp else 30.dp),
                )
            }

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
            )
        }
    }
}
