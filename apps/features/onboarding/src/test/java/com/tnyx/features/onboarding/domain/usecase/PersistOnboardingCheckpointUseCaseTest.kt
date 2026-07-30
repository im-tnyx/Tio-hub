package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.features.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PersistOnboardingCheckpointUseCaseTest {
    private val useCase = PersistOnboardingCheckpointUseCase()

    @Test
    fun savesCheckpointAndReturnsSuccess() = runTest {
        val repository = FakeOnboardingRepository()
        val checkpoint = checkpoint()

        val result = useCase(checkpoint, repository)

        assertEquals(PersistOnboardingCheckpointResult.Success(checkpoint), result)
        assertEquals(checkpoint, repository.checkpoint)
    }

    @Test
    fun returnsFailureWhenSaveThrows() = runTest {
        val repository = FakeOnboardingRepository().apply {
            failSaves = true
        }
        val checkpoint = checkpoint()

        val result = useCase(checkpoint, repository)

        assertEquals(PersistOnboardingCheckpointResult.Failure(checkpoint), result)
    }

    private fun checkpoint(): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = OnboardingDraft(),
            progress = OnboardingProgress(
                flowVersion = DefaultOnboardingFlow.VERSION,
                position = DefaultOnboardingFlow.definition.firstPosition(),
            ),
        )
    }
}

private class FakeOnboardingRepository : OnboardingRepository {
    var checkpoint: OnboardingCheckpoint? = null
    var failSaves: Boolean = false

    override fun observeCheckpoint(): Flow<OnboardingCheckpoint?> = flowOf(checkpoint)

    override suspend fun saveCheckpoint(checkpoint: OnboardingCheckpoint) {
        if (failSaves) error("Expected test save failure")
        this.checkpoint = checkpoint
    }

    override suspend fun clearCheckpoint() = Unit
}
