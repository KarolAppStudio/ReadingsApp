package com.karol.readingsapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.components.SelectionButton
import com.karol.readingsapp.ui.components.TranslationSelector
import com.karol.readingsapp.ui.theme.AdaptiveDimens
import com.karol.readingsapp.ui.theme.GlassBorder
import kotlinx.coroutines.launch
import java.text.NumberFormat

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
    val allBooks by viewModel.allBooks.collectAsState()

    val listState1 = rememberLazyListState()
    val listState2 = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isSyncEnabled by remember { mutableStateOf(value = false) }

    // State for Side 1
    var bookId1 by remember { mutableIntStateOf(bookId) }
    var chapter1 by remember { mutableIntStateOf(chapter) }
    var chapterCount1 by remember { mutableIntStateOf(0) }

    // State for Side 2
    var bookId2 by remember { mutableIntStateOf(bookId) }
    var chapter2 by remember { mutableIntStateOf(chapter) }
    var chapterCount2 by remember { mutableIntStateOf(0) }

    // Helpers for Side 1
    val strings1 = getStrings(selectedCode1, translations)
    val numberFormatter1 = remember(strings1.locale) { NumberFormat.getIntegerInstance(strings1.locale) }
    val bookName1 = strings1.bookNames[bookId1] ?: "Book $bookId1"

    // Helpers for Side 2
    val strings2 = getStrings(selectedCode2, translations)
    val numberFormatter2 = remember(strings2.locale) { NumberFormat.getIntegerInstance(strings2.locale) }
    val bookName2 = strings2.bookNames[bookId2] ?: "Book $bookId2"

    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    // Effects
    LaunchedEffect(bookId1) { chapterCount1 = viewModel.getChapterCount(bookId1) }
    LaunchedEffect(bookId2) { chapterCount2 = viewModel.getChapterCount(bookId2) }

    LaunchedEffect(bookId1, chapter1, selectedCode1) {
        viewModel.loadChapterVerses(bookId1, chapter1)
    }
    LaunchedEffect(bookId2, chapter2, selectedCode2) {
        viewModel.loadSecondChapterVerses(bookId2, chapter2, selectedCode2)
    }

    LaunchedEffect(isSyncEnabled) {
        if (isSyncEnabled) {
            listState2.scrollToItem(listState1.firstVisibleItemIndex, listState1.firstVisibleItemScrollOffset)
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    val displayTitle = if (bookId1 == bookId2 && chapter1 == chapter2) {
                        "$bookName1 ${numberFormatter1.format(chapter1)}"
                    } else {
                        "$bookName1 ${numberFormatter1.format(chapter1)} | $bookName2 ${numberFormatter2.format(chapter2)}"
                    }
                    AutoResizingText(
                        text = displayTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = AdaptiveDimens.bodyFontSize,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, strings1.back, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    SyncToggleButton(isSyncEnabled, strings1.sync) { isSyncEnabled = !isSyncEnabled }
                    ResetButton(strings1.reset) {
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxHeight().widthIn(max = AdaptiveDimens.contentMaxWidth)) {
                // Selection Area
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReadingSideSelector(
                        modifier = Modifier.weight(1f),
                        selectedCode = selectedCode1,
                        translations = translations,
                        allBooks = allBooks,
                        strings = strings1,
                        numberFormatter = numberFormatter1,
                        chapterCount = chapterCount1,
                        isPleasant = isPleasant,
                        onTranslationSelected = { viewModel.setTranslation(it) },
                    ) { b, c ->
                        if (b != -1) bookId1 = b
                        chapter1 = c
                        if (isSyncEnabled) {
                            if (b != -1) bookId2 = b
                            chapter2 = c
                        }
                    }
                    ReadingSideSelector(
                        modifier = Modifier.weight(1f),
                        selectedCode = selectedCode2,
                        translations = translations,
                        allBooks = allBooks,
                        strings = strings2,
                        numberFormatter = numberFormatter2,
                        chapterCount = chapterCount2,
                        isPleasant = isPleasant,
                        onTranslationSelected = { viewModel.loadSecondChapterVerses(bookId2, chapter2, it) },
                    ) { b, c ->
                        if (b != -1) bookId2 = b
                        chapter2 = c
                        if (isSyncEnabled) {
                            if (b != -1) bookId1 = b
                            chapter1 = c
                        }
                    }
                }

                // Content Area
                if (isSyncEnabled) {
                    SyncedVersesList(listState1, verses1, verses2, numberFormatter1, numberFormatter2)
                } else {
                    IndependentVersesList(listState1, listState2, verses1, verses2, numberFormatter1, numberFormatter2)
                }
            }
        }
    }
}

@Composable
private fun getStrings(code: String, translations: List<TranslationEntity>): LocalizedStrings {
    val language = remember(code, translations) {
        translations.find { it.code == code }?.language ?: "English"
    }
    return remember(language) { Localization.getStrings(language) }
}

@Composable
private fun ReadingSideSelector(
    modifier: Modifier,
    selectedCode: String,
    translations: List<TranslationEntity>,
    allBooks: List<BookEntity>,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    chapterCount: Int,
    isPleasant: Boolean,
    onTranslationSelected: (String) -> Unit,
    onLocationSelected: (Int, Int) -> Unit,
) {
    Column(modifier = modifier) {
        TranslationSelector(
            selectedTranslationCode = selectedCode,
            translations = translations,
            onTranslationSelected = onTranslationSelected,
            modifier = Modifier.fillMaxWidth(),
            placeholder = strings.selectBible,
            isPleasant = isPleasant,
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SelectionButton(
                text = strings.book,
                options = remember(allBooks, strings) { allBooks.map { strings.bookNames[it.id] ?: it.name } },
                onOptionSelected = { onLocationSelected(allBooks[it].id, 1) },
                modifier = Modifier.weight(1f),
                isPleasant = isPleasant,
                height = if (AdaptiveDimens.fontScale > 1.0f) 48.dp else 32.dp,
                fontSize = AdaptiveDimens.smallFontSize,
                cornerRadius = 26.dp,
            )
            SelectionButton(
                text = strings.chapter,
                options = remember(chapterCount, numberFormatter) { (1..chapterCount).map { numberFormatter.format(it) } },
                onOptionSelected = { onLocationSelected(-1, it + 1) }, // -1 indicates keeping current book
                modifier = Modifier.weight(1f),
                isPleasant = isPleasant,
                height = if (AdaptiveDimens.fontScale > 1.0f) 48.dp else 32.dp,
                fontSize = AdaptiveDimens.smallFontSize,
                cornerRadius = 26.dp,
            )
        }
    }
}

@Composable
private fun SyncedVersesList(
    listState: LazyListState,
    verses1: List<TargetReadingDetails>,
    verses2: List<TargetReadingDetails>,
    formatter1: NumberFormat,
    formatter2: NumberFormat,
) {
    val maxVerses = maxOf(verses1.size, verses2.size)
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(maxVerses) { index ->
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    verses1.getOrNull(index)?.let { VerseItem(it, formatter1) }
                }
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )
                Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    verses2.getOrNull(index)?.let { VerseItem(it, formatter2) }
                }
            }
        }
    }
}

@Composable
private fun IndependentVersesList(
    listState1: LazyListState,
    listState2: LazyListState,
    verses1: List<TargetReadingDetails>,
    verses2: List<TargetReadingDetails>,
    formatter1: NumberFormat,
    formatter2: NumberFormat,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState1, modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp)) {
            items(verses1) { VerseItem(it, formatter1) }
        }
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        LazyColumn(state = listState2, modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp)) {
            items(verses2) { VerseItem(it, formatter2) }
        }
    }
}

@Composable
private fun SyncToggleButton(isEnabled: Boolean, contentDescription: String, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ledTransition")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ledAlpha",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = contentDescription,
                tint = if (isEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            if (isEnabled) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(Color.Green.copy(alpha = ledAlpha * 0.4f), radius = size.width * 1.5f)
                    drawCircle(Color.Green.copy(alpha = ledAlpha), radius = size.width / 2)
                }
            }
        }
        Text(text = contentDescription, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
    }
}

@Composable
private fun ResetButton(contentDescription: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp)) {
        Icon(Icons.Default.RestartAlt, contentDescription, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(text = contentDescription, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
    }
}

@Composable
fun VerseItem(verse: TargetReadingDetails, numberFormatter: NumberFormat) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = numberFormatter.format(verse.verseId),
            fontSize = AdaptiveDimens.smallFontSize * 0.8f,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = verse.text,
            fontSize = AdaptiveDimens.bodyFontSize,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = AdaptiveDimens.bodyFontSize * 1.4f,
        )
    }
}
