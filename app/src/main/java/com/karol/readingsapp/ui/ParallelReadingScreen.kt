package com.karol.readingsapp.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val listState1 = rememberLazyListState()
    val listState2 = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isSyncEnabled by remember { mutableStateOf(value = false) }

    val infiniteTransition = rememberInfiniteTransition(label = "ledTransition")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ledAlpha",
    )

    LaunchedEffect(isSyncEnabled) {
        listState2.scrollToItem(
            listState1.firstVisibleItemIndex,
            listState1.firstVisibleItemScrollOffset,
        )
    }

    val selectedLanguage1 = remember(selectedCode1, translations) {
        translations.find { it.code == selectedCode1 }?.language ?: "English"
    }
    val strings1 = remember(selectedLanguage1) { Localization.getStrings(selectedLanguage1) }

    val selectedLanguage2 = remember(selectedCode2, translations) {
        translations.find { it.code == selectedCode2 }?.language ?: "English"
    }
    val strings2 = remember(selectedLanguage2) { Localization.getStrings(selectedLanguage2) }

    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    var bookId1 by remember { mutableIntStateOf(bookId) }
    var chapter1 by remember { mutableIntStateOf(chapter) }
    var bookId2 by remember { mutableIntStateOf(bookId) }
    var chapter2 by remember { mutableIntStateOf(chapter) }

    var chapterCount1 by remember { mutableIntStateOf(0) }
    var chapterCount2 by remember { mutableIntStateOf(0) }

    val allBooks by viewModel.allBooks.collectAsState()
    val bookOptions1 = remember(allBooks, strings1) {
        allBooks.map { strings1.bookNames[it.id] ?: it.name }
    }
    val bookOptions2 = remember(allBooks, strings2) {
        allBooks.map { strings2.bookNames[it.id] ?: it.name }
    }

    LaunchedEffect(bookId1) {
        chapterCount1 = viewModel.getChapterCount(bookId1)
    }
    LaunchedEffect(bookId2) {
        chapterCount2 = viewModel.getChapterCount(bookId2)
    }

    val bookName1 = strings1.bookNames[bookId1] ?: "Book $bookId1"
    val bookName2 = strings2.bookNames[bookId2] ?: "Book $bookId2"

    val numberFormatter1 = remember(strings1.locale) {
        NumberFormat.getIntegerInstance(strings1.locale)
    }
    val numberFormatter2 = remember(strings2.locale) {
        NumberFormat.getIntegerInstance(strings2.locale)
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
                        "$bookName1 ${numberFormatter1.format(chapter1)}"
                    } else {
                        "$bookName1 ${numberFormatter1.format(chapter1)} | $bookName2 ${numberFormatter2.format(chapter2)}"
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
                            contentDescription = strings1.back,
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
                            Icon(
                                imageVector = if (isSyncEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = strings1.sync,
                                tint = if (isSyncEnabled) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp),
                            )
                            if (isSyncEnabled) {
                                Canvas(modifier = Modifier.size(6.dp)) {
                                    // Outer glow
                                    drawCircle(
                                        color = Color.Green.copy(alpha = ledAlpha * 0.4f),
                                        radius = size.width * 1.5f,
                                    )
                                    // Main LED dot
                                    drawCircle(
                                        color = Color.Green.copy(alpha = ledAlpha),
                                        radius = size.width / 2,
                                    )
                                }
                            }
                        }
                        Text(
                            text = strings1.sync,
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
                                contentDescription = strings1.reset,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            text = strings1.reset,
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
                        placeholder = strings1.selectBible,
                        isPleasant = isPleasant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SelectionButton(
                            text = strings1.book,
                            options = bookOptions1,
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
                            text = strings1.chapter,
                            options = (1..chapterCount1).map { numberFormatter1.format(it) },
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
                        placeholder = strings2.selectBible,
                        isPleasant = isPleasant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SelectionButton(
                            text = strings2.book,
                            options = bookOptions2,
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
                            text = strings2.chapter,
                            options = (1..chapterCount2).map { numberFormatter2.format(it) },
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
                                v1?.let { VerseItem(it, numberFormatter1) }
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
                                v2?.let { VerseItem(it, numberFormatter2) }
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
                            VerseItem(verse, numberFormatter1)
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
                            VerseItem(verse, numberFormatter2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItem(verse: TargetReadingDetails, numberFormatter: NumberFormat) {
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
