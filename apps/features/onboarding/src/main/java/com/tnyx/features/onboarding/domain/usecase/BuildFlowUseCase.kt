package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.containsPosition
import com.tnyx.features.onboarding.domain.flow.effectiveSections
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionDefinition

class BuildFlowUseCase {
    operator fun invoke(
        flow: OnboardingFlowDefinition,
        checkpoint: OnboardingCheckpoint,
    ): List<OnboardingSectionDefinition> {
        return flow.effectiveSections(checkpoint)
    }

    fun contains(
        flow: OnboardingFlowDefinition,
        checkpoint: OnboardingCheckpoint,
        position: OnboardingPosition,
    ): Boolean {
        return invoke(flow, checkpoint).containsPosition(position)
    }
}
