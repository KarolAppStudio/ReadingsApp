package com.karol.readingsapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.TextBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onHomeClick: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                        imageVector = Icons.Default.Info,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Readings App",
                style = MaterialTheme.typography.headlineMedium,
                color = TextBlue,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = TextBlue.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "A simple app to help you follow your daily Bible reading plan with multiple translations support.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = TextBlue,
                lineHeight = 24.sp,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "© 2026 Karol",
                style = MaterialTheme.typography.labelMedium,
                color = TextBlue.copy(alpha = 0.5f),
            )
        }
    }
}
