package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingAuthState
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingRouteContext
import com.tnyx.features.onboarding.domain.model.OnboardingSectionDefinition
import com.tnyx.features.onboarding.domain.model.OnboardingSectionId
import com.tnyx.features.onboarding.domain.model.OnboardingStepId

fun com.tnyx.features.onboarding.domain.model.OnboardingFlowDefinition.effectiveSections(
    checkpoint: OnboardingCheckpoint,
): List<OnboardingSectionDefinition> {
    val draftAnswers = checkpoint.draft.answers
    val routeContext = checkpoint.routeContext
    val wantsToSkipWorkout = (draftAnswers[OnboardingStepIds.WorkoutIntroChoice] as? OnboardingAnswer.Toggle)
        ?.value == false
    val gymOnlyAccess = (draftAnswers[OnboardingStepIds.WorkoutGymAccess] as? OnboardingAnswer.Text)
        ?.value == "gym"

    val visibleSections = sections.filterNot { section ->
        shouldHideIntro(routeContext) && section.id == OnboardingSectionIds.Intro ||
            shouldHideMobile(routeContext) && section.id == OnboardingSectionIds.Mobile ||
            wantsToSkipWorkout && section.id == OnboardingSectionIds.Workout
    }

    if (!gymOnlyAccess) return visibleSections

    return visibleSections.map { section ->
        if (section.id != OnboardingSectionIds.Workout) {
            section
        } else {
            section.copy(
                steps = section.steps.filterNot { step -> step.id == OnboardingStepIds.WorkoutEquipment },
            )
        }
    }
}

fun List<OnboardingSectionDefinition>.containsPosition(position: OnboardingPosition): Boolean {
    return any { section ->
        section.id == position.sectionId &&
            section.steps.any { step -> step.id == position.stepId }
    }
}

fun List<OnboardingSectionDefinition>.next(position: OnboardingPosition): OnboardingPosition? {
    val positions = flattenedPositions()
    val index = positions.indexOf(position)
    if (index < 0) return null
    return positions.getOrNull(index + 1)
}

fun List<OnboardingSectionDefinition>.previous(position: OnboardingPosition): OnboardingPosition? {
    val positions = flattenedPositions()
    val index = positions.indexOf(position)
    if (index <= 0) return null
    return positions.getOrNull(index - 1)
}

fun List<OnboardingSectionDefinition>.firstPosition(): OnboardingPosition? {
    return firstOrNull()
        ?.steps
        ?.firstOrNull()
        ?.let { step -> OnboardingPosition(sectionId = first().id, stepId = step.id) }
}

fun List<OnboardingSectionDefinition>.firstPosition(sectionId: OnboardingSectionId): OnboardingPosition? {
    return firstOrNull { section -> section.id == sectionId }
        ?.steps
        ?.firstOrNull()
        ?.let { step -> OnboardingPosition(sectionId = sectionId, stepId = step.id) }
}

fun List<OnboardingSectionDefinition>.position(
    sectionId: OnboardingSectionId,
    stepId: OnboardingStepId,
): OnboardingPosition? {
    return firstOrNull { section -> section.id == sectionId }
        ?.steps
        ?.firstOrNull { step -> step.id == stepId }
        ?.let { step -> OnboardingPosition(sectionId = sectionId, stepId = step.id) }
}

private fun List<OnboardingSectionDefinition>.flattenedPositions(): List<OnboardingPosition> {
    return flatMap { section ->
        section.steps.map { step ->
            OnboardingPosition(
                sectionId = section.id,
                stepId = step.id,
            )
        }
    }
}

private fun shouldHideIntro(routeContext: OnboardingRouteContext): Boolean {
    return routeContext.authState == OnboardingAuthState.SignedIn ||
        routeContext.signupCompleted ||
        routeContext.entryPath != com.tnyx.features.onboarding.domain.model.OnboardingEntryPath.GetStarted
}

private fun shouldHideMobile(routeContext: OnboardingRouteContext): Boolean {
    return routeContext.mobilePresent
}
