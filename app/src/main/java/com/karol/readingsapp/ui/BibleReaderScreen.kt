package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.TextBlue

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    bookId: Int,
    chapter: Int,
    viewModel: ReadingViewModel,
    onBackClick: () -> Unit,
) {
    val verses = viewModel.getVersesByBookId(bookId, chapter)
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()

    val selectedLanguage = remember(selectedCode, translations) {
        translations.find { it.code == selectedCode }?.language ?: "English"
    }
    val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }
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
                            tint = TextBlue,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$bookName ${numberFormatter.format(chapter)}",
                            color = TextBlue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                navigationIcon = {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        IconButton(onClick = { /* Handle menu click */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextBlue)
                        }
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlue,
                ),
                modifier = Modifier.height(112.dp),
            )
        },
        containerColor = BackgroundBlue,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            if (verses.isEmpty()) {
                item {
                    Text(
                        text = "Reading content will appear here...",
                        color = TextBlue.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                    )
                }
            } else {
                items(verses) { verse ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = numberFormatter.format(verse.verseId),
                            fontSize = 12.sp,
                            color = TextBlue.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp, end = 8.dp),
                        )
                        Text(
                            text = verse.text,
                            fontSize = 16.sp,
                            color = TextBlue,
                            lineHeight = 24.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
