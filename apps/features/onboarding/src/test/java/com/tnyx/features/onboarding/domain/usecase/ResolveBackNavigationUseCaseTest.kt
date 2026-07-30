package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveBackNavigationUseCaseTest {
    private val useCase = ResolveBackNavigationUseCase()

    @Test
    fun returnsExitWhenCheckpointIsMissing() {
        val result = useCase(null, DefaultOnboardingFlow.definition)

        assertEquals(ResolveBackNavigationResult.Exit, result)
    }

    @Test
    fun returnsExitAtFirstStep() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.Intro,
                stepId = OnboardingStepIds.IntroWelcome,
            ),
            DefaultOnboardingFlow.definition,
        )

        assertEquals(ResolveBackNavigationResult.Exit, result)
    }

    @Test
    fun returnsPreviousCheckpointForLaterSteps() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.BodyGoal,
                stepId = OnboardingStepIds.BodyGoalPrimaryGoal,
            ),
            DefaultOnboardingFlow.definition,
        )

        result as ResolveBackNavigationResult.Previous
        assertEquals(
            OnboardingStepIds.ProfileDateOfBirth,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun returnsWorkoutIntroWhenTargetsWereReachedAfterDecliningWorkout() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsStepsTarget,
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutIntroChoice,
                    com.tnyx.features.onboarding.domain.model.OnboardingAnswer.Toggle(false),
                ),
            ),
            DefaultOnboardingFlow.definition,
        )

        result as ResolveBackNavigationResult.Previous
        assertEquals(
            OnboardingStepIds.WorkoutIntroChoice,
            result.checkpoint.progress.position.stepId,
        )
    }

    @Test
    fun returnsFocusAreasWhenGymOnlyAccessSkippedEquipment() {
        val result = useCase(
            checkpoint(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutTrainingDays,
                draft = OnboardingDraft().withAnswer(
                    OnboardingStepIds.WorkoutGymAccess,
                    com.tnyx.features.onboarding.domain.model.OnboardingAnswer.Text("gym"),
                ),
            ),
            DefaultOnboardingFlow.definition,
        )

        result as ResolveBackNavigationResult.Previous
        assertEquals(
            OnboardingStepIds.WorkoutFocusAreas,
            result.checkpoint.progress.position.stepId,
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
