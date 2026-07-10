package com.karol.readingsapp.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.karol.readingsapp.ui.components.AboutContent
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import com.karol.readingsapp.ui.theme.AppTheme
import com.karol.readingsapp.ui.theme.glassEffect

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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember(strings) { listOf(strings.appearance, strings.about, strings.contact) }
    var themeExpanded by remember { mutableStateOf(value = false) }

    val isGlass = currentTheme == AppTheme.DARK_FROSTED_GLASS

    Scaffold(
        topBar = {
            SettingsTopBar(
                strings = strings,
                onHomeClick = onHomeClick,
                isGlass = isGlass,
            )
        },
        bottomBar = {
            AppBottomNavBar(
                selectedItem = NavItem.Settings,
                strings = strings,
                onHomeClick = onHomeClick,
                onCalendarClick = onCalendarClick,
                onBibleClick = onBibleClick,
                onSettingsClick = {},
                isGlass = isGlass,
            )
        },
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
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
                SettingsTabs(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    isGlass = isGlass,
                ) { selectedTabIndex = it }

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
                                isGlass = isGlass,
                            ) { viewModel.setTheme(it) }

                            1 -> AboutSettings(
                                strings = strings,
                                isGlass = isGlass,
                            )

                            2 -> ContactSettings(
                                strings = strings,
                                isGlass = isGlass,
                            )
                        }
                    }
                }

                if (selectedTabIndex != 0) {
                    SettingsFooter(strings = strings, isGlass = isGlass)
                }
            }
        }
    }
}

@Composable
fun SettingsTopBar(
    strings: LocalizedStrings,
    onHomeClick: () -> Unit,
    isGlass: Boolean = false,
) {
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
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
            )
        }
    }
}

@Composable
fun SettingsTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    isGlass: Boolean = false,
    onTabSelected: (Int) -> Unit,
) {
    val r = with(LocalDensity.current) { 10.dp.toPx() }
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
                                // Left side: straight vertical, rounded at top
                                lineTo(0f, r)
                                quadraticTo(0f, 0f, r, 0f)

                                // Top edge to the top-right corner, rounded
                                lineTo(size.width - slantWidth - r, 0f)
                                // Slanted corner rounding approximation
                                quadraticTo(
                                    size.width - slantWidth,
                                    0f,
                                    (size.width - slantWidth) + (r * 0.57f),
                                    r * 0.82f,
                                )

                                // Right side: 35-degree slant
                                lineTo(size.width, size.height)
                                close()
                            },
                        )
                        .background(
                            if (selected) {
                                if (isGlass) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isGlass) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            },
                        )
                        .then(
                            if (isGlass && selected) {
                                Modifier.glassEffect(alpha = 0.4f)
                            } else if (isGlass) {
                                Modifier.glassEffect(alpha = 0.1f)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onTabSelected(index) }
                        .padding(start = 16.dp, end = 35.dp, top = 8.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        color = if (selected) {
                            if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimary
                        } else {
                            if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
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
                .background(if (isGlass) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
fun AppearanceSettings(
    strings: LocalizedStrings,
    currentTheme: AppTheme,
    themeExpanded: Boolean,
    onThemeExpandedChange: (Boolean) -> Unit,
    isGlass: Boolean = false,
    onThemeSelected: (AppTheme) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isGlass) Modifier.glassEffect() else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isGlass) 0.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
        ) {
            Text(
                text = strings.appearance,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Box {
                OutlinedButton(
                    onClick = { onThemeExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                    ),
                    border = if (isGlass) {
                        ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(0.6f),
                                    Color.White.copy(0.2f),
                                ),
                            ),
                        )
                    } else {
                        ButtonDefaults.outlinedButtonBorder()
                    },
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
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(if (isGlass) Color.DarkGray.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                ) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    theme.getDisplayName(strings),
                                    fontSize = AdaptiveDimens.smallFontSize,
                                    color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
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
    isGlass: Boolean = false,
) {
    if (isGlass) {
        Box(modifier = Modifier.glassEffect()) {
            AboutContent(strings = strings, isGlass = true)
        }
    } else {
        AboutContent(strings = strings)
    }
}

@Composable
fun ContactSettings(
    strings: LocalizedStrings,
    isGlass: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdaptiveDimens.paddingMedium)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isGlass) Modifier.glassEffect() else Modifier),
            colors = CardDefaults.cardColors(
                containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(if (isGlass) 0.dp else 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = strings.contact,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "We’d love to hear from you! Send your questions, suggestions, or feedback to justkarol@icloud.com",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = AdaptiveDimens.smallFontSize,
                        color = if (isGlass) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Start,
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isGlass) Modifier.glassEffect() else Modifier),
            colors = CardDefaults.cardColors(
                containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(if (isGlass) 0.dp else 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = strings.theTeam,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )

                ScrollingCredits(isGlass = isGlass)
            }
        }
    }
}

@Composable
private fun SettingsFooter(strings: LocalizedStrings, isGlass: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AdaptiveDimens.paddingMedium),
    ) {
        Text(
            text = strings.appTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = (if (isGlass) Color.White else MaterialTheme.colorScheme.primary).copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold,
            fontSize = AdaptiveDimens.smallFontSize,
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.developedBy,
            style = MaterialTheme.typography.bodySmall,
            color = (if (isGlass) Color.White else MaterialTheme.colorScheme.primary).copy(alpha = 0.5f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScrollingCredits(isGlass: Boolean) {
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
                        color = if (isGlass) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
