package com.tnyx.features.progress.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressHomeScreen(
    uiState: ProgressHomeUiState,
    onAction: (ProgressHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = uiState.title)
        Text(text = uiState.subtitle)
        Button(onClick = { onAction(ProgressHomeAction.JourneyClicked) }) {
            Text("Journey")
        }
        Button(onClick = { onAction(ProgressHomeAction.PhotosClicked) }) {
            Text("Progress Photos")
        }
    }
}
