package com.tnyx.features.onboarding.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme

@Composable
internal fun OnboardingTopBar(
    showBackButton: Boolean,
    showProgressBar: Boolean,
    completedFraction: Float,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = TnyxTheme.insets.screenHorizontal)
            .height(TnyxTheme.dimens.ScreenHeaderHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = showBackButton) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TnyxTheme.colors.textPrimary,
                modifier = Modifier
                    .size(TnyxTheme.dimens.IconM)
                    .clickable(onClick = onBackClick),
            )
        }

        if (showBackButton && showProgressBar) {
            Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceXS))
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = showProgressBar) {
                OnboardingProgress(
                    completedFraction = completedFraction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
