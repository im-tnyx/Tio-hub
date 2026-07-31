package com.tnyx.features.onboarding.presentation.container

import com.tnyx.features.onboarding.domain.usecase.ValidateOnboardingAnswerUseCase
import com.tnyx.features.onboarding.domain.validator.StepValidator
import com.tnyx.features.onboarding.presentation.OnboardingUiState
import javax.inject.Inject

internal class OnboardingContainerStateFactory @Inject constructor(
    private val stepValidator: StepValidator,
) {
    constructor() : this(stepValidator = StepValidator(ValidateOnboardingAnswerUseCase()))

    operator fun invoke(state: OnboardingUiState): OnboardingContainerState {
        return OnboardingContainerState(
            showButton = stepValidator.shouldShowButton(state),
            buttonText = stepValidator.buttonText(state),
            isButtonEnabled = stepValidator.isStepValid(state),
            showBackButton = stepValidator.shouldShowBackButton(state.position),
            showProgressBar = stepValidator.shouldShowProgressBar(state.position),
        )
    }
}
