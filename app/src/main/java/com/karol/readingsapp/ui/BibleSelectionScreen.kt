package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.CardLavender
import com.karol.readingsapp.ui.theme.TextBlue

enum class NavMode { Grid, List }

@Composable
fun BibleSelectionScreen(
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChapterClick: (Int, Int) -> Unit,
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
        if (selectedBook != null && selectedChapter > 0) {
            verseCount = viewModel.getVerseCount(selectedBook!!.id, selectedChapter)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp),
                color = BackgroundBlue,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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

                    Text(
                        text = when {
                            selectedBook == null -> strings.bible
                            selectedChapter == 0 -> strings.bookNames[selectedBook!!.id] ?: selectedBook!!.name
                            else -> "${strings.bookNames[selectedBook!!.id] ?: selectedBook!!.name} $selectedChapter"
                        },
                        color = TextBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (selectedBook != null) {
                        IconButton(
                            onClick = {
                                if (selectedChapter != 0) {
                                    selectedChapter = 0
                                } else {
                                    selectedBook = null
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection", tint = TextBlue)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                containerColor = BackgroundBlue,
                contentColor = TextBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentMode.ordinal]),
                        color = TextBlue
                    )
                }
            ) {
                NavMode.entries.forEach { mode ->
                    Tab(
                        selected = currentMode == mode,
                        onClick = { currentMode = mode },
                        text = { Text(mode.name, fontSize = 12.sp) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedBook == null) {
                    BookSelection(
                        mode = currentMode,
                        books = allBooks,
                        strings = strings,
                        onBookClick = { selectedBook = it }
                    )
                } else if (selectedChapter == 0) {
                    ChapterSelection(
                        mode = currentMode,
                        chapterCount = chapterCount,
                        onChapterClick = { chapter ->
                            selectedChapter = chapter
                        }
                    )
                } else {
                    VerseSelection(
                        mode = currentMode,
                        verseCount = verseCount,
                        onVerseClick = { verse ->
                            onChapterClick(selectedBook!!.id, selectedChapter)
                        }
                    )
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
    onBookClick: (BookEntity) -> Unit
) {
    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    BookCard(book, strings, onBookClick)
                }
            }
        }
        NavMode.List -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
    onChapterClick: (Int) -> Unit
) {
    val chapters = (1..chapterCount).toList()

    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chapters) { chapter ->
                    ChapterCard(chapter, onChapterClick)
                }
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chapters) { chapter ->
                    Surface(
                        onClick = { onChapterClick(chapter) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Chapter $chapter",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Black
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
    onVerseClick: (Int) -> Unit
) {
    val verses = (1..verseCount).toList()

    when (mode) {
        NavMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(verses) { verse ->
                    ChapterCard(verse, onVerseClick) // Reusing ChapterCard style
                }
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(verses) { verse ->
                    Surface(
                        onClick = { onVerseClick(verse) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Verse $verse",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(book: BookEntity, strings: LocalizedStrings, onClick: (BookEntity) -> Unit) {
    Card(
        onClick = { onClick(book) },
        colors = CardDefaults.cardColors(containerColor = CardLavender),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.bookNames[book.id] ?: book.name,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlue,
                maxLines = 2
            )
        }
    }
}

@Composable
fun BookListItem(book: BookEntity, strings: LocalizedStrings, onClick: (BookEntity) -> Unit) {
    Surface(
        onClick = { onClick(book) },
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = strings.bookNames[book.id] ?: book.name,
            modifier = Modifier.padding(16.dp),
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChapterCard(chapter: Int, onClick: (Int) -> Unit) {
    Card(
        onClick = { onClick(chapter) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chapter.toString(),
                fontWeight = FontWeight.Bold,
                color = TextBlue
            )
        }
    }
}
