package com.karol.readingsapp.feature.settings.ui

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karol.readingsapp.core.i18n.Localization
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.theme.AppTheme
import com.karol.readingsapp.core.ui.components.AboutContent
import com.karol.readingsapp.core.ui.components.AppBottomNavBar
import com.karol.readingsapp.core.ui.components.NavItem
import com.karol.readingsapp.feature.bible.data.LanguageStatus
import com.karol.readingsapp.feature.bible.data.TranslationEntity
import com.karol.readingsapp.feature.shared.ui.ReadingViewModel
import com.karol.readingsapp.feature.voice.data.VoiceGender
import com.karol.readingsapp.feature.voice.ui.VoiceViewModel

@Composable
fun SettingsScreen(
    viewModel: ReadingViewModel,
    voiceViewModel: VoiceViewModel,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onBibleClick: () -> Unit,
) {
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val remoteTranslations by viewModel.remoteTranslations.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val individualProgress by viewModel.individualProgress.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember(strings) { listOf(strings.settings, strings.download, strings.about, strings.contact) }
    var themeExpanded by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            SettingsTopBar(
                strings = strings,
                onHomeClick = onHomeClick,
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
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
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
                ) { selectedTabIndex = it }

                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))

                Box(modifier = Modifier.weight(1f).padding(horizontal = AdaptiveDimens.paddingMedium)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (selectedTabIndex) {
                            0 -> {
                                AppearanceSettings(
                                    strings = strings,
                                    currentTheme = currentTheme,
                                    themeExpanded = themeExpanded,
                                    onThemeExpandedChange = { themeExpanded = it },
                                ) { viewModel.setTheme(it) }
                                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))
                                VoiceSettings(
                                    voiceViewModel = voiceViewModel,
                                )
                            }

                            1 -> DownloadSettings(
                                strings = strings,
                                translations = if (remoteTranslations.isNotEmpty()) remoteTranslations else translations,
                                downloadStatus = downloadStatus,
                                individualProgress = individualProgress,
                                isRefreshing = isRefreshing,
                                onDownloadClick = { language, code -> viewModel.startBatchDownload(listOf(language), listOf(code)) },
                                onRemoveClick = { language, code -> viewModel.removeTranslation(language, code) },
                                onRefreshClick = { viewModel.refreshRemoteTranslations(updateDb = true) }
                            )

                            2 -> AboutSettings(
                                strings = strings,
                            )

                            3 -> ContactSettings(
                                strings = strings,
                            )
                        }
                    }
                }

                if (selectedTabIndex > 1) {
                    SettingsFooter(strings = strings)
                }
            }
        }
    }
}

@Composable
fun SettingsTopBar(
    strings: LocalizedStrings,
    onHomeClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp),
        color = MaterialTheme.colorScheme.background,
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
                    modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 40.dp else 30.dp),
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
}

@Composable
fun SettingsTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val r = with(LocalDensity.current) { 10.dp.toPx() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
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
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            },
                        )
                        .clickable { onTabSelected(index) }
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
}

@Composable
fun DownloadSettings(
    strings: LocalizedStrings,
    translations: List<TranslationEntity>,
    downloadStatus: Map<String, LanguageStatus>,
    individualProgress: Map<String, Float>,
    isRefreshing: Boolean,
    onDownloadClick: (String, String) -> Unit,
    onRemoveClick: (String, String) -> Unit,
    onRefreshClick: () -> Unit,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.stop()
        }
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.availableBibles,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                TextButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotation.value)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.refresh,
                            fontSize = (12 * AdaptiveDimens.fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            translations.forEach { translation ->
                val status = downloadStatus[translation.language] ?: LanguageStatus.FAILED // Fallback or default
                val progress = individualProgress[translation.language] ?: 0f

                // English and Malayalam are pre-installed and marked as DOWNLOADED in LanguageService init.
                // We keep a hardcoded check here as a safety measure.
                val effectiveStatus = if (translation.language == "English" || translation.language == "Malayalam") {
                    LanguageStatus.DOWNLOADED
                } else {
                    status
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = translation.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = AdaptiveDimens.smallFontSize,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Text(
                            text = translation.language,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (12 * AdaptiveDimens.fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }

                    if (effectiveStatus == LanguageStatus.DOWNLOADING) {
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                            val statusText = when {
                                progress < 0.5f -> "Downloading..."
                                progress < 0.9f -> "Installing..."
                                else -> "Finalizing..."
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = (10 * AdaptiveDimens.fontScale).sp
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = (10 * AdaptiveDimens.fontScale).sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = translation.code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )

                        if (effectiveStatus != LanguageStatus.DOWNLOADING) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier.widthIn(min = 100.dp, max = 160.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                when (effectiveStatus) {
                                    LanguageStatus.DOWNLOADED -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.wrapContentWidth()
                                        ) {
                                            Text(
                                                text = strings.installed,
                                                color = Color(0xFF2E7D32), // Dark Green
                                                fontSize = (12 * AdaptiveDimens.fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp),
                                                textAlign = TextAlign.End
                                            )

                                            if (translation.code != "ENG" && translation.code != "MAL") {
                                                Button(
                                                    onClick = {
                                                        onRemoveClick(
                                                            translation.language,
                                                            translation.code
                                                        )
                                                    },
                                                    contentPadding = PaddingValues(
                                                        horizontal = 8.dp,
                                                        vertical = 0.dp
                                                    ),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                ) {
                                                    Text(
                                                        text = "Remove",
                                                        fontSize = (10 * AdaptiveDimens.fontScale).sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        TextButton(
                                            onClick = {
                                                onDownloadClick(
                                                    translation.language,
                                                    translation.code
                                                )
                                            },
                                            contentPadding = PaddingValues(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            modifier = Modifier
                                                .height(32.dp)
                                        ) {
                                            Text(
                                                text = strings.download,
                                                fontSize = (12 * AdaptiveDimens.fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (translation != translations.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
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
) {
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
            Text(
                text = strings.theme,
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
                    border = ButtonDefaults.outlinedButtonBorder(),
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
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                ) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    theme.getDisplayName(strings),
                                    fontSize = AdaptiveDimens.smallFontSize,
                                    color = MaterialTheme.colorScheme.onSurface,
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
) {
    AboutContent(strings = strings)
}

@Composable
fun ContactSettings(
    strings: LocalizedStrings,
) {
    var showFeedbackDialog by remember { mutableStateOf(value = false) }
    var showMessageSentPopup by remember { mutableStateOf(value = false) }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
        ) {
            showFeedbackDialog = false
            showMessageSentPopup = true
        }
    }

    if (showMessageSentPopup) {
        AlertDialog(
            onDismissRequest = { showMessageSentPopup = false },
            confirmButton = {
                TextButton(onClick = { showMessageSentPopup = false }) {
                    Text("OK")
                }
            },
            title = { Text("Success") },
            text = { Text("Message Sent") },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(AdaptiveDimens.paddingMedium)) {
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
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = strings.contact,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "We’d love to hear from you! Send us your questions, suggestions, or feedback.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = AdaptiveDimens.smallFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showFeedbackDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Click Here")
                }
            }
        }

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
                Text(
                    text = strings.theTeam,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )

                ScrollingCredits()
            }
        }
    }
}

@Composable
private fun SettingsFooter(strings: LocalizedStrings) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AdaptiveDimens.paddingMedium),
    ) {
        Text(
            text = strings.appTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold,
            fontSize = AdaptiveDimens.smallFontSize,
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.developedBy,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            fontSize = (10 * AdaptiveDimens.fontScale).sp,
            textAlign = TextAlign.Center,
        )
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

@Composable
fun VoiceSettings(
    voiceViewModel: VoiceViewModel,
) {
    val availableVoices by voiceViewModel.filteredVoices.collectAsStateWithLifecycle()
    val selectedVoice by voiceViewModel.selectedVoice.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(value = false) }

    val selectedVoiceName = selectedVoice?.let { voice ->
        val genderLabel = when (voice.gender) {
            VoiceGender.MALE -> "Male"
            VoiceGender.FEMALE -> "Female"
            VoiceGender.UNKNOWN -> "Voice"
        }
        "${voice.locale.displayLanguage} ($genderLabel)"
    } ?: "Default"

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
            Text(
                text = "Voice Selection",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedVoiceName,
                            fontSize = AdaptiveDimens.smallFontSize,
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                ) {
                    if (availableVoices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No voices available", fontSize = AdaptiveDimens.smallFontSize) },
                            onClick = { expanded = false },
                        )
                    } else {
                        val grouped = availableVoices.groupBy { it.locale.displayLanguage }
                        grouped.forEach { (lang, voices) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        lang,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = AdaptiveDimens.smallFontSize,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                onClick = {},
                                enabled = false,
                            )
                            voices.forEach { voice ->
                                val genderLabel = when (voice.gender) {
                                    VoiceGender.MALE -> "Male"
                                    VoiceGender.FEMALE -> "Female"
                                    VoiceGender.UNKNOWN -> "Voice"
                                }
                                // Find index within same gender/lang to distinguish
                                val sameGenderVoices = voices.filter { it.gender == voice.gender }
                                val index = sameGenderVoices.indexOf(voice) + 1
                                val indexLabel = if (sameGenderVoices.size > 1) " $index" else ""

                                val displayName = "  $genderLabel$indexLabel${if (voice.isOffline) " [Offline]" else ""}"
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayName,
                                            fontSize = AdaptiveDimens.smallFontSize,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    },
                                    onClick = {
                                        voiceViewModel.onVoiceSelected(voice)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSent: () -> Unit,
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val subjects = listOf("Feedback", "Suggestion", "Report an Issue", "Request a feature")
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select Subject") },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true },
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.6f),
                    ) {
                        subjects.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    subject = item
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                if (subject.isNotEmpty()) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("Type your message here...") },
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:justkarol@icloud.com".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                                onSent()
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.ui.graphics.RectangleShape,
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
