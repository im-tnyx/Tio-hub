package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateOnboardingAnswerUseCaseTest {
    private val useCase = UpdateOnboardingAnswerUseCase()

    @Test
    fun writesAnswerIntoCurrentStepDraft() {
        val checkpoint = checkpoint()

        val updated = useCase(
            checkpoint = checkpoint,
            answer = OnboardingAnswer.Text("Santosh"),
        )

        assertEquals(
            OnboardingAnswer.Text("Santosh"),
            updated.draft.answerFor(OnboardingStepIds.ProfileName),
        )
    }

    @Test
    fun removesAnswerWhenNullIsProvided() {
        val checkpoint = checkpoint(
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.ProfileName,
                OnboardingAnswer.Text("Santosh"),
            ),
        )

        val updated = useCase(
            checkpoint = checkpoint,
            answer = null,
        )

        assertNull(updated.draft.answerFor(OnboardingStepIds.ProfileName))
    }

    private fun checkpoint(
        draft: OnboardingDraft = OnboardingDraft(),
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = draft,
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Profile,
                    stepId = OnboardingStepIds.ProfileName,
                ),
            ),
        )
    }
}
