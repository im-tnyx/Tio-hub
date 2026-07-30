package com.tnyx.features.onboarding.presentation.sections.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import com.tnyx.features.onboarding.domain.usecase.BuildReviewSummaryUseCase
import com.tnyx.features.onboarding.presentation.sections.review.steps.SummaryStep

@Composable
internal fun ReviewSectionContent(
    answer: OnboardingAnswer?,
    draftAnswers: Map<OnboardingStepId, OnboardingAnswer>,
    showValidationError: Boolean,
    onAnswerChanged: (OnboardingAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summarySections = remember(draftAnswers) {
        BuildReviewSummaryUseCase()(draftAnswers)
    }

    SummaryStep(
        answer = answer,
        sections = summarySections,
        showValidationError = showValidationError,
        onAnswerChanged = onAnswerChanged,
        modifier = modifier,
    )
}
