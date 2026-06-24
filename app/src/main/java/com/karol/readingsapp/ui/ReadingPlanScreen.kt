package com.karol.readingsapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.plan.SimpleReading
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.theme.GlassBorder
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReadingPlanScreen(
    viewModel: ReadingViewModel,
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

    LaunchedEffect(todayIndex) {
        if (todayIndex != -1) {
            snapshotFlow { listState.layoutInfo.viewportSize.height }
                .first { it > 0 }
                .let { viewportHeight ->
                    // Scroll so the top of the item is roughly at the center of the viewport
                    // Subtracting an estimated half-item height (approx 40dp in pixels) to center it better
                    val density = 3f // Approximate density, though using pixel offset is safer
                    val halfItemHeight = (40 * density).toInt()
                    listState.scrollToItem(todayIndex, -((viewportHeight / 2) - halfItemHeight))
                }
        }
    }

    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                // Custom 40dp Navigation Bar with centered icon and Home button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
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
                    color = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        IconButton(
                            onClick = { currentMonth = currentMonth.minusMonths(1) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.previousMonth,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        AutoResizingText(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM", strings.locale)),
                            color = MaterialTheme.colorScheme.primary,
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = if (isPleasant) 0.dp else 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                    label = { Text(strings.home, textAlign = TextAlign.Center) },
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onHomeClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = strings.calendar) },
                    label = { Text(strings.calendar, textAlign = TextAlign.Center) },
                    selected = true,
                    alwaysShowLabel = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.bible) },
                    label = { Text(strings.bible, textAlign = TextAlign.Center) },
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onBibleClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.settings) },
                    label = { Text(strings.settings, textAlign = TextAlign.Center) },
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onSettingsClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            itemsIndexed(datesInMonth, key = { _, date -> date }) { _, date ->
                val readings = monthlyPlan[date] ?: emptyList()
                ReadingDayItem(
                    date = date,
                    readings = readings,
                    strings = strings,
                    isToday = date == today.toString(),
                )
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
) {
    val parsedDate = try {
        LocalDate.parse(date)
    } catch (_: Exception) {
        null
    }

    val dayOfWeek = parsedDate?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL, strings.locale) ?: "---"
    val dayOfMonth = parsedDate?.dayOfMonth?.toString() ?: "--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp),
            ) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00FF00), shape = CircleShape),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = dayOfMonth,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = dayOfWeek.take(3),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (readings.isEmpty()) {
                    Text(
                        text = strings.noReadingsShort,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    )
                } else {
                    readings.forEach { reading ->
                        val bookName = strings.bookNames[reading.bookId] ?: reading.bookName
                        AutoResizingText(
                            text = "$bookName ${reading.chaptersStr}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
