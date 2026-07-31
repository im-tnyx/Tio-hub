package com.tnyx.features.onboarding.presentation.sections.source.steps

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
internal fun SourceReasonStep(
    answer: OnboardingAnswer?,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId = (answer as? OnboardingAnswer.Text)?.value

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "What made you try Tio right now?",
            description = "This helps us understand whether people come for workouts, nutrition, or overall consistency before we build deeper onboarding branches.",
        )
        SOURCE_REASON_OPTIONS.forEach { option ->
            OnboardingChoiceCard(
                title = option.title,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose your main reason before continuing")
        }
    }
}

private data class SourceReasonOption(
    val id: String,
    val title: String,
    val description: String,
)

private val SOURCE_REASON_OPTIONS = listOf(
    SourceReasonOption(
        id = "workout_focus",
        title = "Workout focus",
        description = "You mainly want better training structure, routines, and consistency.",
    ),
    SourceReasonOption(
        id = "nutrition_focus",
        title = "Nutrition focus",
        description = "You are here more for food guidance, planning, and better daily choices.",
    ),
    SourceReasonOption(
        id = "complete_reset",
        title = "Complete reset",
        description = "You want one place for workouts, nutrition, and overall habit momentum.",
    ),
)
