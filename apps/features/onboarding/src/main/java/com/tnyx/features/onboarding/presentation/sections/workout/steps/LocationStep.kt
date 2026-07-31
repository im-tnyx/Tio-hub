package com.tnyx.features.onboarding.presentation.sections.workout.steps

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
internal fun LocationStep(
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
            title = "Where should Tio bias your routine most weeks?",
            description = "This can differ from your raw access. For example, you may have gym access but still want a home-friendly routine.",
        )
        WorkoutLocationOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one workout location to continue")
        }
    }
}

private enum class WorkoutLocationOption(
    val id: String,
    val label: String,
    val description: String,
) {
    Gym(
        id = "gym",
        label = "Gym focused",
        description = "Bias the plan toward gym-based sessions most weeks.",
    ),
    Home(
        id = "home",
        label = "Home focused",
        description = "Bias the plan toward home-friendly sessions and simpler setup needs.",
    ),
    Both(
        id = "both",
        label = "Flexible mix",
        description = "Keep the plan usable across both home and gym without over-biasing either one.",
    ),
}
