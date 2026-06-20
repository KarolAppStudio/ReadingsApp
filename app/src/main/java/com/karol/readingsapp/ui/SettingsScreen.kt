package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun SettingsScreen(
    viewModel: ReadingViewModel,
    onHomeClick: () -> Unit,
) {
    val translations by viewModel.availableTranslations.collectAsState()
    val selectedCode by viewModel.selectedTranslationCode.collectAsState()

    var stagedSelection by remember(selectedCode) { mutableStateOf(selectedCode) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                color = BackgroundBlue,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        onClick = onHomeClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = TextBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = TextBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        },
        containerColor = BackgroundBlue,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Bible Translation",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val selectedName = translations.find { it.code == stagedSelection }?.name ?: "Select Bible"
                            var expanded by remember { mutableStateOf(value = false) }

                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    onClick = { expanded = true },
                                    color = CardLavender,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = selectedName,
                                            color = TextBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TextBlue,
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    translations.forEach { translation ->
                                        DropdownMenuItem(
                                            text = { Text(translation.name) },
                                            onClick = {
                                                stagedSelection = translation.code
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.setTranslation(stagedSelection) },
                                colors = ButtonDefaults.buttonColors(containerColor = TextBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Save Configuration", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
