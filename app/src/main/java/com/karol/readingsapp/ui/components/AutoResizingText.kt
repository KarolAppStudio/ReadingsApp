package com.karol.readingsapp.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizingText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current,
    minFontSize: TextUnit = 10.sp,
    softWrap: Boolean = false,
) {
    var currentFontSize by remember(text) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        fontSize = currentFontSize,
        textAlign = textAlign,
        style = style,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        softWrap = softWrap,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (layoutResult.hasVisualOverflow && currentFontSize.value > minFontSize.value) {
                currentFontSize = (currentFontSize.value - 0.5f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}
