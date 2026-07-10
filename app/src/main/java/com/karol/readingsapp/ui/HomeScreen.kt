package com.karol.readingsapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.data.LanguageStatus
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import com.karol.readingsapp.ui.theme.AppTheme
import com.karol.readingsapp.ui.theme.glassEffect
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
) {
    val readingsGrouped by viewModel.uiState.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val selectedDate by viewModel.currentDate.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

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

    val currentTheme by viewModel.appTheme.collectAsState()
    val isGlass = currentTheme == AppTheme.LIQUID_FROSTED_GLASS

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AutoResizingText(
                        strings.appTitle,
                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = if (isGlass) Color.White else MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(if (isGlass) Color.DarkGray.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        strings.availableBibles,
                                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = AdaptiveDimens.smallFontSize,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBibleClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        strings.about,
                                        color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
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
                                        tint = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
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
                isGlass = isGlass,
            )
        },
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(horizontal = AdaptiveDimens.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    HomeHeader(
                        isDownloading = isDownloading,
                        selectedName = translations.find { it.code == selectedCode }?.name ?: strings.selectBible,
                        isToday = isToday,
                        strings = strings,
                        displayDate = displayDate,
                        translations = translations,
                        isGlass = isGlass,
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
                        isGlass = isGlass,
                    )
                    Spacer(modifier = Modifier.height(AdaptiveDimens.paddingSmall))
                }
            }
        }
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
    isGlass: Boolean = false,
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
            tint = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
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
                    color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = if (isGlass) Modifier.glassEffect() else Modifier,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = selectedName,
                            color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = AdaptiveDimens.smallFontSize,
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isGlass) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(if (isGlass) Color.DarkGray.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                ) {
                    translations.forEach { translation ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    translation.name,
                                    color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
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
            color = if (isGlass) Color.White else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = AdaptiveDimens.bodyFontSize,
        )
        AutoResizingText(
            text = displayDate,
            color = if (isGlass) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
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
    isGlass: Boolean = false,
) {
    val distinctReadings = remember(items) { items.distinctBy { "${it.bookId} ${it.chapter}" } }

    // Dynamic dimensions for maximum compactness
    val sectionPadding = AdaptiveDimens.paddingSmall
    val titleSize = AdaptiveDimens.smallFontSize
    val innerSpacer = 4.dp
    val itemSpacing = 4.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isGlass) Modifier.glassEffect() else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(sectionPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = (if (isGlass) Color.White else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                )
            }
            Spacer(modifier = Modifier.height(innerSpacer))

            if (items.isEmpty()) {
                Text(
                    noReadingsText,
                    color = (if (isGlass) Color.White else MaterialTheme.colorScheme.primary).copy(alpha = 0.5f),
                    fontSize = AdaptiveDimens.smallFontSize,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                distinctReadings.forEachIndexed { index, item ->
                    ReadingItemRow(
                        item = item,
                        strings = strings,
                        numberFormatter = numberFormatter,
                        isGlass = isGlass,
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
    isGlass: Boolean = false,
    onClick: () -> Unit,
) {
    val bookName = strings.bookNames[item.bookId] ?: item.bookName
    val text = "$bookName ${numberFormatter.format(item.chapter)}"

    val fontSize = AdaptiveDimens.smallFontSize
    val verticalPadding = 6.dp
    val horizontalPadding = 12.dp

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isGlass) Modifier.glassEffect(alpha = 0.3f) else Modifier),
        color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isGlass) 0.dp else 1.dp,
    ) {
        AutoResizingText(
            text = text,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            fontWeight = FontWeight.Bold,
            color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            maxLines = 1,
        )
    }
}
