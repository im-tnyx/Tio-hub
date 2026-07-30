package com.tnyx.features.onboarding.presentation.sections.bodygoal.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingSelectionMode
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun HealthConditionStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = (answer as? OnboardingAnswer.Selections)?.values.orEmpty().toSet()
    val orderedIds = HealthConditionOption.entries.map { it.id }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Any health conditions we should keep in mind?",
            description = "This gives Tio better context before workout and recovery recommendations expand later.",
        )
        HealthConditionOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedIds.contains(option.id),
                selectionMode = OnboardingSelectionMode.Multiple,
                onClick = {
                    onAnswerChanged(
                        selectedIds.toggleHealthConditionSelection(
                            optionId = option.id,
                            orderedIds = orderedIds,
                        ),
                    )
                },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose at least one health context option")
        }
    }
}

private enum class HealthConditionOption(
    val id: String,
    val label: String,
    val description: String,
) {
    None(
        id = "none",
        label = "No current condition",
        description = "Use this when no health context needs to shape your baseline right now.",
    ),
    Diabetes(
        id = "diabetes",
        label = "Diabetes",
        description = "Helpful if energy, recovery, or nutrition pacing needs extra care.",
    ),
    Hypertension(
        id = "hypertension",
        label = "Hypertension",
        description = "Useful context for intensity, recovery, and routine planning.",
    ),
    LowBloodPressure(
        id = "low_bp",
        label = "Low blood pressure",
        description = "Can affect training comfort, hydration, and pacing decisions.",
    ),
    InjuryRecovery(
        id = "injury_recovery",
        label = "Injury recovery",
        description = "Use this if recent pain or recovery status should shape future coaching.",
    ),
    Other(
        id = "other",
        label = "Other health context",
        description = "A broader placeholder for future detailed health-condition support.",
    ),
}

private fun Set<String>.toggleHealthConditionSelection(
    optionId: String,
    orderedIds: List<String>,
): OnboardingAnswer.Selections? {
    val updated = toMutableSet()
    if (optionId == "none") {
        return OnboardingAnswer.Selections(listOf("none"))
    }

    updated.remove("none")
    if (!updated.add(optionId)) {
        updated.remove(optionId)
    }
    if (updated.isEmpty()) return null

    val ordered = orderedIds.filter(updated::contains)
    return OnboardingAnswer.Selections(ordered)
}
