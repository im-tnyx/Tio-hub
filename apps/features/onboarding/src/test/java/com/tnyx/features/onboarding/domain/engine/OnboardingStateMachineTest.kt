package com.tnyx.features.onboarding.domain.engine

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingStateMachineTest {
    private val stateMachine = OnboardingStateMachine(DefaultOnboardingFlow.definition)

    @Test
    fun nextPositionSkipsWorkoutWhenWorkoutIntroIsDeclined() {
        val nextPosition = stateMachine.nextPosition(
            checkpoint(
                sectionId = OnboardingSectionIds.WorkoutIntro,
                stepId = OnboardingStepIds.WorkoutIntroChoice,
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutIntroChoice,
                    OnboardingAnswer.Toggle(false),
                ),
            ),
        )

        assertEquals(OnboardingStepIds.TargetsStepsTarget, nextPosition?.stepId)
    }

    @Test
    fun nextPositionSkipsEquipmentForGymOnlyAccess() {
        val nextPosition = stateMachine.nextPosition(
            checkpoint(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutFocusAreas,
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutGymAccess,
                    OnboardingAnswer.Text("gym"),
                ),
            ),
        )

        assertEquals(OnboardingStepIds.WorkoutTrainingDays, nextPosition?.stepId)
    }

    @Test
    fun previousPositionReturnsWorkoutIntroWhenWorkoutWasSkipped() {
        val previousPosition = stateMachine.previousPosition(
            checkpoint(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsStepsTarget,
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutIntroChoice,
                    OnboardingAnswer.Toggle(false),
                ),
            ),
        )

        assertEquals(OnboardingStepIds.WorkoutIntroChoice, previousPosition?.stepId)
    }

    @Test
    fun nextPositionSkipsMobileWhenRouteContextAlreadyHasMobile() {
        val nextPosition = stateMachine.nextPosition(
            checkpoint(
                sectionId = OnboardingSectionIds.BodyGoal,
                stepId = OnboardingStepIds.BodyGoalHealthCondition,
            ).copy(
                routeContext = com.tnyx.features.onboarding.domain.model.OnboardingRouteContext(
                    mobilePresent = true,
                ),
            ),
        )

        assertEquals(OnboardingStepIds.WorkoutIntroChoice, nextPosition?.stepId)
    }

    @Test
    fun nextSectionEntryPositionReturnsFirstStepOfNextSection() {
        val targetPosition = stateMachine.nextSectionEntryPosition(
            checkpoint(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutExperience,
            ),
        )

        assertEquals(OnboardingStepIds.TargetsStepsTarget, targetPosition?.stepId)
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
