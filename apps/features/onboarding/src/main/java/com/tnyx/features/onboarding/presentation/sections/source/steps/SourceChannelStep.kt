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
internal fun SourceChannelStep(
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
            title = "How did you hear about Tio?",
            description = "This gives us lightweight attribution without changing your workout or nutrition setup.",
        )
        SOURCE_CHANNEL_OPTIONS.forEach { option ->
            OnboardingChoiceCard(
                title = option.title,
                description = option.description,
                selected = selectedId == option.id,
                onClick = {
                    onAnswerChanged(OnboardingAnswer.Text(option.id))
                },
            )
        }
        if (showValidationError) {
            OnboardingValidationMessage("Choose one source before continuing")
        }
    }
}

private data class SourceChannelOption(
    val id: String,
    val title: String,
    val description: String,
)

private val SOURCE_CHANNEL_OPTIONS = listOf(
    SourceChannelOption(
        id = "friend_referral",
        title = "Friend referral",
        description = "Someone you know recommended Tio to you.",
    ),
    SourceChannelOption(
        id = "social_media",
        title = "Social media",
        description = "You discovered Tio through Instagram, YouTube, or another social platform.",
    ),
    SourceChannelOption(
        id = "search",
        title = "Search",
        description = "You were looking for a workout or nutrition app on your own.",
    ),
    SourceChannelOption(
        id = "app_store",
        title = "App store",
        description = "You found Tio while browsing the Play Store.",
    ),
    SourceChannelOption(
        id = "coach_or_gym",
        title = "Coach or gym",
        description = "A trainer, gym, or community partner pointed you here.",
    ),
    SourceChannelOption(
        id = "other",
        title = "Other",
        description = "Another channel brought you to Tio.",
    ),
)
