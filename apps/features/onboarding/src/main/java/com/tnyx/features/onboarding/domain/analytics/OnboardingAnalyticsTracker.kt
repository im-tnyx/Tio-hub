package com.tnyx.features.onboarding.domain.analytics

class OnboardingAnalyticsTracker(
    private val logger: OnboardingAnalyticsLogger,
) {
    constructor() : this(OnboardingAnalyticsLogger())

    fun track(event: OnboardingAnalyticsEvent) {
        when (event) {
            is OnboardingAnalyticsEvent.ScreenView -> {
                logger.log(
                    eventName = "screen_view",
                    params = mapOf(
                        "section" to event.sectionId,
                        "step" to event.stepId,
                    ),
                )
            }

            is OnboardingAnalyticsEvent.NextClicked -> {
                logger.log(
                    eventName = "next_clicked",
                    params = mapOf("section" to event.sectionId),
                )
            }

            is OnboardingAnalyticsEvent.BackClicked -> {
                logger.log(
                    eventName = "back_clicked",
                    params = mapOf("section" to event.sectionId),
                )
            }

            OnboardingAnalyticsEvent.OnboardingCompleted -> {
                logger.log("onboarding_completed")
            }

            is OnboardingAnalyticsEvent.UserAction -> {
                logger.log(
                    eventName = "user_action",
                    params = mapOf(
                        "section" to event.sectionId,
                        "action" to event.actionName,
                    ),
                )
            }
        }
    }
}
