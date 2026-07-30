package com.tnyx.features.onboarding.presentation.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxGhostButton

@Composable
internal fun OnboardingShellScreen(
    isLoading: Boolean,
    completedFraction: Float,
    isSaving: Boolean,
    isLastStep: Boolean,
    hasPersistenceError: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = TnyxTheme.colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TnyxTheme.insets.screenHorizontal - TnyxTheme.dimens.SpaceXS,
                        end = TnyxTheme.insets.screenHorizontal,
                        top = TnyxTheme.insets.screenVertical,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
                OnboardingProgressBar(
                    completedFraction = completedFraction,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { contentPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TnyxTheme.colors.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(
                    horizontal = TnyxTheme.insets.screenHorizontal,
                    vertical = TnyxTheme.insets.screenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
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

            OnboardingFooter(
                isSaving = isSaving,
                isLastStep = isLastStep,
                onContinue = onContinue,
            )
        }
    }
}
