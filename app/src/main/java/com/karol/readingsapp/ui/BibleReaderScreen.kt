package com.karol.readingsapp.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.theme.GlassBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    bookId: Int,
    chapter: Int,
    initialVerse: Int = 1,
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    onParallelClick: (Int, Int) -> Unit,
    onChapterChange: (Int, Int) -> Unit,
) {
    val verses by viewModel.chapterVerses.collectAsState()
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()

    val listState = rememberLazyListState()
    val highlightColor = remember { Animatable(Color.Transparent) }
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(bookId, chapter, selectedCode) {
        viewModel.loadChapterVerses(bookId, chapter)
    }

    LaunchedEffect(verses, initialVerse) {
        if (verses.isNotEmpty()) {
            val index = verses.indexOfFirst { it.verseId == initialVerse }
            if (index != -1) {
                listState.scrollToItem(index)

                // Blink 3 times in 1.5 seconds (each cycle 500ms: 250ms on, 250ms off)
                repeat(3) {
                    highlightColor.animateTo(secondaryColor.copy(alpha = 0.7f), animationSpec = tween(250))
                    highlightColor.animateTo(Color.Transparent, animationSpec = tween(250))
                }
            }
        }
    }

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder
    val bookName = strings.bookNames[bookId] ?: "Book $bookId"

    val numberFormatter = remember(strings.locale) {
        java.text.NumberFormat.getIntegerInstance(strings.locale)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoResizingText(
                            text = "$bookName ${numberFormatter.format(chapter)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = strings.home,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(35.dp),
                            )
                        }
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.back,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onParallelClick(bookId, chapter) }
                            .padding(horizontal = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = strings.parallelReading,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "Parallel",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .pointerInput(bookId, chapter) {
                    // pointerInput uses bookId and chapter as keys to ensure the gesture
                    // handler is re-initialized with the correct state when the page changes.
                    detectHorizontalDragGestures(
                        onDragStart = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount },
                        onDragEnd = {
                            if (offsetX < -60) { // Swipe Right-to-Left (finger moving <-) -> Next
                                scope.launch {
                                    viewModel.getNextChapter(bookId, chapter)?.let { (bId, chap) ->
                                        onChapterChange(bId, chap)
                                    }
                                }
                            } else if (offsetX > 60) { // Swipe Left-to-Right (finger moving ->) -> Previous
                                scope.launch {
                                    viewModel.getPreviousChapter(bookId, chapter)?.let { (bId, chap) ->
                                        onChapterChange(bId, chap)
                                    }
                                }
                            }
                        },
                    )
                },
        ) {
            if (verses.isEmpty()) {
                item {
                    Text(
                        text = strings.loadingReading,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                    )
                }
            } else {
                items(verses, key = { "${it.translationCode}_${it.bookId}_${it.chapter}_${it.verseId}" }) { verse ->
                    val isSelected = verse.verseId == initialVerse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) highlightColor.value else Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                    ) {
                        Text(
                            text = numberFormatter.format(verse.verseId),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp, end = 8.dp),
                        )
                        Text(
                            text = verse.text,
                            fontSize = 16.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            lineHeight = 24.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
