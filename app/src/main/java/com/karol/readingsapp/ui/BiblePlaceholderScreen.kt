package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.CardLavender
import com.karol.readingsapp.ui.theme.TextBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiblePlaceholderScreen(
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                color = BackgroundBlue,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    IconButton(
                        onClick = onHomeClick,
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = TextBlue,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = TextBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onHomeClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    selected = false,
                    onClick = onCalendarClick,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextBlue,
                        unselectedTextColor = TextBlue,
                    ),
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Bible") },
                    label = { Text("Bible") },
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
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextBlue.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Bible Reader Coming Soon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlue
                )
            }
        }
    }
}
