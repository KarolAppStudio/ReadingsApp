package com.karol.readingsapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import com.karol.readingsapp.ui.theme.AppTheme
import com.karol.readingsapp.ui.theme.GlassBorder

@Composable
fun SettingsScreen(
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onBibleClick: () -> Unit,
) {
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember(strings) { listOf(strings.appearance, strings.about) }
    var themeExpanded by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(40.dp),
                color = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        },
        bottomBar = {
            AppBottomNavBar(
                selectedItem = NavItem.Settings,
                strings = strings,
                onHomeClick = onHomeClick,
                onCalendarClick = onCalendarClick,
                onBibleClick = onBibleClick,
            ) {}
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(top = AdaptiveDimens.paddingMedium)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Custom Folder-style Tabs
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy((-20).dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val selected = selectedTabIndex == index
                            Box(
                                modifier = Modifier
                                    .zIndex(if (selected) 1f else 0f)
                                    .clip(
                                        GenericShape { size, _ ->
                                            val slantWidth = size.height * 0.7f // tan(35 degrees) approx 0.7

                                            moveTo(0f, size.height)
                                            // Left side: straight vertical
                                            lineTo(0f, 0f)
                                            // Top edge to the top-right corner
                                            lineTo(size.width - slantWidth, 0f)
                                            // Right side: 35-degree slant
                                            lineTo(size.width, size.height)
                                            close()
                                        },
                                    )
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                        },
                                    )
                                    .clickable { selectedTabIndex = index }
                                    .padding(start = 16.dp, end = 35.dp, top = 8.dp, bottom = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = title,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = AdaptiveDimens.smallFontSize,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                    // Bottom border that connects with the active tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }

                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))

                Box(modifier = Modifier.weight(1f).padding(horizontal = AdaptiveDimens.paddingMedium)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (selectedTabIndex) {
                            0 -> AppearanceSettings(
                                strings = strings,
                                currentTheme = currentTheme,
                                themeExpanded = themeExpanded,
                                onThemeExpandedChange = { themeExpanded = it },
                                onThemeSelected = { viewModel.setTheme(it) },
                                isPleasant = isPleasant,
                            )

                            1 -> AboutSettings(
                                strings = strings,
                                isPleasant = isPleasant,
                            )
                        }
                    }
                }

                Text(
                    text = strings.appTitle,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = AdaptiveDimens.paddingMedium),
                )
            }
        }
    }
}

@Composable
fun AppearanceSettings(
    strings: LocalizedStrings,
    currentTheme: AppTheme,
    themeExpanded: Boolean,
    onThemeExpandedChange: (Boolean) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    isPleasant: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
        ) {
            Text(
                text = strings.appearance,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Box {
                OutlinedButton(
                    onClick = { onThemeExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currentTheme.getDisplayName(strings),
                            fontSize = AdaptiveDimens.smallFontSize,
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { onThemeExpandedChange(false) },
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    theme.getDisplayName(strings),
                                    fontSize = AdaptiveDimens.smallFontSize,
                                )
                            },
                            onClick = {
                                onThemeSelected(theme)
                                onThemeExpandedChange(false)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSettings(
    strings: LocalizedStrings,
    isPleasant: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.developerNoteContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
