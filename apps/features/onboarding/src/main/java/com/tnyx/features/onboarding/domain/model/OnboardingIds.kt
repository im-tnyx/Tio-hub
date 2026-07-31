package com.tnyx.features.onboarding.domain.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class OnboardingSectionId(val value: String) {
    init {
        require(SECTION_ID_PATTERN.matches(value)) {
            "Onboarding section ID must use lowercase snake_case"
        }
    }

    override fun toString(): String = value

    private companion object {
        val SECTION_ID_PATTERN = Regex("[a-z][a-z0-9_]*")
    }
}

@Serializable
@JvmInline
value class OnboardingStepId(val value: String) {
    init {
        require(STEP_ID_PATTERN.matches(value)) {
            "Onboarding step ID must be namespaced as section.step"
        }
    }

    val sectionValue: String
        get() = value.substringBefore('.')

    override fun toString(): String = value

    private companion object {
        val STEP_ID_PATTERN = Regex("[a-z][a-z0-9_]*\\.[a-z][a-z0-9_]*")
    }
}
