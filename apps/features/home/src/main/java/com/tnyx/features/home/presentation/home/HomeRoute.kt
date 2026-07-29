package com.tnyx.features.home.presentation.home

import androidx.compose.runtime.Composable
import com.tnyx.core.ui.shell.domain.model.HomeExperienceMode

@Composable
fun HomeRoute(
    mode: HomeExperienceMode,
) {
    HomeScreen(mode = mode)
}
