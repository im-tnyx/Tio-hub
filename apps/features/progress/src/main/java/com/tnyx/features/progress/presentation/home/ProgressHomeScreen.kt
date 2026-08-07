package com.tnyx.features.progress.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader

@Composable
fun ProgressHomeScreen(
    uiState: ProgressHomeUiState,
    onAction: (ProgressHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
    ) {
        TnyxScreenHeader(
            title = "Progress",
            size = TnyxHeaderSize.Compact,
            uppercaseTitle = false,
        )

        // Progress content layout container
    }
}
