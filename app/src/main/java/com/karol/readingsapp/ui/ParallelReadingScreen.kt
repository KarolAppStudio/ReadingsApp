package com.karol.readingsapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.SelectionButton
import com.karol.readingsapp.ui.components.TranslationSelector
import com.karol.readingsapp.ui.theme.GlassBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParallelReadingScreen(
    bookId: Int,
    chapter: Int,
    viewModel: ReadingViewModel,
    onBackClick: () -> Unit,
) {
    val verses1 by viewModel.chapterVerses.collectAsState()
    val verses2 by viewModel.secondChapterVerses.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode1 by viewModel.selectedTranslationCode.collectAsState()
    val selectedCode2 by viewModel.secondTranslationCode.collectAsState()

    val listState1 = rememberLazyListState()
    val listState2 = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isSyncEnabled by remember { mutableStateOf(value = false) }

    LaunchedEffect(isSyncEnabled) {
        listState2.scrollToItem(
            listState1.firstVisibleItemIndex,
            listState1.firstVisibleItemScrollOffset,
        )
    }

    val selectedLanguage = remember(selectedCode1, translations) {
        translations.find { it.code == selectedCode1 }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    var bookId1 by remember { mutableIntStateOf(bookId) }
    var chapter1 by remember { mutableIntStateOf(chapter) }
    var bookId2 by remember { mutableIntStateOf(bookId) }
    var chapter2 by remember { mutableIntStateOf(chapter) }

    var chapterCount1 by remember { mutableIntStateOf(0) }
    var chapterCount2 by remember { mutableIntStateOf(0) }

    val allBooks by viewModel.allBooks.collectAsState()
    val bookOptions = remember(allBooks, strings) {
        allBooks.map { strings.bookNames[it.id] ?: it.name }
    }

    LaunchedEffect(bookId1) {
        chapterCount1 = viewModel.getChapterCount(bookId1)
    }
    LaunchedEffect(bookId2) {
        chapterCount2 = viewModel.getChapterCount(bookId2)
    }

    val bookName1 = strings.bookNames[bookId1] ?: "Book $bookId1"
    val bookName2 = strings.bookNames[bookId2] ?: "Book $bookId2"

    val numberFormatter = remember(strings.locale) {
        java.text.NumberFormat.getIntegerInstance(strings.locale)
    }

    LaunchedEffect(bookId1, chapter1, selectedCode1) {
        viewModel.loadChapterVerses(bookId1, chapter1)
    }

    LaunchedEffect(bookId2, chapter2, selectedCode2) {
        viewModel.loadSecondChapterVerses(bookId2, chapter2, selectedCode2)
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    val displayTitle = if ((bookId1 == bookId2) && (chapter1 == chapter2)) {
                        "$bookName1 $chapter1"
                    } else {
                        "$bookName1 $chapter1 | $bookName2 $chapter2"
                    }
                    AutoResizingText(
                        text = displayTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { isSyncEnabled = !isSyncEnabled }
                            .padding(horizontal = 8.dp),
                    ) {
                        val activeColor = MaterialTheme.colorScheme.tertiary
                        val inactiveColor = MaterialTheme.colorScheme.primary
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            if (isSyncEnabled) {
                                Canvas(modifier = Modifier.size(32.dp)) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                activeColor.copy(alpha = 0.4f),
                                                Color.Transparent,
                                            ),
                                            center = center,
                                            radius = size.width / 2,
                                        ),
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isSyncEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = strings.sync,
                                tint = if (isSyncEnabled) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            text = "Lock Grids",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                bookId1 = bookId
                                chapter1 = chapter
                                bookId2 = bookId
                                chapter2 = chapter
                                viewModel.resetParallelReading(bookId, chapter)
                                scope.launch {
                                    listState1.animateScrollToItem(0)
                                    listState2.animateScrollToItem(0)
                                }
                            }
                            .padding(horizontal = 8.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = strings.reset,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            text = "Reset",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Selectors Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TranslationSelector(
                        selectedTranslationCode = selectedCode1,
                        translations = translations,
                        onTranslationSelected = {
                            viewModel.setTranslation(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = strings.selectBible,
                        isPleasant = isPleasant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SelectionButton(
                            text = bookName1,
                            options = bookOptions,
                            onOptionSelected = { index ->
                                val newBookId = allBooks[index].id
                                bookId1 = newBookId
                                chapter1 = 1
                                if (isSyncEnabled) {
                                    bookId2 = newBookId
                                    chapter2 = 1
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isPleasant = isPleasant,
                        )
                        SelectionButton(
                            text = chapter1.toString(),
                            options = (1..chapterCount1).map { it.toString() },
                            onOptionSelected = {
                                val newChapter = it + 1
                                chapter1 = newChapter
                                if (isSyncEnabled) {
                                    chapter2 = newChapter
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isPleasant = isPleasant,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    TranslationSelector(
                        selectedTranslationCode = selectedCode2,
                        translations = translations,
                        onTranslationSelected = {
                            viewModel.loadSecondChapterVerses(bookId2, chapter2, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = strings.selectBible,
                        isPleasant = isPleasant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SelectionButton(
                            text = bookName2,
                            options = bookOptions,
                            onOptionSelected = { index ->
                                val newBookId = allBooks[index].id
                                bookId2 = newBookId
                                chapter2 = 1
                                if (isSyncEnabled) {
                                    bookId1 = newBookId
                                    chapter1 = 1
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isPleasant = isPleasant,
                        )
                        SelectionButton(
                            text = chapter2.toString(),
                            options = (1..chapterCount2).map { it.toString() },
                            onOptionSelected = {
                                val newChapter = it + 1
                                chapter2 = newChapter
                                if (isSyncEnabled) {
                                    chapter1 = newChapter
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isPleasant = isPleasant,
                        )
                    }
                }
            }

            // Content Row
            if (isSyncEnabled) {
                val maxVerses = maxOf(verses1.size, verses2.size)
                LazyColumn(
                    state = listState1,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(maxVerses) { index ->
                        val v1 = verses1.getOrNull(index)
                        val v2 = verses2.getOrNull(index)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                            ) {
                                v1?.let { VerseItem(it, numberFormatter) }
                            }
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                            ) {
                                v2?.let { VerseItem(it, numberFormatter) }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // First Column
                    LazyColumn(
                        state = listState1,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                    ) {
                        items(verses1) { verse ->
                            VerseItem(verse, numberFormatter)
                        }
                    }

                    // Divider
                    VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // Second Column
                    LazyColumn(
                        state = listState2,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                    ) {
                        items(verses2) { verse ->
                            VerseItem(verse, numberFormatter)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItem(verse: TargetReadingDetails, numberFormatter: java.text.NumberFormat) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = numberFormatter.format(verse.verseId),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = verse.text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 20.sp,
        )
    }
}
