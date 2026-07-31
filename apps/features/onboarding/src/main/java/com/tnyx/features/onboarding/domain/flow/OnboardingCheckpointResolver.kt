package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress

class OnboardingCheckpointResolver {
    fun resolve(
        checkpoint: OnboardingCheckpoint?,
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        return checkpoint
            ?.migrateIfNeeded(flow)
            ?.takeIf { stored -> stored.isCompatibleWith(flow) }
            ?: freshCheckpoint(flow)
    }

    fun freshCheckpoint(flow: OnboardingFlowDefinition): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            progress = OnboardingProgress(
                flowVersion = flow.version,
                position = flow.firstPosition(),
            ),
        )
    }

    private fun OnboardingCheckpoint.isCompatibleWith(
        flow: OnboardingFlowDefinition,
    ): Boolean {
        if (progress.flowVersion != flow.version || !flow.contains(progress.position)) {
            return false
        }

        val sectionIds = flow.sections.map { section -> section.id }.toSet()
        if (!sectionIds.containsAll(progress.completedSectionIds)) {
            return false
        }

        val stepIds = flow.sections
            .flatMap { section -> section.steps }
            .map { step -> step.id }
            .toSet()
        return stepIds.containsAll(draft.answers.keys)
    }

    private fun OnboardingCheckpoint.migrateIfNeeded(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        if (progress.flowVersion == flow.version) return this
        return when (progress.flowVersion) {
            flow.version - 1 -> migrateFromPreTargetsSummaryAndReferralDetailVersion(flow)
            flow.version - 2 -> migrateFromPreWorkoutGymAccessVersion(flow)
            flow.version - 3 -> migrateFromPreWorkoutSpecialEventVersion(flow)
            flow.version - 4 -> migrateFromPreWorkoutHealthConcernsVersion(flow)
            flow.version - 5 -> migrateFromPreWorkoutSplitVersion(flow)
            flow.version - 6 -> migrateFromPreWorkoutFocusAreasVersion(flow)
            flow.version - 7 -> migrateFromPreHealthConditionVersion(flow)
            flow.version - 8 -> migrateFromPreMobileVersion(flow)
            flow.version - 9 -> migrateFromPreIntroVersion(flow)
            else -> this
        }
    }

    private fun OnboardingCheckpoint.migrateFromPreTargetsSummaryAndReferralDetailVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedTargetsSummaryBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        ) || (
            progress.position.sectionId == OnboardingSectionIds.Targets &&
                progress.position.stepId in setOf(
                    OnboardingStepIds.TargetsGoalPace,
                    OnboardingStepIds.TargetsNutritionSummary,
                )
            )

        val migratedDraft = if (
            hasReachedTargetsSummaryBoundary &&
            draft.answerFor(OnboardingStepIds.TargetsRecommendationSummary) == null
        ) {
            draft.withAnswer(
                OnboardingStepIds.TargetsRecommendationSummary,
                OnboardingAnswer.Toggle(true),
            )
        } else {
            draft
        }

        return copy(
            draft = migratedDraft,
            progress = progress.copy(flowVersion = flow.version),
        )
    }

    private fun OnboardingCheckpoint.migrateFromPreWorkoutGymAccessVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedGymAccessBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.Targets,
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        ) || (
            progress.position.sectionId == OnboardingSectionIds.Workout &&
                progress.position.stepId in setOf(
                    OnboardingStepIds.WorkoutLocation,
                    OnboardingStepIds.WorkoutFocusAreas,
                    OnboardingStepIds.WorkoutEquipment,
                    OnboardingStepIds.WorkoutTrainingDays,
                    OnboardingStepIds.WorkoutDuration,
                    OnboardingStepIds.WorkoutSplit,
                    OnboardingStepIds.WorkoutHealthConcerns,
                    OnboardingStepIds.WorkoutSpecialEventGoal,
                )
            )

        if (!hasReachedGymAccessBoundary) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        val migratedDraft = if (draft.answerFor(OnboardingStepIds.WorkoutGymAccess) == null) {
            draft.withAnswer(
                OnboardingStepIds.WorkoutGymAccess,
                inferredWorkoutGymAccessAnswer(draft),
            )
        } else {
            draft
        }

        return copy(
            draft = migratedDraft,
            progress = progress.copy(flowVersion = flow.version),
        )
    }

    private fun OnboardingCheckpoint.migrateFromPreWorkoutSpecialEventVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        return copy(progress = progress.copy(flowVersion = flow.version))
    }

    private fun OnboardingCheckpoint.migrateFromPreWorkoutHealthConcernsVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        return copy(progress = progress.copy(flowVersion = flow.version))
    }

    private fun OnboardingCheckpoint.migrateFromPreWorkoutSplitVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedSplitBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.Targets,
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        ) || progress.completedSectionIds.contains(OnboardingSectionIds.Workout)
        if (!hasReachedSplitBoundary) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        val workoutDraftKeys = setOf(
            OnboardingStepIds.WorkoutExperience,
            OnboardingStepIds.WorkoutLocation,
            OnboardingStepIds.WorkoutFocusAreas,
            OnboardingStepIds.WorkoutEquipment,
            OnboardingStepIds.WorkoutTrainingDays,
            OnboardingStepIds.WorkoutDuration,
        )
        val completedWorkoutPath = progress.completedSectionIds.contains(OnboardingSectionIds.Workout)
        val hasWorkoutAnswers = draft.answers.keys.any(workoutDraftKeys::contains)
        val wantsWorkoutPlan = completedWorkoutPath || hasWorkoutAnswers

        if (!wantsWorkoutPlan || draft.answerFor(OnboardingStepIds.WorkoutSplit) != null) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        return copy(
            draft = draft.withAnswer(
                OnboardingStepIds.WorkoutSplit,
                OnboardingAnswer.Text("auto"),
            ),
            progress = progress.copy(flowVersion = flow.version),
        )
    }

    private fun OnboardingCheckpoint.migrateFromPreWorkoutFocusAreasVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedFocusAreaBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.Targets,
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        ) || (
            progress.position.sectionId == OnboardingSectionIds.Workout &&
                progress.position.stepId in setOf(
                    OnboardingStepIds.WorkoutEquipment,
                    OnboardingStepIds.WorkoutTrainingDays,
                    OnboardingStepIds.WorkoutDuration,
                )
            )

        if (!hasReachedFocusAreaBoundary) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        val migratedDraft = if (draft.answerFor(OnboardingStepIds.WorkoutFocusAreas) == null) {
            draft.withAnswer(
                OnboardingStepIds.WorkoutFocusAreas,
                defaultWorkoutFocusAreasAnswer(),
            )
        } else {
            draft
        }

        return copy(
            draft = migratedDraft,
            progress = progress.copy(flowVersion = flow.version),
        )
    }

    private fun OnboardingCheckpoint.migrateFromPreHealthConditionVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        return copy(progress = progress.copy(flowVersion = flow.version))
    }

    private fun OnboardingCheckpoint.migrateFromPreMobileVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedMobileBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.WorkoutIntro,
            OnboardingSectionIds.Workout,
            OnboardingSectionIds.Targets,
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        )
        if (!hasReachedMobileBoundary) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        return copy(
            progress = progress.copy(
                flowVersion = flow.version,
                completedSectionIds = progress.completedSectionIds + OnboardingSectionIds.Mobile,
            ),
        )
    }

    private fun OnboardingCheckpoint.migrateFromPreIntroVersion(
        flow: OnboardingFlowDefinition,
    ): OnboardingCheckpoint {
        val hasReachedWorkoutBoundary = progress.position.sectionId in setOf(
            OnboardingSectionIds.Workout,
            OnboardingSectionIds.Targets,
            OnboardingSectionIds.Source,
            OnboardingSectionIds.Review,
        )
        if (!hasReachedWorkoutBoundary) {
            return copy(progress = progress.copy(flowVersion = flow.version))
        }

        val workoutDraftKeys = setOf(
            OnboardingStepIds.WorkoutExperience,
            OnboardingStepIds.WorkoutLocation,
            OnboardingStepIds.WorkoutEquipment,
            OnboardingStepIds.WorkoutTrainingDays,
            OnboardingStepIds.WorkoutDuration,
        )
        val wantsWorkoutPlan = progress.position.sectionId == OnboardingSectionIds.Workout ||
            progress.completedSectionIds.contains(OnboardingSectionIds.Workout) ||
            draft.answers.keys.any(workoutDraftKeys::contains)

        val migratedDraft = if (draft.answerFor(OnboardingStepIds.WorkoutIntroChoice) == null) {
            draft.withAnswer(
                OnboardingStepIds.WorkoutIntroChoice,
                OnboardingAnswer.Toggle(wantsWorkoutPlan),
            )
        } else {
            draft
        }

        return copy(
            draft = migratedDraft,
            progress = progress.copy(
                flowVersion = flow.version,
                completedSectionIds = progress.completedSectionIds +
                    setOf(
                        OnboardingSectionIds.Intro,
                        OnboardingSectionIds.Mobile,
                        OnboardingSectionIds.WorkoutIntro,
                    ),
            ),
        )
    }
}

private fun defaultWorkoutFocusAreasAnswer(): OnboardingAnswer.Selections {
    return OnboardingAnswer.Selections(
        listOf(
            "full_body",
            "shoulders",
            "arms",
            "back",
            "chest",
            "abs",
            "glutes",
            "legs",
            "cardio",
        ),
    )
}

private fun inferredWorkoutGymAccessAnswer(
    draft: OnboardingDraft,
): OnboardingAnswer.Text {
    val locationValue = (draft.answerFor(OnboardingStepIds.WorkoutLocation) as? OnboardingAnswer.Text)
        ?.value
    return when (locationValue) {
        "gym" -> OnboardingAnswer.Text("gym")
        "home" -> OnboardingAnswer.Text("home")
        "both" -> OnboardingAnswer.Text("both")
        else -> {
            val hasEquipment = (draft.answerFor(OnboardingStepIds.WorkoutEquipment) as? OnboardingAnswer.Selections)
                ?.values
                ?.isNotEmpty() == true
            if (hasEquipment) {
                OnboardingAnswer.Text("home")
            } else {
                OnboardingAnswer.Text("both")
            }
        }
    }
}
