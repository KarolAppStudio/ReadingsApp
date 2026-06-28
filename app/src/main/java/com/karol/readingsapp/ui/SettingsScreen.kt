package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    onAboutClick: () -> Unit,
) {
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()

    var themeExpanded by remember { mutableStateOf(value = false) }

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

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
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(AdaptiveDimens.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
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
                                onClick = { themeExpanded = true },
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
                                onDismissRequest = { themeExpanded = false },
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
                                            viewModel.setTheme(theme)
                                            themeExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingSmall))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAboutClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AdaptiveDimens.paddingMedium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.about,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = AdaptiveDimens.bodyFontSize,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingSmall))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
                    ) {
                        Text(
                            text = strings.developerNoteTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = AdaptiveDimens.bodyFontSize,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        Text(
                            text = strings.developerNoteContent,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                fontSize = AdaptiveDimens.smallFontSize,
                                lineHeight = AdaptiveDimens.smallFontSize * 1.4f,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingLarge))

                Text(
                    text = strings.appTitle,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
