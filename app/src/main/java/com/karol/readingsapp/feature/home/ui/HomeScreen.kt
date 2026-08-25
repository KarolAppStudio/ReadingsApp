package com.karol.readingsapp.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.theme.AppTheme
import com.karol.readingsapp.core.theme.eInkBorder
import com.karol.readingsapp.core.ui.components.AppBottomNavBar
import com.karol.readingsapp.core.ui.components.AutoResizingText
import com.karol.readingsapp.core.ui.components.NavItem
import com.karol.readingsapp.feature.bible.data.LanguageStatus
import com.karol.readingsapp.feature.bible.data.TargetReadingDetails
import com.karol.readingsapp.feature.bible.data.TranslationEntity
import com.karol.readingsapp.feature.shared.ui.ReadingViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReadingViewModel,
    onCalendarClick: () -> Unit,
    onBibleClick: () -> Unit,
    onReadingClick: (TargetReadingDetails) -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onContactClick: () -> Unit,
    onParallelClick: () -> Unit,
) {
    val readingsGrouped by viewModel.uiState.collectAsState()
    val downloadedTranslations by viewModel.downloadedTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val selectedDate by viewModel.currentDate.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val strings by viewModel.strings.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val isDownloading = downloadStatus[selectedLanguage] == LanguageStatus.DOWNLOADING

    val today = remember { LocalDate.now() }
    val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val isToday = (selectedDate == todayString) || (selectedDate.isEmpty())

    val displayDate = remember(selectedDate, strings) {
        try {
            val dateToParse = selectedDate.ifEmpty { todayString }
            LocalDate.parse(dateToParse).format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", strings.locale))
        } catch (_: Exception) {
            today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", strings.locale))
        }
    }

    var menuExpanded by remember { mutableStateOf(value = false) }
    val numberFormatter = remember(strings.locale) {
        NumberFormat.getIntegerInstance(strings.locale)
    }

    LaunchedEffect(Unit) {
        if (selectedDate.isEmpty()) {
            viewModel.loadReading(todayString)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    AutoResizingText(
                        strings.appTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    AutoResizingText(
                                        text = strings.about,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = AdaptiveDimens.smallFontSize,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onAboutClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    AutoResizingText(
                                        text = strings.contact,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = AdaptiveDimens.smallFontSize,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onContactClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            AppBottomNavBar(
                selectedItem = NavItem.Home,
                strings = strings,
                onHomeClick = { },
                onCalendarClick = onCalendarClick,
                onBibleClick = onBibleClick,
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(horizontal = AdaptiveDimens.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    HomeHeader(
                        isDownloading = isDownloading,
                        selectedName =
                        downloadedTranslations.find { it.code == selectedCode }?.name ?: strings.selectBible,
                        isToday = isToday,
                        strings = strings,
                        displayDate = displayDate,
                        translations = downloadedTranslations,
                    ) { viewModel.setTranslation(it) }
                }

                // Dynamically show sections based on available data, or default if empty
                val sectionsToShow = if (readingsGrouped.isEmpty()) {
                    listOf("First Reading", "Second Reading", "Third Reading")
                } else {
                    readingsGrouped.keys.sortedBy { it }
                }

                items(sectionsToShow, key = { it }) { type ->
                    val localizedTitle = when (type) {
                        "First Reading" -> strings.firstReading
                        "Second Reading" -> strings.secondReading
                        "Third Reading" -> strings.thirdReading
                        else -> type
                    }
                    ReadingSection(
                        title = localizedTitle,
                        items = readingsGrouped[type] ?: emptyList(),
                        strings = strings,
                        numberFormatter = numberFormatter,
                        noReadingsText = strings.noReadings,
                        onItemClick = onReadingClick,
                    )
                    Spacer(modifier = Modifier.height(AdaptiveDimens.paddingSmall))
                }
            }

            var cardHeight by remember { mutableIntStateOf(0) }
            val isEInk = appTheme == AppTheme.E_INK
            Card(
                modifier = Modifier
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(horizontal = AdaptiveDimens.paddingMedium)
                    .padding(bottom = AdaptiveDimens.paddingSmall)
                    .height((68 * AdaptiveDimens.fontScale).dp)
                    .onGloballyPositioned { coordinates ->
                        cardHeight = coordinates.size.height
                    }
                    .then(if (isEInk) Modifier.eInkBorder() else Modifier),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AutoResizingText(
                        text = strings.shortcut,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.smallFontSize,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShortcutItem(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            label = strings.bibleReader,
                            onClick = onBibleClick,
                        )
                        ShortcutItem(
                            icon = Icons.Default.AutoStories,
                            label = strings.parallelBible,
                            onClick = onParallelClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShortcutItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        AutoResizingText(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun HomeHeader(
    isDownloading: Boolean,
    selectedName: String,
    isToday: Boolean,
    strings: LocalizedStrings,
    displayDate: String,
    translations: List<TranslationEntity>,
    onTranslationSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AdaptiveDimens.paddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 40.dp else 30.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                Surface(
                    onClick = { expanded = true },
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        AutoResizingText(
                            text = selectedName,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = AdaptiveDimens.smallFontSize,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                ) {
                    translations.forEach { translation ->
                        DropdownMenuItem(
                            text = {
                                AutoResizingText(
                                    text = translation.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = AdaptiveDimens.smallFontSize,
                                    fontWeight = FontWeight.Normal,
                                )
                            },
                            onClick = {
                                onTranslationSelected(translation.code)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(AdaptiveDimens.paddingSmall))

    Column(modifier = Modifier.fillMaxWidth()) {
        AutoResizingText(
            text = if (isToday) strings.todaysReadings else strings.selectedReadings,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = AdaptiveDimens.bodyFontSize,
        )
        AutoResizingText(
            text = displayDate,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = AdaptiveDimens.smallFontSize,
        )
    }
    Spacer(modifier = Modifier.height(AdaptiveDimens.paddingMedium))
}

@Composable
fun ReadingSection(
    title: String,
    items: List<TargetReadingDetails>,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    noReadingsText: String,
    onItemClick: (TargetReadingDetails) -> Unit,
) {
    val distinctReadings = remember(items) { items.distinctBy { "${it.bookId} ${it.chapter}" } }

    // Dynamic dimensions for maximum compactness
    val sectionPadding = AdaptiveDimens.paddingSmall
    val titleSize = AdaptiveDimens.smallFontSize
    val innerSpacer = 4.dp
    val itemSpacing = 4.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(sectionPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AutoResizingText(
                    title,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                )
            }
            Spacer(modifier = Modifier.height(innerSpacer))

            if (items.isEmpty()) {
                AutoResizingText(
                    noReadingsText,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                distinctReadings.forEachIndexed { index, item ->
                    ReadingItemRow(
                        item = item,
                        strings = strings,
                        numberFormatter = numberFormatter,
                    ) { onItemClick(item) }
                    if (index < (distinctReadings.size - 1)) {
                        Spacer(modifier = Modifier.height(itemSpacing))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingItemRow(
    item: TargetReadingDetails,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    onClick: () -> Unit,
) {
    val bookName = strings.bookNames[item.bookId] ?: item.bookName
    val text = "$bookName ${numberFormatter.format(item.chapter)}"

    val fontSize = AdaptiveDimens.smallFontSize
    val verticalPadding = 6.dp
    val horizontalPadding = 12.dp

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
    ) {
        AutoResizingText(
            text = text,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            maxLines = 1,
        )
    }
}
