package com.karol.readingsapp.feature.settings.ui

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.theme.AppTheme
import com.karol.readingsapp.core.ui.components.AboutContent
import com.karol.readingsapp.core.ui.components.AppBottomNavBar
import com.karol.readingsapp.core.ui.components.AutoResizingText
import com.karol.readingsapp.core.ui.components.NavItem
import com.karol.readingsapp.feature.bible.data.LanguageStatus
import com.karol.readingsapp.feature.bible.data.TranslationEntity
import com.karol.readingsapp.feature.shared.ui.AppUpdateStatus
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
    initialTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
) {
    val translations by viewModel.availableTranslations.collectAsStateWithLifecycle()
    val remoteTranslations by viewModel.remoteTranslations.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val individualProgress by viewModel.individualProgress.collectAsStateWithLifecycle()
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val strings by viewModel.strings.collectAsStateWithLifecycle()

    LaunchedEffect(updateStatus) {
        when (val status = updateStatus) {
            is AppUpdateStatus.NewVersionAvailable -> {
                snackbarHostState.showSnackbar("New version ${status.version} found. Downloading...")
            }

            is AppUpdateStatus.UpToDate -> {
                snackbarHostState.showSnackbar("App is up to date.")
                viewModel.clearUpdateStatus()
            }

            is AppUpdateStatus.Error -> {
                snackbarHostState.showSnackbar("Update check failed: ${status.message}")
                viewModel.clearUpdateStatus()
            }

            else -> {}
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = remember(strings) { listOf(strings.settings, strings.downloads, strings.about) }
    var themeExpanded by remember { mutableStateOf(value = false) }

    val sortedTranslations = remember(translations, remoteTranslations, downloadStatus) {
        val baseList = remoteTranslations.ifEmpty { translations }
        baseList.sortedWith(
            compareByDescending<TranslationEntity> { translation ->
                (translation.code.uppercase() == "ENG") ||
                    translation.language.equals("English", ignoreCase = true) ||
                    (downloadStatus[translation.language] == LanguageStatus.DOWNLOADED)
            }.thenBy { it.language },
        )
    }

    LaunchedEffect(selectedTabIndex) {
        onTabSelected(selectedTabIndex)
    }

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
            ) {}
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                                Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))
                                UpdateSettings(
                                    strings = strings,
                                    isRefreshing = isRefreshing,
                                ) { viewModel.checkForAppUpdate() }
                            }

                            1 -> DownloadSettings(
                                strings = strings,
                                translations = sortedTranslations,
                                downloadStatus = downloadStatus,
                                individualProgress = individualProgress,
                                isRefreshing = isRefreshing,
                                onDownloadClick = { language, code ->
                                    viewModel.startBatchDownload(listOf(language), listOf(code))
                                },
                                onRemoveClick = { language, code -> viewModel.removeTranslation(language, code) },
                            ) { viewModel.refreshRemoteTranslations(updateDb = true) }

                            2 -> AboutSettings(
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
fun SettingsTopBar(strings: LocalizedStrings, onHomeClick: () -> Unit) {
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
fun SettingsTabs(tabs: List<String>, selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
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
                    AutoResizingText(
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
                    repeatMode = RepeatMode.Restart,
                ),
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AutoResizingText(
                    text = strings.availableBibles,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = AdaptiveDimens.bodyFontSize,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotation.value),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AutoResizingText(
                            text = strings.refresh,
                            fontSize = (12 * AdaptiveDimens.fontScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            translations.forEach { translation ->
                val status = downloadStatus[translation.language] ?: LanguageStatus.FAILED // Fallback or default
                val progress = individualProgress[translation.language] ?: 0f

                // English is pre-installed and marked as DOWNLOADED in LanguageService init.
                // We keep a hardcoded check here as a safety measure.
                val isCore = (translation.code.uppercase() == "ENG") ||
                    translation.language.equals("English", ignoreCase = true)

                val effectiveStatus = if (isCore) {
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
                        AutoResizingText(
                            text = translation.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = AdaptiveDimens.smallFontSize,
                            fontWeight = FontWeight.SemiBold,
                        )
                        AutoResizingText(
                            text = translation.language,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = (12 * AdaptiveDimens.fontScale).sp,
                        )
                    }

                    if (effectiveStatus == LanguageStatus.DOWNLOADING) {
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = (10 * AdaptiveDimens.fontScale).sp,
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = (10 * AdaptiveDimens.fontScale).sp,
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
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                when (effectiveStatus) {
                                    LanguageStatus.DOWNLOADED -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.wrapContentWidth(),
                                        ) {
                                            AutoResizingText(
                                                text = strings.installed,
                                                color = Color(0xFF2E7D32), // Dark Green
                                                fontSize = (12 * AdaptiveDimens.fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp),
                                                textAlign = TextAlign.End,
                                            )

                                            if (!isCore) {
                                                Button(
                                                    onClick = {
                                                        onRemoveClick(
                                                            translation.language,
                                                            translation.code,
                                                        )
                                                    },
                                                    contentPadding = PaddingValues(
                                                        horizontal = 8.dp,
                                                        vertical = 0.dp,
                                                    ),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    ),
                                                ) {
                                                    AutoResizingText(
                                                        text = "Remove",
                                                        fontSize = (10 * AdaptiveDimens.fontScale).sp,
                                                        fontWeight = FontWeight.Bold,
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
                                                    translation.code,
                                                )
                                            },
                                            contentPadding = PaddingValues(
                                                horizontal = 8.dp,
                                                vertical = 4.dp,
                                            ),
                                            modifier = Modifier
                                                .height(32.dp),
                                        ) {
                                            AutoResizingText(
                                                text = strings.download,
                                                fontSize = (12 * AdaptiveDimens.fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.End,
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
                        color = MaterialTheme.colorScheme.outlineVariant,
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
            AutoResizingText(
                text = strings.theme,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = AdaptiveDimens.bodyFontSize,
                fontWeight = FontWeight.Bold,
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
                        AutoResizingText(
                            text = currentTheme.getDisplayName(strings),
                            fontSize = AdaptiveDimens.smallFontSize,
                            modifier = Modifier.weight(1f),
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
                                AutoResizingText(
                                    text = theme.getDisplayName(strings),
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
fun AboutSettings(strings: LocalizedStrings) {
    AboutContent(strings = strings)
}

@Composable
private fun SettingsFooter(strings: LocalizedStrings) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AdaptiveDimens.paddingMedium),
    ) {
        AutoResizingText(
            text = strings.appTitle,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            fontSize = AdaptiveDimens.smallFontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        AutoResizingText(
            text = strings.developedBy,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            fontSize = (10 * AdaptiveDimens.fontScale).sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun UpdateSettings(strings: LocalizedStrings, isRefreshing: Boolean, onCheckForUpdates: () -> Unit) {
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
            AutoResizingText(
                text = strings.updateApplication,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = AdaptiveDimens.bodyFontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Button(
                onClick = onCheckForUpdates,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshing,
                shape = RoundedCornerShape(8.dp),
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = strings.checkForUpdates,
                    fontSize = AdaptiveDimens.smallFontSize,
                )
            }
        }
    }
}

@Composable
fun VoiceSettings(voiceViewModel: VoiceViewModel) {
    val allVoices by voiceViewModel.filteredVoices.collectAsStateWithLifecycle()
    val selectedVoice by voiceViewModel.selectedVoice.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(value = false) }

    var persistentVoiceName by remember { mutableStateOf("Default") }

    val selectedVoiceName = remember(selectedVoice, allVoices) {
        if (allVoices.isEmpty()) {
            "No Voice Available"
        } else {
            selectedVoice?.let { voice ->
                val lang = voice.locale.displayLanguage
                val genderLabel = when (voice.gender) {
                    VoiceGender.MALE -> "Male"
                    VoiceGender.FEMALE -> "Female"
                    VoiceGender.UNKNOWN -> "Voice"
                }
                // Find index within same gender/lang in available voices for consistent display
                val voicesInLang = allVoices.filter { it.locale.language == voice.locale.language }
                val sameGenderVoices = voicesInLang.filter { it.gender == voice.gender }
                val index = sameGenderVoices.indexOfFirst { it.name == voice.name } + 1
                val indexLabel = if ((sameGenderVoices.size > 1) && (index > 0)) " $index" else ""
                val offlineLabel = if (voice.isOffline) " [Offline]" else ""

                val name = "$lang ($genderLabel$indexLabel$offlineLabel)"
                persistentVoiceName = name
                name
            } ?: persistentVoiceName
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
            AutoResizingText(
                text = "Voice Selection",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = AdaptiveDimens.bodyFontSize,
                fontWeight = FontWeight.Bold,
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
                        AutoResizingText(
                            text = selectedVoiceName,
                            fontSize = AdaptiveDimens.smallFontSize,
                            modifier = Modifier.weight(1f, fill = false),
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
                    if (allVoices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No Voice Available", fontSize = AdaptiveDimens.smallFontSize) },
                            onClick = { expanded = false },
                        )
                    } else {
                        val grouped = allVoices.groupBy { it.locale.displayLanguage }
                        grouped.forEach { (lang, voices) ->
                            DropdownMenuItem(
                                text = {
                                    AutoResizingText(
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

                                val offlineLabel = if (voice.isOffline) " [Offline]" else ""
                                val displayName = "  $genderLabel$indexLabel$offlineLabel"
                                DropdownMenuItem(
                                    text = {
                                        AutoResizingText(
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

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { voiceViewModel.voiceService.checkAndInstallVoices() },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp),
            ) {
                AutoResizingText(
                    text = "Download more voices...",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
