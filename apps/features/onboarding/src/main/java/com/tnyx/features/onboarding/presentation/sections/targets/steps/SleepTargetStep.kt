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
internal fun SleepTargetStep(
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
            title = "What sleep rhythm should your plan protect?",
            description = "This is a lightweight recovery target so future coaching can respect your usual sleep timing instead of pushing a generic routine.",
        )
        SleepTargetOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose the sleep rhythm that fits you best")
        }
    }
}

private enum class SleepTargetOption(
    val id: String,
    val label: String,
    val description: String,
) {
    RecoverEarly(
        id = "recover_early",
        label = "Early recovery",
        description = "Prioritize winding down earlier so your plan can protect 8-hour recovery blocks.",
    ),
    BalancedEvenings(
        id = "balanced_evenings",
        label = "Balanced evenings",
        description = "Keep a steady 7-8 hour rhythm that works with a normal workday and flexible evenings.",
    ),
    FlexibleLateSchedule(
        id = "flexible_late_schedule",
        label = "Late schedule",
        description = "Account for later nights so reminders and recovery targets do not feel unrealistic.",
    ),
}
