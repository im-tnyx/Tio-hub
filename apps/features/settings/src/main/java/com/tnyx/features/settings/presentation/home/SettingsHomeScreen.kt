package com.tnyx.features.settings.presentation.home

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
fun SettingsHomeScreen(
    uiState: SettingsHomeUiState,
    onAction: (SettingsHomeAction) -> Unit,
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
        Text("Theme")
        Text("Language")
        Text("Notifications")
        Text("Units")
        Button(onClick = { onAction(SettingsHomeAction.BackClicked) }) {
            Text("Back")
        }
    }
}
