package com.karol.readingsapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.GlassBorder
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
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val isDownloading = downloadStatus[selectedLanguage] == com.karol.readingsapp.data.LanguageStatus.DOWNLOADING

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
                title = {
                    AutoResizingText(
                        strings.appTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
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
                                    Text(
                                        strings.settings,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSettingsClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        strings.about,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
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
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                val selectedName = translations.find { it.code == selectedCode }?.name ?: strings.selectBible
                var expanded by remember { mutableStateOf(value = false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp),
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
                                    Text(
                                        text = selectedName,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
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
                                            Text(
                                                translation.name,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        },
                                        onClick = {
                                            viewModel.setTranslation(translation.code)
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    AutoResizingText(
                        text = if (isToday) strings.todaysReadings else strings.selectedReadings,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    AutoResizingText(
                        text = displayDate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
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
    val sectionPadding = 8.dp
    val titleSize = 12.sp
    val innerSpacer = 4.dp
    val itemSpacing = 4.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(sectionPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                )
            }
            Spacer(modifier = Modifier.height(innerSpacer))

            if (items.isEmpty()) {
                Text(
                    noReadingsText,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontSize = 13.sp,
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

    val fontSize = 14.sp
    val verticalPadding = 6.dp
    val horizontalPadding = 12.dp

    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isPleasant) 0.dp else 1.dp,
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
