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
internal fun WorkoutSplitStep(
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
            title = "How should your workout split feel?",
            description = "Choose whether Tio should recommend a split or follow a clear training structure.",
        )
        WorkoutSplitOption.entries.forEach { option ->
            OnboardingChoiceCard(
                title = option.label,
                badge = option.badge,
                description = option.description,
                selected = selectedId == option.id,
                onClick = { onAnswerChanged(OnboardingAnswer.Text(option.id)) },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Select one workout split to continue")
        }
    }
}

private enum class WorkoutSplitOption(
    val id: String,
    val label: String,
    val badge: String,
    val description: String,
) {
    Auto(
        id = "auto",
        label = "Recommend for me",
        badge = "Simple start",
        description = "Tio chooses a balanced split based on your time, goals, and current level.",
    ),
    FullBody(
        id = "full_body",
        label = "Full body",
        badge = "All-in-one",
        description = "Each session covers most major muscle groups for balanced weekly coverage.",
    ),
    UpperLower(
        id = "upper_lower",
        label = "Upper / lower",
        badge = "Balanced structure",
        description = "Split upper-body and lower-body days for steady recovery and progression.",
    ),
    PushPullLegs(
        id = "ppl",
        label = "Push / pull / legs",
        badge = "Classic split",
        description = "Separate pushing, pulling, and leg work into distinct training days.",
    ),
    BodyPart(
        id = "body_part",
        label = "Body part split",
        badge = "Focused sessions",
        description = "Train one or two muscle groups per session with more direct volume.",
    ),
}
