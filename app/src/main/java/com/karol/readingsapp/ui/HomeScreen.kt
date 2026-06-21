package com.karol.readingsapp.ui

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.CardLavender
import com.karol.readingsapp.ui.theme.DateGrey
import com.karol.readingsapp.ui.theme.TextBlue
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
                    label = { Text(strings.home, textAlign = TextAlign.Center) },
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
                    label = { Text(strings.calendar, textAlign = TextAlign.Center) },
                    selected = false,
                    onClick = onCalendarClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.bible) },
                    label = { Text(strings.bible, textAlign = TextAlign.Center) },
                    selected = false,
                    onClick = onBibleClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.settings) },
                    label = { Text(strings.settings, textAlign = TextAlign.Center) },
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
                val selectedName = translations.find { it.code == selectedCode }?.name ?: "Select Bible"
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
                        tint = TextBlue,
                        modifier = Modifier.size(30.dp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
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
                                            color = TextBlue,
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
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    AutoResizingText(
                        text = if ((selectedDate == todayString) || (selectedDate.isEmpty())) strings.todaysReadings else strings.selectedReadings,
                        color = TextBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    AutoResizingText(
                        text = displayDate,
                        color = DateGrey,
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
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun AutoResizingText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
) {
    var currentFontSize by remember(text) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(value = false) }

    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        fontSize = currentFontSize,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        softWrap = false,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        onTextLayout = { layoutResult ->
            if (layoutResult.didOverflowWidth) {
                if (currentFontSize.value > 10f) {
                    currentFontSize = (currentFontSize.value - 0.5f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
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
    
    // Dynamic dimensions for maximum compactness
    val sectionPadding = 8.dp
    val titleSize = 12.sp
    val innerSpacer = 4.dp
    val itemSpacing = 4.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLavender),
        shape = RoundedCornerShape(12.dp),
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
    onClick: () -> Unit,
) {
    val bookName = strings.bookNames[item.bookId] ?: item.bookName
    val text = "$bookName ${item.chapter}"
    
    val fontSize = 14.sp
    val verticalPadding = 6.dp
    val horizontalPadding = 12.dp

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
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
