package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.TargetReadingDetails
import com.karol.readingsapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReadingViewModel,
    onReadingClick: (TargetReadingDetails) -> Unit,
) {
    val readingsGrouped by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadReading("2026-06-12") // To match the screenshot date
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Reading Companion",
                        color = TextBlue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu click */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlue,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
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
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Bible") },
                    label = { Text("Bible") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
            }
        },
        containerColor = BackgroundBlue,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = TextBlue,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        "Today's Readings",
                        color = TextBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        "Friday, 12 June 2026",
                        color = DateGrey,
                        fontSize = 14.sp,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            val types = listOf("First Reading", "Second Reading", "Third Reading")
            items(types) { type ->
                ReadingSection(
                    title = type,
                    items = readingsGrouped[type] ?: emptyList(),
                    onItemClick = onReadingClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReadingSection(
    title: String,
    items: List<TargetReadingDetails>,
    onItemClick: (TargetReadingDetails) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLavender),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                title,
                color = TextBlue.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (items.isEmpty()) {
                // For demonstration, if no items, show a placeholder matching the screenshot
                when (title) {
                    "First Reading" -> {
                        ReadingItemPlaceholder("Judges 10") {
                            onItemClick(TargetReadingDetails("", "Judges", 10, "Placeholder text", title))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ReadingItemPlaceholder("Judges 11") {
                            onItemClick(TargetReadingDetails("", "Judges", 11, "Placeholder text", title))
                        }
                    }
                    "Second Reading" -> {
                        ReadingItemPlaceholder("Isaiah 36") {
                            onItemClick(TargetReadingDetails("", "Isaiah", 36, "Placeholder text", title))
                        }
                    }
                    "Third Reading" -> {
                        ReadingItemPlaceholder("1 Peter 2") {
                            onItemClick(TargetReadingDetails("", "1 Peter", 2, "Placeholder text", title))
                        }
                    }
                }
            } else {
                // Group by book and chapter to avoid repeating for each verse
                val distinctReadings = items.distinctBy { "${it.bookName} ${it.chapter}" }
                distinctReadings.forEachIndexed { index, item ->
                    ReadingItemRow(item) { onItemClick(item) }
                    if (index < (distinctReadings.size - 1)) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingItemRow(item: TargetReadingDetails, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = "${item.bookName} ${item.chapter}",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
    }
}

@Composable
fun ReadingItemPlaceholder(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
    }
}
