package com.tnyx.features.onboarding.presentation.sections.workout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.sections.workout.steps.DurationStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.EquipmentStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.ExperienceStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.FocusAreasStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.GymAccessStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.LocationStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.TrainingDaysStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.WorkoutHealthConcernsStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.WorkoutSpecialEventStep
import com.tnyx.features.onboarding.presentation.sections.workout.steps.WorkoutSplitStep

@Composable
internal fun WorkoutSectionContent(
    stepId: OnboardingStepId,
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (stepId) {
        OnboardingStepIds.WorkoutExperience -> ExperienceStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutGymAccess -> GymAccessStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutLocation -> LocationStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutFocusAreas -> FocusAreasStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutEquipment -> EquipmentStep(
            answer = answer,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutTrainingDays -> TrainingDaysStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutDuration -> DurationStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutSplit -> WorkoutSplitStep(
            answer = answer,
            showValidationError = showValidationError,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutHealthConcerns -> WorkoutHealthConcernsStep(
            answer = answer,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        OnboardingStepIds.WorkoutSpecialEventGoal -> WorkoutSpecialEventStep(
            answer = answer,
            onAnswerChanged = onAnswerChanged,
            modifier = modifier,
        )

        else -> Unit
    }
}
