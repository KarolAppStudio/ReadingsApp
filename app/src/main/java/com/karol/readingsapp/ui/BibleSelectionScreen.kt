package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.CardLavender
import com.karol.readingsapp.ui.theme.TextBlue

@Composable
fun BibleSelectionScreen(
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(40.dp),
                color = BackgroundBlue,
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
                            tint = TextBlue,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = TextBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                    label = { Text(strings.home) },
                    selected = false,
                    onClick = onHomeClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = strings.calendar) },
                    label = { Text(strings.calendar) },
                    selected = false,
                    onClick = onCalendarClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.bible) },
                    label = { Text(strings.bible) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextBlue,
                        selectedTextColor = TextBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = CardLavender,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.settings) },
                    label = { Text(strings.settings) },
                    selected = false,
                    onClick = onSettingsClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
            }
        },
        containerColor = BackgroundBlue,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
