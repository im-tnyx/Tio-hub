package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingUiStateFactoryTest {
    private val factory = OnboardingUiStateFactory()

    @Test
    fun buildsStateForRequiredStepWithValidatedAnswer() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Intro,
            stepId = OnboardingStepIds.IntroWelcome,
        )

        val state = factory(checkpoint, DefaultOnboardingFlow.definition)

        assertEquals(OnboardingStepIds.IntroWelcome, state.position?.stepId)
        assertTrue(state.canContinue)
        assertFalse(state.canSkipSection)
        assertEquals(1, state.sectionNumber)
        assertEquals(1, state.stepNumber)
    }

    @Test
    fun buildsStateForOptionalWorkoutStepWithoutAnswer() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutEquipment,
        )

        val state = factory(checkpoint, DefaultOnboardingFlow.definition, hasPersistenceError = true)

        assertTrue(state.canContinue)
        assertTrue(state.canSkipSection)
        assertTrue(state.hasPersistenceError)
        assertEquals(6, state.sectionNumber)
        assertEquals(18, state.stepNumber)
    }

    @Test
    fun removesWorkoutFromEffectiveProgressWhenWorkoutWasDeclined() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Targets,
            stepId = OnboardingStepIds.TargetsStepsTarget,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.WorkoutIntroChoice,
                OnboardingAnswer.Toggle(false),
            ),
        )

        val state = factory(checkpoint, DefaultOnboardingFlow.definition)

        assertEquals(6, state.sectionNumber)
        assertEquals(8, state.sectionCount)
        assertEquals(14, state.stepNumber)
        assertEquals(22, state.totalSteps)
        assertEquals(14f / 22f, state.completedFraction, 0.0001f)
    }

    @Test
    fun keepsSplitInVisibleProgressWhenWorkoutPathIsActive() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutSplit,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.WorkoutIntroChoice,
                OnboardingAnswer.Toggle(true),
            ),
        )

        val state = factory(checkpoint, DefaultOnboardingFlow.definition)

        assertEquals(6, state.sectionNumber)
        assertEquals(21, state.stepNumber)
        assertEquals(32, state.totalSteps)
    }

    @Test
    fun removesEquipmentFromVisibleProgressWhenGymOnlyAccessWasSelected() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutTrainingDays,
            draft = OnboardingDraft()
                .withAnswer(OnboardingStepIds.WorkoutIntroChoice, OnboardingAnswer.Toggle(true))
                .withAnswer(OnboardingStepIds.WorkoutGymAccess, OnboardingAnswer.Text("gym")),
        )

        val state = factory(checkpoint, DefaultOnboardingFlow.definition)

        assertEquals(6, state.sectionNumber)
        assertEquals(18, state.stepNumber)
        assertEquals(31, state.totalSteps)
    }

    private fun checkpoint(
        sectionId: com.tnyx.features.onboarding.domain.model.OnboardingSectionId,
        stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId,
        draft: OnboardingDraft = OnboardingDraft(),
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = draft,
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
