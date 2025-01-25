package com.example.diarys22387.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun DiarysTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        // primary, secondary, ...
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
