package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkipOnboardingSectionUseCaseTest {
    private val useCase = SkipOnboardingSectionUseCase()

    @Test
    fun returnsNullForNonSkippableSection() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.Profile,
                stepId = OnboardingStepIds.ProfileName,
            ),
            DefaultOnboardingFlow.definition,
        )

        assertNull(result)
    }

    @Test
    fun jumpsToNextSectionWhenCurrentSectionIsSkippable() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutExperience,
            ),
            DefaultOnboardingFlow.definition,
        )

        assertEquals(OnboardingStepIds.TargetsStepsTarget, result?.progress?.position?.stepId)
    }

    private fun checkpoint(
        sectionId: com.tnyx.features.onboarding.domain.model.OnboardingSectionId,
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = sectionId,
                    stepId = stepId,
                ),
            ),
        )
    }
}
