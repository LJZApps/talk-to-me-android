package com.lnzpk.chat_app.rewrite.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelect(
    modifier: Modifier = Modifier,
    title: String = "TITLE",
    onClick: () -> Unit
) {
    ElevatedCard (
        modifier = modifier,
        onClick = onClick
    ) {
        Column (
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold
                ),
            )
        }
    }
}