package com.karol.readingsapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.ui.theme.glassEffect

@Composable
fun AnimatedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    backgroundColor: Color = Color.White,
    isGlass: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val expandedState = remember { MutableTransitionState(initialState = false) }
    expandedState.targetState = expanded

    if (expandedState.currentState || expandedState.targetState) {
        val density = LocalDensity.current
        Popup(
            onDismissRequest = onDismissRequest,
            offset = IntOffset(
                with(density) { offset.x.roundToPx() },
                with(density) { offset.y.roundToPx() },
            ),
            properties = PopupProperties(focusable = true),
        ) {
            AnimatedVisibility(
                visibleState = expandedState,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGlass) Color.DarkGray.copy(alpha = 0.9f) else backgroundColor,
                    contentColor = if (isGlass) Color.White else (if (backgroundColor == Color.White) Color.Black else contentColorFor(backgroundColor)),
                    tonalElevation = if (isGlass) 0.dp else 3.dp,
                    shadowElevation = if (isGlass) 0.dp else 8.dp,
                    modifier = modifier.then(if (isGlass) Modifier.glassEffect() else Modifier),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        content = content,
                    )
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
    isGlass: Boolean = false,
    placeholder: String = "",
) {
    var expanded by remember { mutableStateOf(value = false) }
    val transName = translations.find { it.code == selectedTranslationCode }?.name ?: placeholder

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().then(if (isGlass) Modifier.glassEffect() else Modifier),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = transName,
                    color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isGlass) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        AnimatedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 36.dp),
            backgroundColor = Color.White,
            isGlass = isGlass,
            modifier = Modifier
                .heightIn(max = 500.dp)
                .widthIn(min = 1.dp)
                .width(IntrinsicSize.Min),
        ) {
            translations.forEach { translation ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = translation.name,
                            fontSize = 12.sp,
                            color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                            softWrap = false,
                        )
                    },
                    onClick = {
                        onTranslationSelected(translation.code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun SelectionButton(
    text: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isGlass: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    height: androidx.compose.ui.unit.Dp = 20.dp,
    cornerRadius: androidx.compose.ui.unit.Dp? = null,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(if (isGlass) Modifier.glassEffect() else Modifier),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            colors = if (isGlass) {
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
            shape = cornerRadius?.let { RoundedCornerShape(it) }
                ?: RoundedCornerShape(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AutoResizingText(
                    text = text,
                    fontSize = fontSize,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isGlass) Color.White else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size((fontSize.value * 1.4f).dp),
                )
            }
        }
        AnimatedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, height),
            isGlass = isGlass,
            modifier = Modifier
                .heightIn(max = 500.dp)
                .widthIn(min = 1.dp)
                .width(IntrinsicSize.Min),
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            color = if (isGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            softWrap = false,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.heightIn(min = 32.dp),
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}
