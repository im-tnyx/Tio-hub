package com.tnyx.features.onboarding.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingStepDefinition(
    val id: OnboardingStepId,
    val isRequired: Boolean = true,
)

@Serializable
data class OnboardingSectionDefinition(
    val id: OnboardingSectionId,
    val steps: List<OnboardingStepDefinition>,
    val isSkippable: Boolean = false,
) {
    init {
        require(steps.isNotEmpty()) { "Onboarding section must contain at least one step" }
        require(steps.distinctBy(OnboardingStepDefinition::id).size == steps.size) {
            "Onboarding step IDs must be unique within a section"
        }
        require(steps.all { step -> step.id.sectionValue == id.value }) {
            "Onboarding step IDs must use their owning section namespace"
        }
    }
}

@Serializable
data class OnboardingPosition(
    val sectionId: OnboardingSectionId,
    val stepId: OnboardingStepId,
)

@Serializable
data class OnboardingFlowDefinition(
    val version: Int,
    val sections: List<OnboardingSectionDefinition>,
) {
    init {
        require(version > 0) { "Onboarding flow version must be positive" }
        require(sections.isNotEmpty()) { "Onboarding flow must contain at least one section" }
        require(sections.distinctBy(OnboardingSectionDefinition::id).size == sections.size) {
            "Onboarding section IDs must be unique"
        }

        val stepIds = sections.flatMap { section -> section.steps.map(OnboardingStepDefinition::id) }
        require(stepIds.distinct().size == stepIds.size) {
            "Onboarding step IDs must be unique across the flow"
        }
    }

    val totalSteps: Int
        get() = sections.sumOf { section -> section.steps.size }

    fun firstPosition(): OnboardingPosition = orderedPositions().first()

    fun contains(position: OnboardingPosition): Boolean {
        return orderedPositions().contains(position)
    }

    fun next(position: OnboardingPosition): OnboardingPosition? {
        val positions = orderedPositions()
        val currentIndex = positions.indexOf(position)
        return positions.getOrNull(currentIndex + 1).takeIf { currentIndex >= 0 }
    }

    fun previous(position: OnboardingPosition): OnboardingPosition? {
        val positions = orderedPositions()
        val currentIndex = positions.indexOf(position)
        return positions.getOrNull(currentIndex - 1).takeIf { currentIndex >= 0 }
    }

    fun completedFraction(position: OnboardingPosition): Float {
        val positions = orderedPositions()
        val currentIndex = positions.indexOf(position)
        require(currentIndex >= 0) { "Onboarding position does not belong to this flow" }
        return (currentIndex + 1).toFloat() / positions.size
    }

    private fun orderedPositions(): List<OnboardingPosition> {
        return sections.flatMap { section ->
            section.steps.map { step ->
                OnboardingPosition(
                    sectionId = section.id,
                    stepId = step.id,
                )
            }
        }
    }
}
