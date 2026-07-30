package com.tnyx.features.onboarding.presentation.sections.targets.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun NutritionSummaryStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
    ) {
        OnboardingStepHeading(
            title = "Which nutrition baseline should your starter summary emphasize?",
            description = "This stays intentionally simple for now, but it gives the next plan version a clear nutrition starting point.",
        )
        NutritionSummaryOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose the nutrition focus you want to start from")
        }
    }
}

private enum class NutritionSummaryOption(
    val id: String,
    val label: String,
    val description: String,
) {
    ProteinPriority(
        id = "protein_priority",
        label = "Protein priority",
        description = "Lead with protein awareness so strength, recovery, and satiety are easier to support.",
    ),
    BalancedPlate(
        id = "balanced_plate",
        label = "Balanced plate",
        description = "Use a steadier mix of protein, carbs, fiber, and fats without an aggressive bias.",
    ),
    HydrationConsistency(
        id = "hydration_consistency",
        label = "Hydration first",
        description = "Keep water consistency and simple meal structure as the first behavior target.",
    ),
}
