package com.tnyx.features.onboarding.domain.flow

import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingCheckpoint
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.model.OnboardingPosition
import com.tnyx.features.onboarding.domain.model.OnboardingProgress
import com.tnyx.features.onboarding.domain.model.OnboardingStepId
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingCheckpointResolverTest {
    private val flow = DefaultOnboardingFlow.definition
    private val resolver = OnboardingCheckpointResolver()

    @Test
    fun typedAnswersSurviveCheckpointSerialization() {
        val checkpoint = checkpoint(
            draft = OnboardingDraft()
                .withAnswer(
                    OnboardingStepIds.ProfileName,
                    OnboardingAnswer.Text("Santosh"),
                )
                .withAnswer(
                    OnboardingStepIds.BodyGoalCurrentWeight,
                    OnboardingAnswer.Decimal(78.4),
                )
                .withAnswer(
                    OnboardingStepIds.WorkoutTrainingDays,
                    OnboardingAnswer.Selections(listOf("monday", "thursday")),
                )
                .withAnswer(
                    OnboardingStepIds.WorkoutEquipment,
                    OnboardingAnswer.Toggle(true),
                ),
        )
        val json = Json {
            classDiscriminator = "answer_type"
            encodeDefaults = true
        }

        val serialized = json.encodeToString(OnboardingCheckpoint.serializer(), checkpoint)
        val restored = json.decodeFromString(OnboardingCheckpoint.serializer(), serialized)

        assertEquals(checkpoint, restored)
    }

    @Test
    fun draftUpdatesAreImmutable() {
        val emptyDraft = OnboardingDraft()
        val populatedDraft = emptyDraft.withAnswer(
            OnboardingStepIds.ProfileName,
            OnboardingAnswer.Text("Santosh"),
        )

        assertNull(emptyDraft.answerFor(OnboardingStepIds.ProfileName))
        assertEquals(
            OnboardingAnswer.Text("Santosh"),
            populatedDraft.answerFor(OnboardingStepIds.ProfileName),
        )
        assertTrue(populatedDraft.withoutAnswer(OnboardingStepIds.ProfileName).answers.isEmpty())
    }

    @Test
    fun validCheckpointResumesWithoutLosingDraft() {
        val expected = checkpoint(
            position = OnboardingPosition(
                sectionId = OnboardingSectionIds.BodyGoal,
                stepId = OnboardingStepIds.BodyGoalCurrentWeight,
            ),
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.ProfileName,
                OnboardingAnswer.Text("Santosh"),
            ),
            completedSectionIds = setOf(OnboardingSectionIds.Profile),
        )

        assertEquals(expected, resolver.resolve(expected, flow))
    }

    @Test
    fun versionMismatchStartsFreshCheckpoint() {
        val stale = checkpoint(
            flowVersion = flow.version + 1,
            draft = OnboardingDraft().withAnswer(
                OnboardingStepIds.ProfileName,
                OnboardingAnswer.Text("Stale"),
            ),
        )

        val resolved = resolver.resolve(stale, flow)

        assertEquals(flow.version, resolved.progress.flowVersion)
        assertEquals(flow.firstPosition(), resolved.progress.position)
        assertTrue(resolved.draft.answers.isEmpty())
    }

    @Test
    fun unknownPositionStartsFreshCheckpoint() {
        val invalid = checkpoint(
            position = OnboardingPosition(
                sectionId = OnboardingSectionIds.Profile,
                stepId = OnboardingStepId("profile.unknown"),
            ),
        )

        val resolved = resolver.resolve(invalid, flow)

        assertEquals(flow.firstPosition(), resolved.progress.position)
        assertFalse(resolved.progress.isCompleted)
    }

    @Test
    fun unknownDraftAnswerStartsFreshCheckpoint() {
        val invalid = checkpoint(
            draft = OnboardingDraft().withAnswer(
                OnboardingStepId("profile.retired"),
                OnboardingAnswer.Text("Legacy"),
            ),
        )

        val resolved = resolver.resolve(invalid, flow)

        assertEquals(flow.firstPosition(), resolved.progress.position)
        assertTrue(resolved.draft.answers.isEmpty())
    }

    private fun checkpoint(
        flowVersion: Int = flow.version,
        position: OnboardingPosition = flow.firstPosition(),
        draft: OnboardingDraft = OnboardingDraft(),
        completedSectionIds: Set<com.tnyx.features.onboarding.domain.model.OnboardingSectionId> =
            emptySet(),
    ): OnboardingCheckpoint {
        return OnboardingCheckpoint(
            draft = draft,
            progress = OnboardingProgress(
                flowVersion = flowVersion,
                position = position,
                completedSectionIds = completedSectionIds,
            ),
        )
    }
}
