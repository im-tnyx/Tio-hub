package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionId
import com.tnyx.features.onboarding.domain.model.OnboardingStepDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId

object OnboardingSectionIds {
    val Profile = OnboardingSectionId("profile")
    val BodyGoal = OnboardingSectionId("body_goal")
    val Workout = OnboardingSectionId("workout")
    val Review = OnboardingSectionId("review")
}

object OnboardingStepIds {
    val ProfileName = OnboardingStepId("profile.name")
    val ProfileGender = OnboardingStepId("profile.gender")
    val ProfileDateOfBirth = OnboardingStepId("profile.date_of_birth")

    val BodyGoalPrimaryGoal = OnboardingStepId("body_goal.primary_goal")
    val BodyGoalHeight = OnboardingStepId("body_goal.height")
    val BodyGoalCurrentWeight = OnboardingStepId("body_goal.current_weight")
    val BodyGoalTargetWeight = OnboardingStepId("body_goal.target_weight")
    val BodyGoalActivityLevel = OnboardingStepId("body_goal.activity_level")

    val WorkoutExperience = OnboardingStepId("workout.experience")
    val WorkoutLocation = OnboardingStepId("workout.location")
    val WorkoutEquipment = OnboardingStepId("workout.equipment")
    val WorkoutTrainingDays = OnboardingStepId("workout.training_days")
    val WorkoutDuration = OnboardingStepId("workout.duration")

    val ReviewSummary = OnboardingStepId("review.summary")
}

object DefaultOnboardingFlow {
    const val VERSION: Int = 1

    val definition = OnboardingFlowDefinition(
        version = VERSION,
        sections = listOf(
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Profile,
                steps = listOf(
                    required(OnboardingStepIds.ProfileName),
                    required(OnboardingStepIds.ProfileGender),
                    required(OnboardingStepIds.ProfileDateOfBirth),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.BodyGoal,
                steps = listOf(
                    required(OnboardingStepIds.BodyGoalPrimaryGoal),
                    required(OnboardingStepIds.BodyGoalHeight),
                    required(OnboardingStepIds.BodyGoalCurrentWeight),
                    required(OnboardingStepIds.BodyGoalTargetWeight),
                    required(OnboardingStepIds.BodyGoalActivityLevel),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Workout,
                steps = listOf(
                    required(OnboardingStepIds.WorkoutExperience),
                    required(OnboardingStepIds.WorkoutLocation),
                    optional(OnboardingStepIds.WorkoutEquipment),
                    required(OnboardingStepIds.WorkoutTrainingDays),
                    required(OnboardingStepIds.WorkoutDuration),
                ),
                isSkippable = true,
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Review,
                steps = listOf(required(OnboardingStepIds.ReviewSummary)),
            ),
        ),
    )

    private fun required(id: OnboardingStepId): OnboardingStepDefinition {
        return OnboardingStepDefinition(id = id)
    }

    private fun optional(id: OnboardingStepId): OnboardingStepDefinition {
        return OnboardingStepDefinition(id = id, isRequired = false)
    }
}
