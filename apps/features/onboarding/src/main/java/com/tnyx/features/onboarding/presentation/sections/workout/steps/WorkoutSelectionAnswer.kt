package com.tnyx.features.onboarding.presentation.sections.workout.steps

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer

internal fun Set<String>.toggleWorkoutSelection(
    optionId: String,
    orderedIds: List<String>,
): OnboardingAnswer.Selections? {
    val updated = toMutableSet().apply {
        if (!add(optionId)) {
            remove(optionId)
        }
    }
    if (updated.isEmpty()) return null
    val ordered = orderedIds.filter(updated::contains)
    return OnboardingAnswer.Selections(ordered)
}
