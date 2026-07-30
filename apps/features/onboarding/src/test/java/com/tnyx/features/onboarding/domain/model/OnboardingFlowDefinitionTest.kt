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
            listOf("intro", "profile", "body_goal", "mobile", "workout_intro", "workout", "targets", "source", "review"),
            flow.sections.map { section -> section.id.value },
        )
        assertEquals(31, flow.totalSteps)
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Intro,
                stepId = OnboardingStepIds.IntroWelcome,
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
        val introEnd = OnboardingPosition(
            sectionId = OnboardingSectionIds.Intro,
            stepId = OnboardingStepIds.IntroWelcome,
        )
        val profileStart = OnboardingPosition(
            sectionId = OnboardingSectionIds.Profile,
            stepId = OnboardingStepIds.ProfileName,
        )
        val profileEnd = OnboardingPosition(
            sectionId = OnboardingSectionIds.Profile,
            stepId = OnboardingStepIds.ProfileDateOfBirth,
        )
        val bodyStart = OnboardingPosition(
            sectionId = OnboardingSectionIds.BodyGoal,
            stepId = OnboardingStepIds.BodyGoalPrimaryGoal,
        )

        assertEquals(profileStart, flow.next(introEnd))
        assertEquals(introEnd, flow.previous(profileStart))
        assertEquals(bodyStart, flow.next(profileEnd))
        assertEquals(profileEnd, flow.previous(bodyStart))
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.BodyGoal,
                stepId = OnboardingStepIds.BodyGoalHealthCondition,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.BodyGoal,
                    stepId = OnboardingStepIds.BodyGoalActivityLevel,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Mobile,
                stepId = OnboardingStepIds.MobileNumber,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.BodyGoal,
                    stepId = OnboardingStepIds.BodyGoalHealthCondition,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.WorkoutIntro,
                stepId = OnboardingStepIds.WorkoutIntroChoice,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Mobile,
                    stepId = OnboardingStepIds.MobileNumber,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutExperience,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.WorkoutIntro,
                    stepId = OnboardingStepIds.WorkoutIntroChoice,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutGymAccess,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutExperience,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutLocation,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutGymAccess,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutFocusAreas,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutLocation,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutEquipment,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutFocusAreas,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutSplit,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutDuration,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutHealthConcerns,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutSplit,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutSpecialEventGoal,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutHealthConcerns,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsRecommendationSummary,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Targets,
                    stepId = OnboardingStepIds.TargetsWaterTarget,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsGoalPace,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Targets,
                    stepId = OnboardingStepIds.TargetsRecommendationSummary,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsNutritionSummary,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Targets,
                    stepId = OnboardingStepIds.TargetsGoalPace,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutDuration,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutSplit,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutSplit,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutHealthConcerns,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutHealthConcerns,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutSpecialEventGoal,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutGymAccess,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutLocation,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Workout,
                stepId = OnboardingStepIds.WorkoutFocusAreas,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutEquipment,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.BodyGoal,
                stepId = OnboardingStepIds.BodyGoalHealthCondition,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Mobile,
                    stepId = OnboardingStepIds.MobileNumber,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.WorkoutIntro,
                stepId = OnboardingStepIds.WorkoutIntroChoice,
            ),
            flow.previous(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutExperience,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Targets,
                stepId = OnboardingStepIds.TargetsStepsTarget,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Workout,
                    stepId = OnboardingStepIds.WorkoutSpecialEventGoal,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Source,
                stepId = OnboardingStepIds.SourceChannel,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Targets,
                    stepId = OnboardingStepIds.TargetsNutritionSummary,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Source,
                stepId = OnboardingStepIds.SourceReason,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Source,
                    stepId = OnboardingStepIds.SourceChannel,
                ),
            ),
        )
        assertEquals(
            OnboardingPosition(
                sectionId = OnboardingSectionIds.Review,
                stepId = OnboardingStepIds.ReviewSummary,
            ),
            flow.next(
                OnboardingPosition(
                    sectionId = OnboardingSectionIds.Source,
                    stepId = OnboardingStepIds.SourceReason,
                ),
            ),
        )
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
            sectionId = OnboardingSectionIds.Mobile,
            stepId = OnboardingStepIds.MobileNumber,
        )

        val encoded = Json.encodeToString(OnboardingPosition.serializer(), position)
        val decoded = Json.decodeFromString(OnboardingPosition.serializer(), encoded)

        assertEquals(position, decoded)
        assertTrue(encoded.contains("\"mobile\""))
        assertTrue(encoded.contains("\"mobile.number\""))
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
