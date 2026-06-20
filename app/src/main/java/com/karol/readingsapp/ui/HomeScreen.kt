package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.TargetReadingDetails
import com.karol.readingsapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val isDownloading = downloadStatus[selectedLanguage] == com.karol.readingsapp.data.LanguageStatus.DOWNLOADING
    
    val today = remember { LocalDate.now() }
    val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    
    val displayDate = remember(selectedDate, strings) {
        try {
            val dateToParse = selectedDate.ifEmpty { todayString }
            LocalDate.parse(dateToParse).format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", strings.locale))
        } catch (_: Exception) {
            today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", strings.locale))
        }
    }

    var menuExpanded by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        if (selectedDate.isEmpty()) {
            viewModel.loadReading(todayString)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.appTitle,
                        color = TextBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextBlue)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.about) },
                                onClick = {
                                    menuExpanded = false
                                    onAboutClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = TextBlue,
                                    )
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlue,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                    label = { Text(strings.home) },
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
                    selected = false,
                    onClick = onBibleClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = TextBlue,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if ((selectedDate == todayString) || (selectedDate.isEmpty())) strings.todaysReadings else strings.selectedReadings,
                            color = TextBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Text(
                            displayDate,
                            color = DateGrey,
                            fontSize = 14.sp,
                        )
                    }

                    val selectedName = translations.find { it.code == selectedCode }?.name ?: "Select Bible"
                    var expanded by remember { mutableStateOf(value = false) }

                    Box {
                        Surface(
                            onClick = { expanded = true },
                            color = CardLavender,
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
                                        color = TextBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = selectedName,
                                    color = TextBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextBlue,
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            translations.forEach { translation ->
                                DropdownMenuItem(
                                    text = { Text(translation.name) },
                                    onClick = {
                                        viewModel.setTranslation(translation.code)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Dynamically show sections based on available data, or default if empty
            val sectionsToShow = if (readingsGrouped.isEmpty()) {
                listOf("First Reading", "Second Reading", "Third Reading")
            } else {
                readingsGrouped.keys.sortedBy { it }
            }

            items(sectionsToShow) { type ->
                val localizedTitle = when(type) {
                    "First Reading" -> strings.firstReading
                    "Second Reading" -> strings.secondReading
                    "Third Reading" -> strings.thirdReading
                    else -> type
                }
                ReadingSection(
                    title = localizedTitle,
                    items = readingsGrouped[type] ?: emptyList(),
                    strings = strings,
                    noReadingsText = strings.noReadings,
                    onItemClick = onReadingClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReadingSection(
    title: String,
    items: List<TargetReadingDetails>,
    strings: LocalizedStrings,
    noReadingsText: String,
    onItemClick: (TargetReadingDetails) -> Unit,
) {
    val distinctReadings = remember(items) { items.distinctBy { "${it.bookId} ${it.chapter}" } }
    val itemCount = distinctReadings.size
    
    // Dynamic dimensions based on item count
    val sectionPadding = if (itemCount > 2) 12.dp else 16.dp
    val titleSize = if (itemCount > 2) 12.sp else 14.sp
    val innerSpacer = if (itemCount > 2) 8.dp else 12.dp
    val itemSpacing = if (itemCount > 2) 6.dp else 8.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLavender),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(sectionPadding),
        ) {
            Text(
                title,
                color = TextBlue.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
            )
            Spacer(modifier = Modifier.height(innerSpacer))
            
            if (items.isEmpty()) {
                Text(
                    noReadingsText,
                    color = TextBlue.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                distinctReadings.forEachIndexed { index, item ->
                    ReadingItemRow(
                        item = item, 
                        strings = strings,
                        dense = itemCount > 2,
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
    dense: Boolean,
    onClick: () -> Unit
) {
    val bookName = strings.bookNames[item.bookId] ?: item.bookName
    val text = "$bookName ${item.chapter}"
    val isLong = text.length > 20
    
    // Scaling font and padding based on density and text length
    val fontSize = when {
        dense && isLong -> 13.sp
        dense || isLong -> 14.sp
        else -> 16.sp
    }
    
    val verticalPadding = when {
        dense && isLong -> 10.dp
        dense -> 12.dp
        else -> 16.dp
    }

    val horizontalPadding = if (isLong) 12.dp else 16.dp

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = fontSize,
            maxLines = 1
        )
    }
}
