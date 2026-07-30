package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvanceOnboardingStepUseCaseTest {
    private val useCase = AdvanceOnboardingStepUseCase()

    @Test
    fun marksSectionCompleteWhenAdvancingAcrossBoundary() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Profile,
            stepId = OnboardingStepIds.ProfileDateOfBirth,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.BodyGoalPrimaryGoal,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.Profile),
        )
    }

    @Test
    fun advancesFromBodyGoalToHealthCondition() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalActivityLevel,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.BodyGoalHealthCondition,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromHealthConditionToMobile() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalHealthCondition,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.MobileNumber,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.BodyGoal),
        )
    }

    @Test
    fun advancesFromMobileToWorkoutIntro() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Mobile,
            stepId = OnboardingStepIds.MobileNumber,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutIntroChoice,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.Mobile),
        )
    }

    @Test
    fun workoutIntroYesContinuesToWorkoutSection() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.WorkoutIntro,
            stepId = OnboardingStepIds.WorkoutIntroChoice,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.WorkoutIntroChoice,
                com.tnyx.features.onboarding.domain.model.OnboardingAnswer.Toggle(true),
            ),
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutExperience,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun workoutIntroNoSkipsToTargets() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.WorkoutIntro,
            stepId = OnboardingStepIds.WorkoutIntroChoice,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.WorkoutIntroChoice,
                com.tnyx.features.onboarding.domain.model.OnboardingAnswer.Toggle(false),
            ),
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.TargetsStepsTarget,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.WorkoutIntro),
        )
    }

    @Test
    fun advancesFromWorkoutExperienceToGymAccess() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutExperience,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutGymAccess,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutGymAccessToLocation() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutGymAccess,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutLocation,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutLocationToFocusAreas() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutLocation,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutFocusAreas,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutFocusAreasToEquipment() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutFocusAreas,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutEquipment,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun gymOnlyFocusAreasSkipEquipment() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutFocusAreas,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.WorkoutGymAccess,
                com.tnyx.features.onboarding.domain.model.OnboardingAnswer.Text("gym"),
            ),
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutTrainingDays,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutDurationToSplit() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutDuration,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutSplit,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutSplitToHealthConcerns() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutSplit,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutHealthConcerns,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutHealthConcernsToSpecialEventGoal() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutHealthConcerns,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.WorkoutSpecialEventGoal,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromWorkoutToTargetsBeforeSource() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutSpecialEventGoal,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.TargetsStepsTarget,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.Workout),
        )
    }

    @Test
    fun advancesFromTargetsWaterTargetToRecommendationSummary() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Targets,
            stepId = OnboardingStepIds.TargetsWaterTarget,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.TargetsRecommendationSummary,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromTargetsRecommendationSummaryToGoalPace() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Targets,
            stepId = OnboardingStepIds.TargetsRecommendationSummary,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.TargetsGoalPace,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromTargetsGoalPaceToNutritionSummary() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Targets,
            stepId = OnboardingStepIds.TargetsGoalPace,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.TargetsNutritionSummary,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromTargetsToSource() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Targets,
            stepId = OnboardingStepIds.TargetsNutritionSummary,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.SourceChannel,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromSourceChannelToSourceReason() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Source,
            stepId = OnboardingStepIds.SourceChannel,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.SourceReason,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun advancesFromSourceReasonToReview() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Source,
            stepId = OnboardingStepIds.SourceReason,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Next
        assertEquals(
            OnboardingStepIds.ReviewSummary,
            result.checkpoint.progress.position.stepId,
        )
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.Source),
        )
    }

    @Test
    fun marksCheckpointCompletedAtFinalStep() {
        val checkpoint = checkpoint(
            sectionId = OnboardingSectionIds.Review,
            stepId = OnboardingStepIds.ReviewSummary,
        )

        val result = useCase(checkpoint, DefaultOnboardingFlow.definition)

        result as AdvanceOnboardingStepResult.Completed
        assertTrue(result.checkpoint.progress.isCompleted)
        assertTrue(
            result.checkpoint.progress.completedSectionIds.contains(OnboardingSectionIds.Review),
        )
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
