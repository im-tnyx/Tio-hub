package com.tnyx.wear.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator

import com.tnyx.wear.theme.CardBackground

@Composable
fun CircularBezelProgress(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background ring path
        CircularProgressIndicator(
            progress = 1.0f,
            modifier = Modifier.fillMaxSize().padding(6.dp),
            indicatorColor = CardBackground,
            strokeWidth = 5.dp
        )
        
        // Front active progress ring
        CircularProgressIndicator(
            progress = progress.coerceIn(0.0f, 1.0f),
            modifier = Modifier.fillMaxSize().padding(6.dp),
            indicatorColor = ringColor,
            strokeWidth = 5.dp
        )
        
        content()
    }
}
