package com.tnyx.features.onboarding.presentation.sections.intro

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.intro.steps.IntroExperienceModeStep
import com.tnyx.features.onboarding.presentation.sections.intro.steps.IntroWelcomeStep

@Composable
internal fun IntroSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.IntroWelcome -> IntroWelcomeStep(
            modifier = modifier,
        )

        OnboardingStepIds.IntroExperienceMode -> IntroExperienceModeStep(
            answer = answer,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}
