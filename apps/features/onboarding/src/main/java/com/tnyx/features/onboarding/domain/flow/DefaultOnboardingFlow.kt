package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionId
import com.tnyx.features.onboarding.domain.model.OnboardingStepDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingStepId

object OnboardingSectionIds {
    val Intro = OnboardingSectionId("intro")
    val Profile = OnboardingSectionId("profile")
    val BodyGoal = OnboardingSectionId("body_goal")
    val Mobile = OnboardingSectionId("mobile")
    val WorkoutIntro = OnboardingSectionId("workout_intro")
    val Workout = OnboardingSectionId("workout")
    val Targets = OnboardingSectionId("targets")
    val Source = OnboardingSectionId("source")
    val Review = OnboardingSectionId("review")
}

object OnboardingStepIds {
    val IntroWelcome = OnboardingStepId("intro.welcome")

    val ProfileName = OnboardingStepId("profile.name")
    val ProfileGender = OnboardingStepId("profile.gender")
    val ProfileDateOfBirth = OnboardingStepId("profile.date_of_birth")

    val BodyGoalPrimaryGoal = OnboardingStepId("body_goal.primary_goal")
    val BodyGoalHeight = OnboardingStepId("body_goal.height")
    val BodyGoalCurrentWeight = OnboardingStepId("body_goal.current_weight")
    val BodyGoalTargetWeight = OnboardingStepId("body_goal.target_weight")
    val BodyGoalActivityLevel = OnboardingStepId("body_goal.activity_level")
    val BodyGoalHealthCondition = OnboardingStepId("body_goal.health_condition")

    val MobileNumber = OnboardingStepId("mobile.number")

    val WorkoutIntroChoice = OnboardingStepId("workout_intro.choice")

    val WorkoutExperience = OnboardingStepId("workout.experience")
    val WorkoutGymAccess = OnboardingStepId("workout.gym_access")
    val WorkoutLocation = OnboardingStepId("workout.location")
    val WorkoutFocusAreas = OnboardingStepId("workout.focus_areas")
    val WorkoutEquipment = OnboardingStepId("workout.equipment")
    val WorkoutTrainingDays = OnboardingStepId("workout.training_days")
    val WorkoutDuration = OnboardingStepId("workout.duration")
    val WorkoutSplit = OnboardingStepId("workout.split")
    val WorkoutHealthConcerns = OnboardingStepId("workout.health_concerns")
    val WorkoutSpecialEventGoal = OnboardingStepId("workout.special_event_goal")

    val TargetsStepsTarget = OnboardingStepId("targets.steps_target")
    val TargetsSleepTarget = OnboardingStepId("targets.sleep_target")
    val TargetsWaterTarget = OnboardingStepId("targets.water_target")
    val TargetsRecommendationSummary = OnboardingStepId("targets.recommendation_summary")
    val TargetsGoalPace = OnboardingStepId("targets.goal_pace")
    val TargetsNutritionSummary = OnboardingStepId("targets.nutrition_summary")

    val SourceChannel = OnboardingStepId("source.channel")
    val SourceReason = OnboardingStepId("source.reason")
    val SourceReferralDetail = OnboardingStepId("source.referral_detail")

    val ReviewSummary = OnboardingStepId("review.summary")
}

object DefaultOnboardingFlow {
    const val VERSION: Int = 14

    val definition = OnboardingFlowDefinition(
        version = VERSION,
        sections = listOf(
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Intro,
                steps = listOf(
                    required(OnboardingStepIds.IntroWelcome),
                ),
            ),
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
                    required(OnboardingStepIds.BodyGoalHealthCondition),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Mobile,
                steps = listOf(
                    required(OnboardingStepIds.MobileNumber),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.WorkoutIntro,
                steps = listOf(
                    required(OnboardingStepIds.WorkoutIntroChoice),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Workout,
                steps = listOf(
                    required(OnboardingStepIds.WorkoutExperience),
                    required(OnboardingStepIds.WorkoutGymAccess),
                    required(OnboardingStepIds.WorkoutLocation),
                    required(OnboardingStepIds.WorkoutFocusAreas),
                    optional(OnboardingStepIds.WorkoutEquipment),
                    required(OnboardingStepIds.WorkoutTrainingDays),
                    required(OnboardingStepIds.WorkoutDuration),
                    required(OnboardingStepIds.WorkoutSplit),
                    optional(OnboardingStepIds.WorkoutHealthConcerns),
                    optional(OnboardingStepIds.WorkoutSpecialEventGoal),
                ),
                isSkippable = true,
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Targets,
                steps = listOf(
                    required(OnboardingStepIds.TargetsStepsTarget),
                    required(OnboardingStepIds.TargetsSleepTarget),
                    required(OnboardingStepIds.TargetsWaterTarget),
                    required(OnboardingStepIds.TargetsRecommendationSummary),
                    required(OnboardingStepIds.TargetsGoalPace),
                    required(OnboardingStepIds.TargetsNutritionSummary),
                ),
            ),
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Source,
                steps = listOf(
                    required(OnboardingStepIds.SourceChannel),
                    required(OnboardingStepIds.SourceReason),
                    optional(OnboardingStepIds.SourceReferralDetail),
                ),
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
