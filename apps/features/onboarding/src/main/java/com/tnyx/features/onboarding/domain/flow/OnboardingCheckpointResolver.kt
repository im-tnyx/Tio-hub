package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress

class OnboardingCheckpointResolver {
    fun resolve(
        checkpoint: OnboardingCheckpoint?,
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        return checkpoint
            ?.takeIf { stored -> stored.isCompatibleWith(flow) }
            ?: freshCheckpoint(flow)
    }

    fun freshCheckpoint(flow: OnboardingFlowDefinition): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            progress = OnboardingProgress(
                flowVersion = flow.version,
                position = flow.firstPosition(),
            ),
        )
    }

    private fun OnboardingCheckpoint.isCompatibleWith(
        flow: OnboardingFlowDefinition,
    ): Boolean {
        if (progress.flowVersion != flow.version || !flow.contains(progress.position)) {
            return false
        }

        val sectionIds = flow.sections.map { section -> section.id }.toSet()
        if (!sectionIds.containsAll(progress.completedSectionIds)) {
            return false
        }

        val stepIds = flow.sections
            .flatMap { section -> section.steps }
            .map { step -> step.id }
            .toSet()
        return stepIds.containsAll(draft.answers.keys)
    }
}
