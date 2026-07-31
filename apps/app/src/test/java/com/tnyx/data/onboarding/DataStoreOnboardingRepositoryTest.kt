package com.tnyx.data.onboarding

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreOnboardingRepositoryTest {

    @Test
    fun checkpointSurvivesRepositoryRecreation() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstRepository = DataStoreOnboardingRepository(context)
        firstRepository.clearCheckpoint()
        val expected = checkpoint()

        firstRepository.saveCheckpoint(expected)
        val recreatedRepository = DataStoreOnboardingRepository(context)

        assertEquals(expected, recreatedRepository.observeCheckpoint().first())
        recreatedRepository.clearCheckpoint()
    }

    @Test
    fun clearCheckpointPersistsEmptyState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstRepository = DataStoreOnboardingRepository(context)
        firstRepository.clearCheckpoint()
        firstRepository.saveCheckpoint(checkpoint())

        firstRepository.clearCheckpoint()
        val recreatedRepository = DataStoreOnboardingRepository(context)

        assertNull(recreatedRepository.observeCheckpoint().first())
    }

    @Test
    fun malformedCheckpointIsRejectedByCodec() {
        assertNull(OnboardingCheckpointCodec.decodeOrNull("""{"flowVersion":"invalid"}"""))
    }

    private fun checkpoint(): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft()
                .withAnswer(
                    OnboardingStepIds.ProfileName,
                    OnboardingAnswer.Text("Santosh"),
                )
                .withAnswer(
                    OnboardingStepIds.WorkoutTrainingDays,
                    OnboardingAnswer.Selections(listOf("monday", "friday")),
                ),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutTrainingDays,
                ),
                completedSectionIds = setOf(
                    OnboardingSectionIds.Profile,
                    OnboardingSectionIds.BodyGoal,
                ),
            ),
        )
    }
}
