package com.karol.readingsapp.feature.bible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.core.i18n.Localization
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.ui.components.AutoResizingText
import com.karol.readingsapp.core.ui.components.SelectionButton
import com.karol.readingsapp.feature.bible.data.BookEntity
import com.karol.readingsapp.feature.bible.data.ChapterReference
import com.karol.readingsapp.feature.bible.data.LanguageStatus
import com.karol.readingsapp.feature.bible.data.TargetReadingDetails
import com.karol.readingsapp.feature.shared.ui.ReadingViewModel
import com.karol.readingsapp.feature.voice.ui.VoiceControlBar
import com.karol.readingsapp.feature.voice.ui.VoiceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    bookId: Int,
    chapter: Int,
    initialVerse: Int = 1,
    readingType: String? = null,
    viewModel: ReadingViewModel,
    voiceViewModel: VoiceViewModel,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    onParallelClick: (Int, Int) -> Unit,
    onChapterChange: (Int, Int, String?) -> Unit,
) {
    val allChapters by viewModel.allChapters.collectAsState()
    val translations by viewModel.downloadedTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()

    val effectiveAllChapters = remember(allChapters, bookId, chapter) {
        if (allChapters.isEmpty()) {
            listOf(ChapterReference(bookId, chapter))
        } else {
            allChapters
        }
    }

    val totalChapters = effectiveAllChapters.size
    val circularMultiplier = 1000
    val virtualPageCount = totalChapters * circularMultiplier

    val pagerState = rememberPagerState(
        initialPage = remember(effectiveAllChapters) {
            val index = effectiveAllChapters.indexOfFirst { (it.bookId == bookId) && (it.chapter == chapter) }
            val safeIndex = if (index != -1) index else 0
            ((circularMultiplier / 2) * totalChapters) + safeIndex
        },
    ) { virtualPageCount }

    LaunchedEffect(bookId, chapter, effectiveAllChapters) {
        if (totalChapters > 0) {
            val targetIndex = effectiveAllChapters.indexOfFirst { (it.bookId == bookId) && (it.chapter == chapter) }
            if (targetIndex != -1) {
                val currentActualIndex = pagerState.currentPage % totalChapters
                val isNotInitialized = pagerState.currentPage < totalChapters
                if ((isNotInitialized || (targetIndex != currentActualIndex)) && !pagerState.isScrollInProgress) {
                    val virtualIndex = ((circularMultiplier / 2) * totalChapters) + targetIndex
                    pagerState.scrollToPage(virtualIndex)
                }
            }
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (totalChapters > 0) {
            val actualIndex = pagerState.settledPage % totalChapters
            val currentRef = effectiveAllChapters[actualIndex]
            if ((currentRef.bookId != bookId) || (currentRef.chapter != chapter)) {
                onChapterChange(currentRef.bookId, currentRef.chapter, readingType)
            }
        }
    }

    val displayIndex = pagerState.currentPage % totalChapters
    val currentRef = effectiveAllChapters[displayIndex]

    val displayBookId = currentRef.bookId
    val displayChapter = currentRef.chapter

    var chapterCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(displayBookId) {
        chapterCount = viewModel.getChapterCount(displayBookId)
    }

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val bookName = strings.bookNames[displayBookId] ?: "Book $displayBookId"

    val bookOptions = remember(allBooks, strings) {
        allBooks.map { strings.bookNames[it.id] ?: it.name }
    }

    val numberFormatter = remember(strings.locale) {
        NumberFormat.getIntegerInstance(strings.locale)
    }

    val scope = rememberCoroutineScope()

    var currentVerses by remember { mutableStateOf<List<TargetReadingDetails>>(emptyList()) }
    var selectedVerseId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(displayBookId, displayChapter, selectedCode) {
        currentVerses = emptyList()
        selectedVerseId = null
    }

    val uiState by viewModel.uiState.collectAsState()

    val textToRead = remember(currentVerses, selectedVerseId, readingType, uiState) {
        if (selectedVerseId != null) {
            currentVerses.find { it.verseId == selectedVerseId }?.text ?: ""
        } else if ((readingType != null) && uiState.containsKey(readingType)) {
            uiState[readingType]?.joinToString(" ") { it.text } ?: ""
        } else {
            currentVerses.joinToString(" ") { it.text }
        }
    }

    if (allChapters.isEmpty() && uiState.isEmpty() && currentVerses.isEmpty()) {
        Scaffold(
            topBar = {
                ReaderTopBar(
                    bookName = bookName,
                    displayBookId = displayBookId,
                    displayChapter = displayChapter,
                    strings = strings,
                    numberFormatter = numberFormatter,
                    bookOptions = bookOptions,
                    chapterCount = chapterCount,
                    allBooks = allBooks,
                    onHomeClick = onHomeClick,
                    onBackClick = onBackClick,
                    onParallelClick = { onParallelClick(displayBookId, displayChapter) },
                    onChapterChange = onChapterChange,
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    AutoResizingText(
                        text = strings.loadingReading,
                        fontSize = AdaptiveDimens.smallFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            ReaderTopBar(
                bookName = bookName,
                displayBookId = displayBookId,
                displayChapter = displayChapter,
                strings = strings,
                numberFormatter = numberFormatter,
                bookOptions = bookOptions,
                chapterCount = chapterCount,
                allBooks = allBooks,
                onHomeClick = onHomeClick,
                onBackClick = onBackClick,
                onParallelClick = { onParallelClick(displayBookId, displayChapter) },
                onChapterChange = onChapterChange,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            VoiceControlBar(
                viewModel = voiceViewModel,
                textToRead = textToRead,
                locale = strings.locale,
                modifier = Modifier
                    .padding(horizontal = AdaptiveDimens.paddingMedium)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { it },
        ) { pageIndex ->
            ReaderPagerPage(
                pageIndex = pageIndex,
                pagerState = pagerState,
                innerPadding = innerPadding,
                totalChapters = totalChapters,
                allChapters = effectiveAllChapters,
                bookId = bookId,
                chapter = chapter,
                initialVerse = initialVerse,
                readingType = readingType,
                selectedVerseId = if (displayIndex == (pageIndex % totalChapters)) selectedVerseId else null,
                onVerseClick = { vId ->
                    if (displayIndex == (pageIndex % totalChapters)) {
                        selectedVerseId = if (selectedVerseId == vId) null else vId
                    }
                },
                currentVerses = if (displayIndex == (pageIndex % totalChapters)) currentVerses else emptyList(),
                onVersesLoaded = { loadedVerses ->
                    if (displayIndex == (pageIndex % totalChapters)) {
                        currentVerses = loadedVerses
                    }
                },
                viewModel = viewModel,
                numberFormatter = numberFormatter,
                strings = strings,
                onChapterChange = onChapterChange,
                scope = scope,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    bookName: String,
    displayBookId: Int,
    displayChapter: Int,
    strings: LocalizedStrings,
    numberFormatter: NumberFormat,
    bookOptions: List<String>,
    chapterCount: Int,
    allBooks: List<BookEntity>,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    onParallelClick: () -> Unit,
    onChapterChange: (Int, Int, String?) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = AdaptiveDimens.contentMaxWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
                                    text = "$bookName ${numberFormatter.format(displayChapter)}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = AdaptiveDimens.bodyFontSize,
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
                                        modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 40.dp else 30.dp),
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
                                    .clickable { onParallelClick() }
                                    .padding(horizontal = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = strings.parallelReading,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                                AutoResizingText(
                                    text = strings.parallelReading,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AdaptiveDimens.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SelectionButton(
                            text = strings.book,
                            options = bookOptions,
                            onOptionSelected = { index ->
                                onChapterChange(allBooks[index].id, 1, null)
                            },
                            modifier = Modifier.weight(1f),
                            height = if (AdaptiveDimens.fontScale > 1.0f) 48.dp else 32.dp,
                            fontSize = AdaptiveDimens.smallFontSize,
                            cornerRadius = 26.dp,
                        )
                        SelectionButton(
                            text = strings.chapter,
                            options = (1..chapterCount).map { numberFormatter.format(it) },
                            onOptionSelected = { index ->
                                onChapterChange(displayBookId, index + 1, null)
                            },
                            modifier = Modifier.weight(1f),
                            height = if (AdaptiveDimens.fontScale > 1.0f) 48.dp else 32.dp,
                            fontSize = AdaptiveDimens.smallFontSize,
                            cornerRadius = 26.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderPagerPage(
    pageIndex: Int,
    pagerState: PagerState,
    innerPadding: PaddingValues,
    totalChapters: Int,
    allChapters: List<ChapterReference>,
    bookId: Int,
    chapter: Int,
    initialVerse: Int,
    readingType: String?,
    selectedVerseId: Int?,
    onVerseClick: (Int) -> Unit,
    onVersesLoaded: (List<TargetReadingDetails>) -> Unit,
    currentVerses: List<TargetReadingDetails>,
    viewModel: ReadingViewModel,
    numberFormatter: NumberFormat,
    strings: LocalizedStrings,
    onChapterChange: (Int, Int, String?) -> Unit,
    scope: CoroutineScope,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0, zIndex = 1f - pageOffset.absoluteValue)
                }
            }
            .graphicsLayer {
                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                val absOffset = pageOffset.absoluteValue
                translationX = pageOffset * size.width
                alpha = (1f - absOffset).coerceIn(0f, 1f)
                scaleX = 1f
                scaleY = 1f
            }
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
            ),
    ) {
        if (totalChapters > 0) {
            val actualIndex = pageIndex % totalChapters
            val ref = allChapters[actualIndex]
            val nextRef = allChapters[(actualIndex + 1) % totalChapters]
            val nextBookName = if (nextRef.bookId != ref.bookId) {
                strings.bookNames[nextRef.bookId]
            } else {
                null
            }

            ChapterPage(
                bookId = ref.bookId,
                chapter = ref.chapter,
                initialVerse = if ((ref.bookId == bookId) && (ref.chapter == chapter)) initialVerse else 1,
                readingType = if ((ref.bookId == bookId) && (ref.chapter == chapter)) readingType else null,
                selectedVerseId = selectedVerseId,
                onVerseClick = onVerseClick,
                onVersesLoaded = onVersesLoaded,
                providedVerses = currentVerses.ifEmpty { null },
                viewModel = viewModel,
                numberFormatter = numberFormatter,
                strings = strings,
                onNextChapter = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onPreviousChapter = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                nextBookName = nextBookName,
                onChapterChange = onChapterChange,
            )
        }
    }
}

@Composable
fun ChapterPage(
    bookId: Int,
    chapter: Int,
    initialVerse: Int,
    readingType: String?,
    selectedVerseId: Int?,
    onVerseClick: (Int) -> Unit,
    onVersesLoaded: (List<TargetReadingDetails>) -> Unit = {},
    providedVerses: List<TargetReadingDetails>? = null,
    viewModel: ReadingViewModel,
    numberFormatter: NumberFormat,
    strings: LocalizedStrings,
    onNextChapter: (() -> Unit)? = null,
    onPreviousChapter: (() -> Unit)? = null,
    nextBookName: String? = null,
    onChapterChange: (Int, Int, String?) -> Unit,
) {
    var verses by remember { mutableStateOf<List<TargetReadingDetails>>(emptyList()) }
    val listState = rememberLazyListState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()
    val isComplete by viewModel.isCurrentTranslationComplete.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val translations by viewModel.downloadedTranslations.collectAsState()

    val currentLang = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val status = downloadStatus[currentLang]

    val nextPortion = remember(readingType, bookId, chapter) {
        viewModel.getNextPortion(readingType)
    }

    LaunchedEffect(bookId, chapter, selectedCode, providedVerses) {
        verses = providedVerses ?: viewModel.getChapterVerses(bookId, chapter)
    }

    LaunchedEffect(verses) {
        if (verses.isNotEmpty()) {
            onVersesLoaded(verses)
        }
    }

    LaunchedEffect(verses, initialVerse) {
        if (verses.isNotEmpty() && (initialVerse > 0)) {
            val index = verses.indexOfFirst { it.verseId == initialVerse }
            if (index != -1) {
                listState.scrollToItem(index)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = AdaptiveDimens.contentMaxWidth),
            contentPadding = PaddingValues(
                horizontal = AdaptiveDimens.paddingMedium,
                vertical = AdaptiveDimens.paddingMedium,
            ),
        ) {
            if (verses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 64.dp), // Adjust for bottom bar
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!isComplete) {
                                AutoResizingText(
                                    text = strings.downloadRequired,
                                    fontSize = AdaptiveDimens.bodyFontSize,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.startBatchDownload(listOf(currentLang), force = true)
                                    },
                                    shape = RoundedCornerShape(26.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    ) // Using Home as placeholder for download icon
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AutoResizingText(strings.download, fontSize = AdaptiveDimens.smallFontSize)
                                }
                            } else if (status == LanguageStatus.FAILED) {
                                AutoResizingText(
                                    text = strings.failed,
                                    fontSize = AdaptiveDimens.bodyFontSize,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.startBatchDownload(listOf(currentLang), force = true)
                                    },
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                    ),
                                ) {
                                    AutoResizingText(strings.retry, fontSize = AdaptiveDimens.smallFontSize)
                                }
                            } else if (status == com.karol.readingsapp.feature.bible.data.LanguageStatus.DOWNLOADED) {
                                AutoResizingText(
                                    text = strings.contentNotFound,
                                    fontSize = AdaptiveDimens.bodyFontSize,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                AutoResizingText(
                                    text = strings.loadingReading,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    fontSize = AdaptiveDimens.bodyFontSize,
                                )
                            }
                        }
                    }
                }
            } else {
                items(verses, key = { "${it.translationCode}_${it.bookId}_${it.chapter}_${it.verseId}" }) { verse ->
                    val isSelected = verse.verseId == selectedVerseId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVerseClick(verse.verseId) }
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                    ) {
                        Text(
                            text = numberFormatter.format(verse.verseId),
                            fontSize = AdaptiveDimens.smallFontSize * 0.85f,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp, end = 8.dp),
                        )
                        Text(
                            text = verse.text,
                            fontSize = AdaptiveDimens.bodyFontSize,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = AdaptiveDimens.bodyFontSize * 1.5f,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AdaptiveDimens.paddingLarge, bottom = AdaptiveDimens.paddingLarge * 2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = AdaptiveDimens.paddingMedium),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            onPreviousChapter?.let { action ->
                                TextButton(
                                    onClick = action,
                                    colors = ButtonDefaults.textButtonColors(),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AutoResizingText(
                                        text = strings.back,
                                        fontSize = AdaptiveDimens.smallFontSize,
                                    )
                                }
                            } ?: Spacer(modifier = Modifier.width(1.dp))

                            onNextChapter?.let { action ->
                                val showNextPortion = nextPortion != null
                                Button(
                                    onClick = {
                                        if (showNextPortion) {
                                            viewModel.loadChapterVerses(nextPortion.bookId, nextPortion.chapter)
                                            onChapterChange(
                                                nextPortion.bookId,
                                                nextPortion.chapter,
                                                nextPortion.readingType,
                                            )
                                        } else {
                                            action()
                                        }
                                    },
                                    shape = RoundedCornerShape(26.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(),
                                ) {
                                    val nextPortionName = nextPortion?.let {
                                        strings.bookNames[it.bookId] ?: it.bookName
                                    }
                                    AutoResizingText(
                                        text = when {
                                            nextPortionName != null -> "${strings.nextPortion}: $nextPortionName"
                                            nextBookName != null -> "${strings.nextReading}: $nextBookName"
                                            else -> strings.nextReading
                                        },
                                        fontSize = AdaptiveDimens.smallFontSize,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
