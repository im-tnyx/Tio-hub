package com.tnyx.features.onboarding.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingAnswer {
    @Serializable
    @SerialName("text")
    data class Text(val value: String) : OnboardingAnswer

    @Serializable
    @SerialName("decimal")
    data class Decimal(val value: Double) : OnboardingAnswer {
        init {
            require(value.isFinite()) { "Onboarding decimal answer must be finite" }
        }
    }

    @Serializable
    @SerialName("selections")
    data class Selections(val values: List<String>) : OnboardingAnswer {
        init {
            require(values.all(String::isNotBlank)) {
                "Onboarding selections must not contain blank values"
            }
            require(values.distinct().size == values.size) {
                "Onboarding selections must not contain duplicates"
            }
        }
    }

    @Serializable
    @SerialName("toggle")
    data class Toggle(val value: Boolean) : OnboardingAnswer
}

@Serializable
data class OnboardingDraft(
    val answers: Map<OnboardingStepId, OnboardingAnswer> = emptyMap(),
) {
    fun answerFor(stepId: OnboardingStepId): OnboardingAnswer? = answers[stepId]

    fun withAnswer(
        stepId: OnboardingStepId,
        answer: OnboardingAnswer,
    ): OnboardingDraft {
        return copy(answers = answers + (stepId to answer))
    }

    fun withoutAnswer(stepId: OnboardingStepId): OnboardingDraft {
        return copy(answers = answers - stepId)
    }
}
