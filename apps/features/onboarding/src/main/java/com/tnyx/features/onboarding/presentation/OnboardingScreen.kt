package com.tnyx.features.onboarding.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxGhostButton
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.presentation.bodygoal.BodyGoalStepContent
import com.tnyx.features.onboarding.presentation.profile.ProfileStepContent
import com.tnyx.features.onboarding.presentation.workout.WorkoutStepContent

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = TnyxTheme.colors.background,
        topBar = {
            TnyxScreenHeader(
                title = "Set up Tio",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { onAction(OnboardingAction.BackClicked) },
                uppercaseTitle = false,
                reserveNavigationSpace = false,
            )
        },
    ) { contentPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TnyxTheme.colors.primary)
                }
            }

            state.position != null -> {
                OnboardingContent(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            else -> {
                OnboardingLoadError(
                    onRetry = { onAction(OnboardingAction.Retry) },
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}

@Composable
private fun OnboardingLoadError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = TnyxTheme.insets.screenHorizontal,
                vertical = TnyxTheme.insets.screenVertical,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Your onboarding progress could not be loaded.",
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.error,
        )
        TnyxPrimaryButton(
            text = "Retry",
            onPressed = onRetry,
            modifier = Modifier.padding(top = TnyxTheme.dimens.SpaceM),
        )
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val position = requireNotNull(state.position)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = TnyxTheme.insets.screenHorizontal,
                vertical = TnyxTheme.insets.screenVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        Text(
            text = "Section ${state.sectionNumber} of ${state.sectionCount}",
            style = TnyxTheme.typography.labelLarge,
            color = TnyxTheme.colors.textSecondary,
        )
        LinearProgressIndicator(
            progress = { state.completedFraction },
            modifier = Modifier.fillMaxWidth(),
            color = TnyxTheme.colors.primary,
            trackColor = TnyxTheme.colors.surfaceVariant,
        )
        Text(
            text = "Step ${state.stepNumber} of ${state.totalSteps}",
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textMuted,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
        ) {
            when (position.sectionId) {
                OnboardingSectionIds.Profile -> {
                    key(position.stepId.value) {
                        ProfileStepContent(
                            stepId = position.stepId,
                            answer = state.currentAnswer,
                            showValidationError =
                                state.validationError == OnboardingValidationError.RequiredAnswerInvalid,
                            onAnswerChanged = { answer ->
                                onAction(OnboardingAction.AnswerChanged(answer))
                            },
                        )
                    }
                }

                OnboardingSectionIds.BodyGoal -> {
                    key(position.stepId.value) {
                        BodyGoalStepContent(
                            stepId = position.stepId,
                            answer = state.currentAnswer,
                            showValidationError =
                                state.validationError == OnboardingValidationError.RequiredAnswerInvalid,
                            onAnswerChanged = { answer ->
                                onAction(OnboardingAction.AnswerChanged(answer))
                            },
                        )
                    }
                }

                OnboardingSectionIds.Workout -> {
                    key(position.stepId.value) {
                        WorkoutStepContent(
                            stepId = position.stepId,
                            answer = state.currentAnswer,
                            showValidationError =
                                state.validationError == OnboardingValidationError.RequiredAnswerInvalid,
                            onAnswerChanged = { answer ->
                                onAction(OnboardingAction.AnswerChanged(answer))
                            },
                        )
                    }
                }

                else -> {
                    Text(
                        text = position.stepId.value.toDisplayLabel(),
                        style = TnyxTheme.typography.headlineMedium,
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "The section-specific form will render in this container.",
                        style = TnyxTheme.typography.bodyLarge,
                        color = TnyxTheme.colors.textSecondary,
                    )
                    if (state.validationError == OnboardingValidationError.RequiredAnswerInvalid) {
                        Text(
                            text = "Complete this step with a valid answer before continuing.",
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.error,
                        )
                    }
                }
            }

            if (state.hasPersistenceError) {
                Text(
                    text = "Your progress could not be saved. Try again.",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.error,
                )
                TnyxGhostButton(
                    text = "Retry",
                    onPressed = { onAction(OnboardingAction.Retry) },
                )
            }
        }

        if (state.canSkipSection) {
            TnyxGhostButton(
                text = "Skip this section",
                onPressed = { onAction(OnboardingAction.SkipSectionClicked) },
                enabled = !state.isSaving,
                expand = true,
            )
        }
        TnyxPrimaryButton(
            text = when {
                state.isSaving -> "Saving..."
                state.isLastStep -> "Finish"
                else -> "Continue"
            },
            onPressed = { onAction(OnboardingAction.ContinueClicked) },
            enabled = !state.isSaving,
            expand = true,
        )
    }
}

private fun String.toDisplayLabel(): String {
    return substringAfter('.')
        .split('_')
        .joinToString(separator = " ") { word ->
            word.replaceFirstChar(Char::titlecase)
        }
}
