package com.karol.readingsapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.data.bible.TranslationEntity

@Composable
fun TranslationSelector(
    selectedTranslationCode: String,
    translations: List<TranslationEntity>,
    onTranslationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPleasant: Boolean = false,
) {
    var expanded by remember { mutableStateOf(value = false) }
    val transName = translations.find { it.code == selectedTranslationCode }?.name ?: placeholder

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            color = if (isPleasant) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer,
            shape = if (isPleasant) RoundedCornerShape(12.dp) else RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = transName,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 36.dp),
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
    isPleasant: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    height: androidx.compose.ui.unit.Dp = 20.dp,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            shape = if (isPleasant) RoundedCornerShape(6.dp) else RoundedCornerShape(2.dp),
        ) {
            AutoResizingText(
                text = text,
                fontSize = fontSize,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, height),
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
