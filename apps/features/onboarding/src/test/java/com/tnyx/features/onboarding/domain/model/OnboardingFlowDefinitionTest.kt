package com.tnyx.features.onboarding.domain.model

import com.tnyx.features.onboarding.domain.flow.DefaultOnboardingFlow
import com.tnyx.features.onboarding.domain.flow.OnboardingSectionIds
import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingFlowDefinitionTest {

    @Test
    fun defaultFlowUsesStableSectionAndStepIds() {
        val flow = DefaultOnboardingFlow.definition

        assertEquals(
            listOf("profile", "body_goal", "workout", "review"),
            flow.sections.map { section -> section.id.value },
        )
        assertEquals(14, flow.totalSteps)
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Profile,
                stepId = OnboardingStepIds.ProfileName,
            ),
            flow.firstPosition(),
        )
        assertTrue(flow.sections.single { it.id == OnboardingSectionIds.Workout }.isSkippable)
        assertFalse(
            flow.sections
                .single { it.id == OnboardingSectionIds.Workout }
                .steps
                .single { it.id == OnboardingStepIds.WorkoutEquipment }
                .isRequired,
        )
    }

    @Test
    fun nextAndPreviousCrossSectionBoundaries() {
        val flow = DefaultOnboardingFlow.definition
        val profileEnd = OnboardingPosition(
            sectionId = OnboardingSectionIds.Profile,
            stepId = OnboardingStepIds.ProfileDateOfBirth,
        )
        val bodyStart = OnboardingPosition(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalPrimaryGoal,
        )

        assertEquals(bodyStart, flow.next(profileEnd))
        assertEquals(profileEnd, flow.previous(bodyStart))
        assertNull(flow.previous(flow.firstPosition()))
        assertNull(
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Review,
                    stepId = OnboardingStepIds.ReviewSummary,
                ),
            ),
        )
    }

    @Test
    fun insertingANewStepDoesNotInvalidateExistingPosition() {
        val existingPosition = OnboardingPosition(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalCurrentWeight,
        )
        val evolvedFlow = DefaultOnboardingFlow.definition.copy(
            version = 2,
            sections = DefaultOnboardingFlow.definition.sections.map { section ->
                if (section.id != OnboardingSectionIds.Profile) {
                    section
                } else {
                    section.copy(
                        steps = section.steps + OnboardingStepDefinition(
                            id = OnboardingStepId("profile.username"),
                            isRequired = false,
                        ),
                    )
                }
            },
        )

        assertTrue(evolvedFlow.contains(existingPosition))
    }

    @Test
    fun positionRoundTripsWithStableStringIds() {
        val position = OnboardingPosition(
            sectionId = OnboardingSectionIds.Workout,
            stepId = OnboardingStepIds.WorkoutDuration,
        )

        val encoded = Json.encodeToString(OnboardingPosition.serializer(), position)
        val decoded = Json.decodeFromString(OnboardingPosition.serializer(), encoded)

        assertEquals(position, decoded)
        assertTrue(encoded.contains("\"workout\""))
        assertTrue(encoded.contains("\"workout.duration\""))
    }

    @Test
    fun invalidAndDuplicateDefinitionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            OnboardingStepId("height")
        }
        assertThrows(IllegalArgumentException::class.java) {
            OnboardingSectionDefinition(
                id = OnboardingSectionIds.Profile,
                steps = listOf(
                    OnboardingStepDefinition(OnboardingStepIds.WorkoutDuration),
                ),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            OnboardingFlowDefinition(
                version = 1,
                sections = listOf(
                    DefaultOnboardingFlow.definition.sections.first(),
                    DefaultOnboardingFlow.definition.sections.first(),
                ),
            )
        }
    }
}
