package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxGhostButton
import com.tnyx.features.onboarding.presentation.component.OnboardingBottomBar
import com.tnyx.features.onboarding.presentation.component.OnboardingTopBar

@Composable
internal fun OnboardingShellScreen(
    isLoading: Boolean,
    completedFraction: Float,
    hasPersistenceError: Boolean,
    showBackButton: Boolean,
    showProgressBar: Boolean,
    showContinueButton: Boolean,
    continueButtonText: String,
    isContinueEnabled: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = TnyxTheme.colors.primary)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OnboardingTopBar(
                showBackButton = showBackButton,
                showProgressBar = showProgressBar,
                completedFraction = completedFraction,
                onBackClick = onBack,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            start = TnyxTheme.insets.screenHorizontal,
                            end = TnyxTheme.insets.screenHorizontal,
                            top = TnyxTheme.insets.screenVertical,
                            bottom = TnyxTheme.dimens.SpaceXL,
                        ),
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
                ) {
                    content()

                    if (hasPersistenceError) {
                        androidx.compose.material3.Text(
                            text = "Your progress could not be saved. Try again.",
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.error,
                        )
                        TnyxGhostButton(
                            text = "Retry",
                            onPressed = onRetry,
                        )
                    }
                }
            }

            OnboardingBottomBar(
                visible = showContinueButton,
                text = continueButtonText,
                enabled = isContinueEnabled,
                onClick = onContinue,
            )
        }
    }
}
