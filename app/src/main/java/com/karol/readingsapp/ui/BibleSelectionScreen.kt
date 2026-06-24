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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
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
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.theme.GlassBorder

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
        if ((selectedBook != null) && (selectedChapter > 0)) {
            verseCount = viewModel.getVerseCount(selectedBook!!.id, selectedChapter)
        }
    }

    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp),
                color = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
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
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    AutoResizingText(
                        text = when {
                            selectedBook == null -> strings.bible
                            selectedChapter == 0 -> strings.bookNames[selectedBook!!.id] ?: selectedBook!!.name
                            else -> "${strings.bookNames[selectedBook!!.id] ?: selectedBook!!.name} $selectedChapter"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
                                .clickable {
                                    val bookId = selectedBook?.id ?: 0
                                    val chapter = if (selectedChapter > 0) selectedChapter else 1
                                    onParallelClick(bookId, chapter)
                                }
                                .padding(horizontal = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Parallel Reading",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = "Parallel",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                            )
                        }

                        if (selectedBook != null) {
                            IconButton(
                                onClick = {
                                    if (selectedChapter != 0) {
                                        selectedChapter = 0
                                    } else {
                                        selectedBook = null
                                    }
                                },
                            ) {
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
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onCalendarClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = strings.bible) },
                    label = { Text(strings.bible, textAlign = TextAlign.Center) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SecondaryTabRow(
                selectedTabIndex = currentMode.ordinal,
                containerColor = if (isPleasant) Color.Transparent else MaterialTheme.colorScheme.background,
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
                        text = { Text(mode.name, fontSize = 12.sp) },
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
                    ) { chapter ->
                        selectedChapter = chapter
                    }
                } else {
                    VerseSelection(
                        mode = currentMode,
                        verseCount = verseCount,
                    ) { verse ->
                        onChapterClick(selectedBook!!.id, selectedChapter, verse)
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
    onChapterClick: (Int) -> Unit,
) {
    val chapters = (1..chapterCount).toList()

    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chapters) { chapter ->
                    ChapterCard(chapter, onChapterClick)
                }
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(chapters) { chapter ->
                    Surface(
                        onClick = { onChapterClick(chapter) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Chapter $chapter",
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
fun VerseSelection(
    mode: NavMode,
    verseCount: Int,
    onVerseClick: (Int) -> Unit,
) {
    val verses = (1..verseCount).toList()

    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(verses) { verse ->
                    ChapterCard(verse, onVerseClick) // Reusing ChapterCard style
                }
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(verses) { verse ->
                    Surface(
                        onClick = { onVerseClick(verse) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Verse $verse",
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
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    val (backgroundColor, textColor) = when (book.testament) {
        "OT" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "NT" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        onClick = { onClick(book) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(2.dp),
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
fun ChapterCard(chapter: Int, onClick: (Int) -> Unit) {
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    Card(
        onClick = { onClick(chapter) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = if (isPleasant) null else CardDefaults.outlinedCardBorder(),
        elevation = if (isPleasant) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(1.dp),
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = chapter.toString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
