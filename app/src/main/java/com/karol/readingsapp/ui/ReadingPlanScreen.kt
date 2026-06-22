package com.karol.readingsapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.karol.readingsapp.data.plan.SimpleReading
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReadingPlanScreen(
    viewModel: ReadingViewModel,
    onDateClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onBibleClick: () -> Unit,
    onSettingsClick: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }
    val monthlyPlan by viewModel.monthlyPlan.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedTranslation by viewModel.selectedTranslationCode.collectAsState()

    val selectedLanguage = remember(selectedTranslation, translations) {
        translations.find { it.code == selectedTranslation }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

    val listState = rememberLazyListState()

    LaunchedEffect(currentMonth) {
        val monthStr = String.format(Locale.US, "%04d-%02d", currentMonth.year, currentMonth.monthValue)
        viewModel.loadMonthlyPlan(monthStr)
    }

    val datesInMonth = remember(currentMonth) {
        val daysInMonth = currentMonth.lengthOfMonth()
        (1..daysInMonth).map { day ->
            currentMonth.atDay(day).toString() // YYYY-MM-DD
        }
    }

    val todayIndex = remember(datesInMonth, today) {
        datesInMonth.indexOf(today.toString())
    }

    LaunchedEffect(todayIndex, listState.layoutInfo.viewportSize) {
        if (todayIndex != -1 && listState.layoutInfo.viewportSize.height > 0) {
            val viewportHeight = listState.layoutInfo.viewportSize.height
            // Scroll so the top of the item is roughly at the center of the viewport
            // Subtracting an estimated half-item height (approx 40dp in pixels) to center it better
            val density = 3f // Approximate density, though using pixel offset is safer
            val halfItemHeight = (40 * density).toInt() 
            listState.scrollToItem(todayIndex, -(viewportHeight / 2 - halfItemHeight))
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                // Custom 40dp Navigation Bar with centered icon and Home button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    color = BackgroundBlue,
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
                                tint = TextBlue,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = TextBlue,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
                
                // Month Selector Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = BackgroundBlue,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { currentMonth = currentMonth.minusMonths(1) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.previousMonth,
                                tint = TextBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AutoResizingText(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM", strings.locale)),
                            color = TextBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )

                        IconButton(
                            onClick = { currentMonth = currentMonth.plusMonths(1) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = strings.nextMonth,
                                tint = TextBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                    label = { Text(strings.home) },
                    selected = false,
                    onClick = onHomeClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = strings.calendar) },
                    label = { Text(strings.calendar) },
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(datesInMonth, key = { _, date -> date }) { _, date ->
                val readings = monthlyPlan[date] ?: emptyList()
                ReadingDayItem(
                    date = date,
                    readings = readings,
                    strings = strings,
                    isToday = date == today.toString()
                ) {
                    onDateClick(date)
                }
            }
        }
    }
}

@Composable
fun ReadingDayItem(
    date: String,
    readings: List<SimpleReading>,
    strings: LocalizedStrings,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val parsedDate = try {
        LocalDate.parse(date)
    } catch (_: Exception) {
        null
    }
    
    val dayOfWeek = parsedDate?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL, strings.locale) ?: "---"
    val dayOfMonth = parsedDate?.dayOfMonth?.toString() ?: "--"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) CardLavender else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = dayOfMonth,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlue
                )
                Text(
                    text = dayOfWeek.take(3),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (readings.isEmpty()) {
                    Text(
                        text = strings.noReadingsShort,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                } else {
                    readings.forEach { reading ->
                        val bookName = strings.bookNames[reading.bookId] ?: reading.bookName
                        AutoResizingText(
                            text = "$bookName ${reading.chaptersStr}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

