package com.karol.readingsapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.data.plan.SimpleReading
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
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
    onDateClick: (String) -> Unit,
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

    val numberFormatter = remember(strings.locale) {
        NumberFormat.getIntegerInstance(strings.locale)
    }

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

    val density = LocalDensity.current
    LaunchedEffect(todayIndex) {
        if (todayIndex != -1) {
            snapshotFlow { listState.layoutInfo }
                .first { it.viewportSize.height > 0 }
                .let { layoutInfo ->
                    val viewportHeight = layoutInfo.viewportSize.height
                    val itemHeightPx = with(density) { 90.dp.toPx() }.toInt()
                    listState.scrollToItem(todayIndex, -((viewportHeight / 2) - (itemHeightPx / 2)))
                }
        }
    }

    Scaffold(
        topBar = {
            ReadingPlanTopBar(
                currentMonth = currentMonth,
                strings = strings,
                onHomeClick = onHomeClick,
                onPreviousMonthClick = { currentMonth = currentMonth.minusMonths(1) },
            ) { currentMonth = currentMonth.plusMonths(1) }
        },
        bottomBar = {
            AppBottomNavBar(
                selectedItem = NavItem.Calendar,
                strings = strings,
                onHomeClick = onHomeClick,
                onCalendarClick = { },
                onBibleClick = onBibleClick,
                onSettingsClick = onSettingsClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth)
                    .padding(horizontal = AdaptiveDimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                itemsIndexed(datesInMonth, key = { _, date -> date }) { _, date ->
                    val readings = monthlyPlan[date] ?: emptyList()
                    ReadingDayItem(
                        date = date,
                        readings = readings,
                        strings = strings,
                        numberFormatter = numberFormatter,
                        isToday = date == today.toString(),
                    ) { onDateClick(date) }
                }
            }
        }
    }
}

@Composable
fun ReadingPlanTopBar(
    currentMonth: YearMonth,
    strings: LocalizedStrings,
    onHomeClick: () -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
) {
    Column(modifier = Modifier.statusBarsPadding()) {
        // Custom 40dp Navigation Bar with centered icon and Home button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
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
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = onPreviousMonthClick,
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
                    fontSize = AdaptiveDimens.bodyFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                IconButton(
                    onClick = onNextMonthClick,
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
}

@Composable
fun ReadingDayItem(
    date: String,
    readings: List<SimpleReading>,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val parsedDate = try {
        LocalDate.parse(date)
    } catch (_: Exception) {
        null
    }

    val dayOfWeek = parsedDate?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL, strings.locale) ?: "---"
    val dayOfMonth = parsedDate?.dayOfMonth?.let { numberFormatter.format(it) } ?: "--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick,
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
                    // Bright LED Light Indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF00FF00), shape = CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = dayOfMonth,
                    fontSize = AdaptiveDimens.titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                AutoResizingText(
                    text = dayOfWeek,
                    fontSize = AdaptiveDimens.smallFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    minFontSize = AdaptiveDimens.smallFontSize * 0.5f,
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
                        fontSize = AdaptiveDimens.smallFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    )
                } else {
                    readings.forEach { reading ->
                        val bookName = strings.bookNames[reading.bookId] ?: reading.bookName
                        val chapters = Localization.localizeDigits(reading.chaptersStr, strings.locale)
                        AutoResizingText(
                            text = "$bookName $chapters",
                            fontSize = AdaptiveDimens.smallFontSize,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
