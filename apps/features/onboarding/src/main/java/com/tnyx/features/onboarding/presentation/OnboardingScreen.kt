package com.tnyx.features.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.presentation.completion.OnboardingCompletionScreen
import com.tnyx.features.onboarding.presentation.sections.OnboardingSectionContent
import com.tnyx.features.onboarding.presentation.shell.OnboardingErrorState
import com.tnyx.features.onboarding.presentation.shell.OnboardingShellScreen

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val position = state.position

    state.completionStage?.let { completionStage ->
        OnboardingCompletionScreen(
            stage = completionStage,
            onContinue = { onAction(OnboardingAction.ContinueClicked) },
            modifier = modifier,
        )
        return
    }

    if (position == null && !state.isLoading) {
        OnboardingErrorState(
            message = "Your onboarding progress could not be loaded.",
            onRetry = { onAction(OnboardingAction.Retry) },
            modifier = modifier,
        )
        return
    }

    OnboardingShellScreen(
        isLoading = state.isLoading,
        completedFraction = state.completedFraction,
        isSaving = state.isSaving,
        isLastStep = state.isLastStep,
        hasPersistenceError = state.hasPersistenceError,
        onBack = { onAction(OnboardingAction.BackClicked) },
        onRetry = { onAction(OnboardingAction.Retry) },
        onContinue = { onAction(OnboardingAction.ContinueClicked) },
        modifier = modifier,
    ) {
        if (position != null) {
            OnboardingSectionContent(
                position = position,
                currentAnswer = state.currentAnswer,
                draftAnswers = state.draftAnswers,
                validationError = state.validationError,
                onAction = onAction,
            )
        }
    }
}
