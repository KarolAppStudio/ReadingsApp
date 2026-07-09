package com.karol.readingsapp.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.ui.components.AppBottomNavBar
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.NavItem
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import java.text.NumberFormat

enum class NavMode { Grid, List }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSelectionScreen(
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChapterClick: (Int, Int, Int) -> Unit,
    onParallelClick: (Int, Int) -> Unit,
) {
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

    val numberFormatter = remember(strings.locale) {
        NumberFormat.getIntegerInstance(strings.locale)
    }

    var currentMode by remember { mutableStateOf(NavMode.Grid) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var selectedChapter by remember { mutableIntStateOf(0) }
    var chapterCount by remember { mutableIntStateOf(0) }
    var verseCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedBook) {
        selectedBook?.let {
            chapterCount = viewModel.getChapterCount(it.id)
            selectedChapter = 0
            verseCount = 0
        }
    }

    LaunchedEffect(selectedBook, selectedChapter) {
        if (selectedBook != null && selectedChapter > 0) {
            verseCount = viewModel.getVerseCount(selectedBook!!.id, selectedChapter)
        }
    }

    Scaffold(
        topBar = {
            SelectionTopBar(
                selectedBook = selectedBook,
                selectedChapter = selectedChapter,
                strings = strings,
                numberFormatter = numberFormatter,
                onHomeClick = onHomeClick,
                onParallelClick = {
                    val bookId = selectedBook?.id ?: 0
                    val chapter = if (selectedChapter > 0) selectedChapter else 1
                    onParallelClick(bookId, chapter)
                },
            ) {
                if (selectedChapter != 0) {
                    selectedChapter = 0
                } else {
                    selectedBook = null
                }
            }
        },
        bottomBar = {
            AppBottomNavBar(
                selectedItem = NavItem.Bible,
                strings = strings,
                onHomeClick = onHomeClick,
                onCalendarClick = onCalendarClick,
                onBibleClick = { },
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
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = AdaptiveDimens.contentMaxWidth),
            ) {
                SecondaryTabRow(
                    selectedTabIndex = currentMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(currentMode.ordinal),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                ) {
                    NavMode.entries.forEach { mode ->
                        Tab(
                            selected = currentMode == mode,
                            onClick = { currentMode = mode },
                            text = { Text(mode.name, fontSize = AdaptiveDimens.smallFontSize) },
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedBook == null) {
                        BookSelection(
                            mode = currentMode,
                            books = allBooks,
                            strings = strings,
                        ) { selectedBook = it }
                    } else if (selectedChapter == 0) {
                        ChapterSelection(
                            mode = currentMode,
                            chapterCount = chapterCount,
                            strings = strings,
                            numberFormatter = numberFormatter,
                        ) { chapter ->
                            selectedChapter = chapter
                        }
                    } else {
                        VerseSelection(
                            mode = currentMode,
                            verseCount = verseCount,
                            strings = strings,
                            numberFormatter = numberFormatter,
                        ) { verse ->
                            onChapterClick(selectedBook!!.id, selectedChapter, verse)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionTopBar(
    selectedBook: BookEntity?,
    selectedChapter: Int,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    onHomeClick: () -> Unit,
    onParallelClick: () -> Unit,
    onClearSelection: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            AutoResizingText(
                text = when {
                    selectedBook == null -> strings.bible
                    selectedChapter == 0 -> strings.bookNames[selectedBook.id] ?: selectedBook.name
                    else -> "${strings.bookNames[selectedBook.id] ?: selectedBook.name} ${numberFormatter.format(selectedChapter)}"
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = AdaptiveDimens.bodyFontSize,
                modifier = Modifier.align(Alignment.Center),
                maxLines = 1,
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onParallelClick() }
                        .padding(horizontal = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = strings.parallelReading,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = strings.parallelReading,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }

                if (selectedBook != null) {
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear selection",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookSelection(
    mode: NavMode,
    books: List<BookEntity>,
    strings: LocalizedStrings,
    onBookClick: (BookEntity) -> Unit,
) {
    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(books) { book ->
                    BookCard(book, strings, onBookClick)
                }
            }
        }

        NavMode.List -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(books) { book ->
                    BookListItem(book, strings, onBookClick)
                }
            }
        }
    }
}

@Composable
fun ChapterSelection(
    mode: NavMode,
    chapterCount: Int,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    onChapterClick: (Int) -> Unit,
) {
    GridOrListSelection(
        mode = mode,
        items = (1..chapterCount).toList(),
        itemLabel = { "${strings.chapter} ${numberFormatter.format(it)}" },
        gridLabel = { numberFormatter.format(it) },
        onItemClick = onChapterClick,
    )
}

@Composable
fun VerseSelection(
    mode: NavMode,
    verseCount: Int,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    onVerseClick: (Int) -> Unit,
) {
    GridOrListSelection(
        mode = mode,
        items = (1..verseCount).toList(),
        itemLabel = { "${strings.verse} ${numberFormatter.format(it)}" },
        gridLabel = { numberFormatter.format(it) },
        onItemClick = onVerseClick,
    )
}

@Composable
fun GridOrListSelection(
    mode: NavMode,
    items: List<Int>,
    itemLabel: (Int) -> String,
    gridLabel: (Int) -> String,
    onItemClick: (Int) -> Unit,
) {
    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items) { item ->
                    ChapterCard(item, gridLabel(item), onItemClick)
                }
            }
        }

        NavMode.List -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items) { item ->
                    Surface(
                        onClick = { onItemClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = itemLabel(item),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(book: BookEntity, strings: LocalizedStrings, onClick: (BookEntity) -> Unit) {
    Log.d("BookCard", "Book ID: ${book.id}, Name: ${book.name}, Localized: ${strings.bookNames[book.id]}")

    val (backgroundColor, textColor) = when (book.testament) {
        "OT" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "NT" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        onClick = { onClick(book) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AutoResizingText(
                text = strings.bookNames[book.id] ?: book.name,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                softWrap = false,
                minFontSize = 8.sp,
            )
        }
    }
}

@Composable
fun BookListItem(book: BookEntity, strings: LocalizedStrings, onClick: (BookEntity) -> Unit) {
    Surface(
        onClick = { onClick(book) },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = strings.bookNames[book.id] ?: book.name,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun ChapterCard(item: Int, label: String, onClick: (Int) -> Unit) {
    Card(
        onClick = { onClick(item) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
