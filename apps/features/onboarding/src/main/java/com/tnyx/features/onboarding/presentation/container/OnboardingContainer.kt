package com.tnyx.features.onboarding.presentation.container

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.presentation.OnboardingAction
import com.tnyx.features.onboarding.presentation.OnboardingUiState
import com.tnyx.features.onboarding.presentation.completion.OnboardingCompletionScreen
import com.tnyx.features.onboarding.presentation.sections.OnboardingSectionContent
import com.tnyx.features.onboarding.presentation.shell.OnboardingErrorState
import com.tnyx.features.onboarding.presentation.shell.OnboardingShellScreen

@Composable
internal fun OnboardingContainer(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    containerStateFactory: OnboardingContainerStateFactory = OnboardingContainerStateFactory(),
) {
    val position = state.position
    val containerState = containerStateFactory(state)

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
        hasPersistenceError = state.hasPersistenceError,
        showBackButton = containerState.showBackButton,
        showProgressBar = containerState.showProgressBar,
        showContinueButton = containerState.showButton,
        continueButtonText = containerState.buttonText,
        isContinueEnabled = containerState.isButtonEnabled,
        onBack = { onAction(OnboardingAction.BackClicked) },
        onRetry = { onAction(OnboardingAction.Retry) },
        onContinue = { onAction(OnboardingAction.ContinueClicked) },
        modifier = modifier,
    ) {
        if (position != null) {
            AnimatedContent(
                targetState = position,
                label = "onboarding_position_content",
            ) { currentPosition ->
                OnboardingSectionContent(
                    position = currentPosition,
                    currentAnswer = state.currentAnswer,
                    draftAnswers = state.draftAnswers,
                    validationError = state.validationError,
                    onAction = onAction,
                )
            }
        }
    }
}
