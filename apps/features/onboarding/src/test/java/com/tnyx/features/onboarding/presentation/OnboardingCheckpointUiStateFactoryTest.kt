package com.tnyx.features.onboarding.presentation

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingCheckpointUiStateFactoryTest {
    private val factory = OnboardingCheckpointUiStateFactory()

    @Test
    fun buildsReadyStateByDefault() {
        val state = factory(
            checkpoint = checkpoint(),
            flow = DefaultOnboardingFlow.definition,
        )

        assertFalse(state.isSaving)
        assertFalse(state.hasPersistenceError)
    }

    @Test
    fun buildsSavingState() {
        val state = factory(
            checkpoint = checkpoint(),
            flow = DefaultOnboardingFlow.definition,
            status = OnboardingCheckpointUiStatus.Saving,
        )

        assertTrue(state.isSaving)
        assertFalse(state.hasPersistenceError)
    }

    @Test
    fun buildsPersistenceErrorState() {
        val state = factory(
            checkpoint = checkpoint(),
            flow = DefaultOnboardingFlow.definition,
            status = OnboardingCheckpointUiStatus.PersistenceError,
        )

        assertFalse(state.isSaving)
        assertTrue(state.hasPersistenceError)
    }

    private fun checkpoint(): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Profile,
                    stepId = OnboardingStepIds.ProfileName,
                ),
            ),
        )
    }
}
