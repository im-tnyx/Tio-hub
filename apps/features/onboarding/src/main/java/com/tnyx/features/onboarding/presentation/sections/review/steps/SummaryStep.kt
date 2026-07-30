package com.tnyx.features.onboarding.presentation.sections.review.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.usecase.ReviewSummarySection
import com.tnyx.features.onboarding.presentation.common.OnboardingChoiceCard
import com.tnyx.features.onboarding.presentation.common.OnboardingSelectionMode
import com.tnyx.features.onboarding.presentation.common.OnboardingStepHeading
import com.tnyx.features.onboarding.presentation.common.OnboardingValidationMessage

@Composable
internal fun SummaryStep(
    answer: OnboardingAnswer?,
    sections: List<ReviewSummarySection>,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isConfirmed = (answer as? OnboardingAnswer.Toggle)?.value == true

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceL),
    ) {
        OnboardingStepHeading(
            title = "Review your setup",
            description = "Check your details once, then finish onboarding and let Tio use these as your starting preferences.",
        )
        sections.forEach { section ->
            ReviewSummarySectionBlock(section = section)
        }
        OnboardingChoiceCard(
            title = "Everything looks right",
            description = "I understand these details can be refined later in the app.",
            selected = isConfirmed,
            selectionMode = OnboardingSelectionMode.Multiple,
            onClick = {
                onAnswerChanged(
                    if (isConfirmed) null else OnboardingAnswer.Toggle(true),
                )
            },
        )
        if (showValidationError) {
            OnboardingValidationMessage("Confirm the review to finish onboarding")
        }
    }
}

@Composable
private fun ReviewSummarySectionBlock(
    section: ReviewSummarySection,
) {
    TnyxCard(
        variant = TnyxCardVariant.Outlined,
        onClick = null,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
        ) {
            Text(
                text = section.title,
                style = TnyxTheme.typography.titleLarge,
                color = TnyxTheme.colors.textPrimary,
            )
            section.rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(color = TnyxTheme.colors.surfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = row.label,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                    Text(
                        text = row.value,
                        style = TnyxTheme.typography.titleMedium,
                        color = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
