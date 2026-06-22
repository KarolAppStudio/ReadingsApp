package com.karol.readingsapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.ui.components.AutoResizingText
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.CardLavender
import com.karol.readingsapp.ui.theme.TextBlue

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
            listState1.firstVisibleItemScrollOffset
        )
    }

    val selectedLanguage = remember(selectedCode1, translations) {
        translations.find { it.code == selectedCode1 }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
    val bookName = strings.bookNames[bookId] ?: "Book $bookId"

    val numberFormatter = remember(strings.locale) {
        java.text.NumberFormat.getIntegerInstance(strings.locale)
    }

    LaunchedEffect(bookId, chapter, selectedCode1) {
        viewModel.loadChapterVerses(bookId, chapter)
    }

    LaunchedEffect(bookId, chapter, selectedCode2) {
        viewModel.loadSecondChapterVerses(bookId, chapter, selectedCode2)
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    AutoResizingText(
                        text = "$bookName $chapter",
                        color = TextBlue,
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
                            tint = TextBlue
                        )
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { isSyncEnabled = !isSyncEnabled }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            if (isSyncEnabled) {
                                Canvas(modifier = Modifier.size(32.dp)) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF00FF00).copy(alpha = 0.4f),
                                                Color.Transparent
                                            ),
                                            center = center,
                                            radius = size.width / 2
                                        )
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isSyncEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = strings.sync,
                                tint = if (isSyncEnabled) Color(0xFF00FF00) else TextBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Lock Grids",
                            color = TextBlue,
                            fontSize = 10.sp
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                viewModel.resetParallelReading(bookId, chapter)
                                scope.launch {
                                    listState1.animateScrollToItem(0)
                                    listState2.animateScrollToItem(0)
                                }
                            }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = strings.reset,
                                tint = TextBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Reset",
                            color = TextBlue,
                            fontSize = 10.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlue),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = BackgroundBlue,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selectors Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TranslationSelector(
                    selectedTranslationCode = selectedCode1,
                    translations = translations,
                    onTranslationSelected = { viewModel.setTranslation(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = strings.selectBible
                )
                TranslationSelector(
                    selectedTranslationCode = selectedCode2,
                    translations = translations,
                    onTranslationSelected = { viewModel.loadSecondChapterVerses(bookId, chapter, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = strings.selectBible
                )
            }

            // Content Row
            if (isSyncEnabled) {
                val maxVerses = maxOf(verses1.size, verses2.size)
                LazyColumn(
                    state = listState1,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(maxVerses) { index ->
                        val v1 = verses1.getOrNull(index)
                        val v2 = verses2.getOrNull(index)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                if (v1 != null) VerseItem(v1, numberFormatter)
                            }
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                thickness = 1.dp,
                                color = TextBlue.copy(alpha = 0.2f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                if (v2 != null) VerseItem(v2, numberFormatter)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // First Column
                    LazyColumn(
                        state = listState1,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp)
                    ) {
                        items(verses1) { verse ->
                            VerseItem(verse, numberFormatter)
                        }
                    }

                    // Divider
                    VerticalDivider(thickness = 1.dp, color = TextBlue.copy(alpha = 0.2f))

                    // Second Column
                    LazyColumn(
                        state = listState2,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp)
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
fun TranslationSelector(
    selectedTranslationCode: String,
    translations: List<TranslationEntity>,
    onTranslationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(value = false) }
    val transName = translations.find { it.code == selectedTranslationCode }?.name ?: placeholder

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            color = CardLavender,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transName,
                    color = TextBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            translations.forEach { translation ->
                DropdownMenuItem(
                    text = { Text(translation.name) },
                    onClick = {
                        onTranslationSelected(translation.code)
                        expanded = false
                    }
                )
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
            color = TextBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = verse.text,
            fontSize = 14.sp,
            color = TextBlue,
            lineHeight = 20.sp
        )
    }
}
