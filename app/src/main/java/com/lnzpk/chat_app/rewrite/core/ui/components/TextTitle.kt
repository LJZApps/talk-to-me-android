package com.lnzpk.chat_app.rewrite.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun TextTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        ),
        modifier = modifier
    )
}