package de.ljz.talktome.rewrite.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun TextDescription(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = 16.sp
    ),
    textAlign = TextAlign.Left,
    modifier = modifier
  )
}