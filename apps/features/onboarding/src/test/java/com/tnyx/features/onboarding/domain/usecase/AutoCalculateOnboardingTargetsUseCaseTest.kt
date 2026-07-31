package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import org.junit.Assert.assertEquals
import org.junit.Test

class AutoCalculateOnboardingTargetsUseCaseTest {
    private val useCase = AutoCalculateOnboardingTargetsUseCase()

    @Test
    fun derivesStableTargetSnapshotFromBodyGoalAnswers() {
        val snapshot = useCase(
            OnboardingDraft()
                .withAnswer(OnboardingStepIds.BodyGoalPrimaryGoal, OnboardingAnswer.Text("lose_weight"))
                .withAnswer(OnboardingStepIds.BodyGoalActivityLevel, OnboardingAnswer.Text("active"))
                .withAnswer(OnboardingStepIds.BodyGoalCurrentWeight, OnboardingAnswer.Decimal(80.0)),
        )

        assertEquals(11000, snapshot.stepsTarget)
        assertEquals("balanced_evenings", snapshot.sleepTargetId)
        assertEquals(2750, snapshot.waterTargetMl)
        assertEquals(2420, snapshot.caloriesTarget)
        assertEquals(144, snapshot.proteinTargetGrams)
        assertEquals(67, snapshot.fatTargetGrams)
        assertEquals(310, snapshot.carbsTargetGrams)
        assertEquals(32, snapshot.fiberTargetGrams)
        assertEquals(0.5, snapshot.goalPaceKgPerWeek, 0.0)
    }
}
