package com.tnyx.features.profile.presentation.home

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
fun ProfileHomeScreen(
    uiState: ProfileHomeUiState,
    onAction: (ProfileHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = uiState.displayName)
        Text(text = uiState.subtitle)
        Text(text = "Current plan: ${uiState.planLabel}")
        Button(onClick = { onAction(ProfileHomeAction.JourneyClicked) }) {
            Text("Journey")
        }
        Button(onClick = { onAction(ProfileHomeAction.ProgressPhotosClicked) }) {
            Text("Progress Photos")
        }
        Button(onClick = { onAction(ProfileHomeAction.SettingsClicked) }) {
            Text("Settings")
        }
    }
}
