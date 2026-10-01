package com.kuzulabz.waifutaggercn.ui.components

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DynamicPromptBox(text: String) {
    val lines = text.count { it == ',' } + 1
    Text(
        text = text,
        modifier = Modifier
            .heightIn(min = 56.dp, max = (80 + lines * 8).coerceAtMost(420).dp)
            .verticalScroll(rememberScrollState())
    )
}
