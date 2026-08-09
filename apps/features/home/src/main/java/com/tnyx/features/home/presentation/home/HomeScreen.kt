package com.tnyx.features.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.shell.domain.model.HomeExperienceMode

@Composable
fun HomeScreen(
    mode: HomeExperienceMode,
    modifier: Modifier = Modifier,
) {
    val focus = when (mode) {
        HomeExperienceMode.Nutrition -> "Nutrition-focused summary"
        HomeExperienceMode.Workout -> "Workout-focused summary"
        HomeExperienceMode.Balanced -> "Balanced coaching summary"
        HomeExperienceMode.Custom -> "Custom summary"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Home",
                fontWeight = FontWeight.Bold,
            )
            Text(text = "$focus. Detailed actions remain inside their owning tabs.")
        }
    }
}
