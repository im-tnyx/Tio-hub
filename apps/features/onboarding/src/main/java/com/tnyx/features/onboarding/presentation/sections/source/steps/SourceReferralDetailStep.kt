package com.tnyx.features.onboarding.presentation.sections.source.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading

@Composable
internal fun SourceReferralDetailStep(
    answer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = referralDetailCopyFor(
        (draftAnswers[OnboardingStepIds.SourceChannel] as? OnboardingAnswer.Text)?.value,
    )
    val initialValue = (answer as? OnboardingAnswer.Text)?.value.orEmpty()
    var detail by rememberSaveable { mutableStateOf(initialValue) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = copy.title,
            description = copy.description,
        )
        TnyxTextField(
            value = detail,
            onValueChange = { updatedValue ->
                detail = updatedValue
                onAnswerChanged(
                    updatedValue.trim().takeIf(String::isNotBlank)?.let(OnboardingAnswer::Text),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(copy.label) },
            placeholder = { Text(copy.placeholder) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            helperMessage = "Optional. This is only for cleaner attribution and future invite/referral logic.",
        )
    }
}

private data class ReferralDetailCopy(
    val title: String,
    val description: String,
    val label: String,
    val placeholder: String,
)

private fun referralDetailCopyFor(channelId: String?): ReferralDetailCopy {
    return when (channelId) {
        "friend_referral" -> ReferralDetailCopy(
            title = "Who sent you to Tio?",
            description = "If someone referred you, add a quick name or handle so we can keep future referral logic cleaner.",
            label = "Friend or creator",
            placeholder = "Example: Aman, @fitwithravi",
        )
        "coach_or_gym" -> ReferralDetailCopy(
            title = "Which coach, gym, or community brought you here?",
            description = "This helps us understand partner-led discovery without overloading your main onboarding reason.",
            label = "Coach, gym, or group",
            placeholder = "Example: Pulse Fitness, Coach Neha",
        )
        "social_media" -> ReferralDetailCopy(
            title = "Any creator or platform detail worth noting?",
            description = "You can leave a handle or campaign cue if there was a specific post, reel, or creator behind the install.",
            label = "Creator or campaign",
            placeholder = "Example: YouTube Shorts, @trainwithsid",
        )
        else -> ReferralDetailCopy(
            title = "Any quick attribution detail worth keeping?",
            description = "This stays optional, but a small note can help future source and invite logic stay cleaner.",
            label = "Extra source detail",
            placeholder = "Example: office challenge, wellness group, WhatsApp share",
        )
    }
}
