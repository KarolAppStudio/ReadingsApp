package com.karol.readingsapp.feature.contact.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens
import com.karol.readingsapp.core.ui.components.AutoResizingText

@Composable
fun ContactScreen(strings: LocalizedStrings, onHomeClick: () -> Unit) {
    var showFeedbackDialog by remember { mutableStateOf(value = false) }
    var showMessageSentPopup by remember { mutableStateOf(value = false) }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
        ) {
            showFeedbackDialog = false
            showMessageSentPopup = true
        }
    }

    if (showMessageSentPopup) {
        AlertDialog(
            onDismissRequest = { showMessageSentPopup = false },
            confirmButton = {
                TextButton(onClick = { showMessageSentPopup = false }) {
                    AutoResizingText("OK", fontSize = AdaptiveDimens.smallFontSize)
                }
            },
            title = { AutoResizingText("Success", fontSize = AdaptiveDimens.bodyFontSize) },
            text = { AutoResizingText("Message Sent", fontSize = AdaptiveDimens.smallFontSize) },
        )
    }

    Scaffold(
        topBar = {
            ContactTopBar(strings = strings, onHomeClick = onHomeClick)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = AdaptiveDimens.contentMaxWidth)
                        .padding(AdaptiveDimens.paddingLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(AdaptiveDimens.paddingMedium),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        AutoResizingText(
                            text = strings.contact,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = AdaptiveDimens.bodyFontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = "We’d love to hear from you! Send us your questions, suggestions, or feedback.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = AdaptiveDimens.smallFontSize,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            textAlign = TextAlign.Start,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showFeedbackDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            AutoResizingText("Click Here", fontSize = AdaptiveDimens.bodyFontSize)
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AdaptiveDimens.paddingMedium),
            ) {
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = AdaptiveDimens.smallFontSize,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = strings.developedBy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun ContactTopBar(strings: LocalizedStrings, onHomeClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(40.dp),
        color = MaterialTheme.colorScheme.background,
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
                    contentDescription = strings.home,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 40.dp else 30.dp),
                )
            }

            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
            )
        }
    }
}

@Composable
fun FeedbackDialog(onDismiss: () -> Unit, onSent: () -> Unit) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val subjects = listOf("Feedback", "Suggestion", "Report an Issue", "Request a feature")
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AutoResizingText(
                    text = "Feedback",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = AdaptiveDimens.titleFontSize,
                    fontWeight = FontWeight.Bold,
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { AutoResizingText("Select Subject", fontSize = AdaptiveDimens.smallFontSize) },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true },
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.6f),
                    ) {
                        subjects.forEach { item ->
                            DropdownMenuItem(
                                text = { AutoResizingText(item, fontSize = AdaptiveDimens.smallFontSize) },
                                onClick = {
                                    subject = item
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                if (subject.isNotEmpty()) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = {
                            AutoResizingText("Type your message here...", fontSize = AdaptiveDimens.smallFontSize)
                        },
                        shape = RectangleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:justkarol@icloud.com".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                                onSent()
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                    ) {
                        AutoResizingText("Send", fontSize = AdaptiveDimens.bodyFontSize)
                    }
                }
            }
        }
    }
}
