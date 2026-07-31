package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.containsPosition
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import javax.inject.Inject

class AlignOnboardingCheckpointUseCase @Inject constructor(
    private val buildFlowUseCase: BuildFlowUseCase,
) {
    constructor() : this(buildFlowUseCase = BuildFlowUseCase())

    operator fun invoke(
        checkpoint: OnboardingCheckpoint,
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val effectiveSections = buildFlowUseCase(flow, checkpoint)
        val currentPosition = checkpoint.progress.position
        if (effectiveSections.containsPosition(currentPosition)) {
            return checkpoint
        }

        var candidate = flow.next(currentPosition)
        while (candidate != null && !effectiveSections.containsPosition(candidate)) {
            candidate = flow.next(candidate)
        }

        val alignedPosition = candidate ?: effectiveSections.firstOrNull()
            ?.steps
            ?.firstOrNull()
            ?.let { step ->
                com.tnyx.features.onboarding.domain.model.OnboardingPosition(
                    sectionId = effectiveSections.first().id,
                    stepId = step.id,
                )
            }
            ?: currentPosition

        return checkpoint.copy(
            progress = checkpoint.progress.copy(position = alignedPosition),
        )
    }
}
