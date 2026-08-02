# Tio Android Onboarding Architecture

Status: Active foundation
Last verified: 2026-07-30
Owner: Android Onboarding

## Truth Boundary

This document governs the Tio Android onboarding implementation under
`apps/features/onboarding`. Runtime source remains behavior truth.

`G:\projects\Tnyx-hub\apps\android\mobile` is a design and behavior reference
only. Its Firebase, backend, remote-config, state-machine, and persistence
implementation is not Tio runtime truth.

## Current Runtime Status

Implemented:

- stable `OnboardingSectionId` and namespaced `OnboardingStepId` values,
- versioned `OnboardingFlowDefinition`,
- dedicated `BuildFlowUseCase` for effective-flow construction,
- dedicated `RestoreFlowUseCase` for resume-aware checkpoint restoration,
- deterministic first/next/previous position handling,
- a domain-owned `OnboardingStateMachine` that centralizes conditional next,
  previous, and section-skip transitions,
- insertion-safe serialized positions,
- the current local flow definition,
- typed `OnboardingDraft` answers and versioned `OnboardingProgress`,
- atomic `OnboardingCheckpoint` storage behind `OnboardingRepository`,
- app-owned Preferences DataStore persistence,
- explicit `ResumeManager` snapshot persistence alongside checkpoint storage,
- compatible-checkpoint resume and stale-checkpoint reset behavior,
- typed `RootRoute.Onboarding` destination and feature-owned navigation,
- `Route + Screen + ViewModel + UiState + Action` presentation container,
- a dedicated `SectionRenderer` that maps runtime section/step state to screen
  content,
- a dedicated `StepValidator` that centralizes button text, shell visibility,
  and required-answer validation wiring,
- serialized answer/save/navigation operations with validation and retry,
- next, back, skippable-section, and local-completion behavior,
- Profile name, gender, and date-of-birth step content,
- Profile-specific answer validation and ISO date storage,
- Body Goal primary-goal, height, current-weight, target-weight, and
  activity-level step content,
- Body Goal-specific validation and stable persisted goal/activity IDs,
- Workout intro gate, experience, gym-access, location-bias, focus-areas,
  conditional equipment, training-days, duration, split, optional workout
  concerns, and optional special-event step content,
- Workout-specific validation, conditional visible-progress handling, and
  stable persisted workout IDs and duration minute values,
- Targets step-target, sleep-target, water-target, recommendation-summary,
  goal-pace, and nutrition-summary step content with stable typed target IDs,
  bounded numeric targets, and a required target-bridge confirmation step,
- Source channel and reason step content with stable persisted source IDs,
- signed-in/profile-aware checkpoint preparation that pre-seeds available
  profile answers before rendering the local flow,
- auth/mobile-aware effective-flow handling so prefilled users can skip the
  local intro/mobile path without adding extra intro or typing-only steps,
- Review summary content with explicit local confirmation,
- post-review setup and ready completion states before app entry,
- `OnboardingUiState` draft-answer exposure for review-only summary rendering,
- shared onboarding choice cards for single-select and multi-select steps,
- scroll-safe content handling for longer onboarding forms,
- onboarding analytics event contracts and tracker/logger wiring with a local
  placeholder sink,
- Welcome `Get Started` entry into `RootRoute.Onboarding`,
- completed-onboarding sync to the owner-scoped Profile, Nutrition, and Workout
  Supabase rows for fields represented by the current schema,
- guest cold-start entry to `MainGraph` after completed onboarding.

Not implemented:

- guest-to-auth account handoff or full business-repository finalization,
- remote persistence for source attribution and answers not represented by the
  current owner schemas,
- conditional remote-config paths,
- production analytics sink.

## Current Local Flow

| Section ID | Initial steps | Business destination |
|---|---|---|
| `intro` | `welcome` (form implemented) | Onboarding entry |
| `profile` | `name`, `gender`, `date_of_birth` (forms implemented) | Profile |
| `body_goal` | `primary_goal`, `height`, `current_weight`, `target_weight`, `activity_level`, `health_condition` (forms implemented) | Profile/Nutrition contracts as approved |
| `mobile` | `number` (form implemented) | Profile / future auth verification |
| `workout_intro` | `choice` (form implemented) | Onboarding workout gate |
| `workout` | `experience`, `gym_access`, `location`, `focus_areas`, optional `equipment`, `training_days`, `duration`, `split`, optional `health_concerns`, optional `special_event_goal` (forms implemented) | Workout |
| `targets` | `steps_target`, `sleep_target`, `water_target`, `recommendation_summary`, `goal_pace`, `nutrition_summary` (forms implemented) | Future Nutrition/Progress/Recovery contracts |
| `source` | `channel`, `reason` (forms implemented) | Onboarding / future attribution contract |
| `review` | `summary` (form implemented) | Onboarding orchestration only |

The Workout section is skippable in version 1. A section being skippable does
not transfer ownership of its answers to Onboarding.

Profile answers currently use:

- a 2-30 character text answer for `profile.name`,
- `male`, `female`, or `prefer_not_to_say` for `profile.gender`,
- an ISO `yyyy-MM-dd` past date for `profile.date_of_birth`.

Body Goal answers currently use:

- one primary-goal ID from `build_muscle`, `lose_weight`, `keep_fit`,
  `boost_strength`, or `manage_stress`,
- a bounded height decimal in centimeters (`80.0..260.0`),
- bounded current and target weight decimals in kilograms (`20.0..400.0`),
- one activity-level ID from `sedentary`, `light`, `active`, `very_active`,
  or `dynamic`.

Workout answers currently use:

- one experience ID from `fresh`, `beginner`, `intermediate`, or `advanced`,
- one workout-access ID from `gym`, `home`, or `both`,
- one workout-location bias ID from `gym`, `home`, or `both`,
- one or more focus-area IDs from `full_body`, `shoulders`, `arms`, `back`,
  `chest`, `abs`, `glutes`, `legs`, and `cardio`,
- optional equipment selections from `dumbbells`, `bench`, `mat`, `barbell`,
  `bands`, and `kettlebell` when home-capable setup is selected,
- one or more training-day IDs from `monday` through `sunday`,
- one duration minute value from `30`, `45`, `60`, `90`, or `120`,
- one workout-split ID from `auto`, `full_body`, `upper_lower`, `ppl`, or
  `body_part`,
- optional free-text workout health concerns,
- optional free-text special-event goal.

Target answers currently use:

- one whole-number steps target in the range `2000..30000`,
- one sleep-rhythm ID from `recover_early`, `balanced_evenings`, or
  `flexible_late_schedule`,
- one whole-number water target in milliliters in the range `500..6000`,
- one required `Toggle(true)` confirmation for
  `targets.recommendation_summary`,
- one goal-pace ID from `relaxed`, `steady`, or `ambitious`,
- one nutrition-summary ID from `protein_priority`, `balanced_plate`, or
  `hydration_consistency`.

Source answers currently use:

- one discovery-source ID from `friend_referral`, `social_media`, `search`,
  `app_store`, `coach_or_gym`, or `other`.
- one source-reason ID from `workout_focus`, `nutrition_focus`, or
  `complete_reset`.

Review currently uses:

- the persisted local draft to render a read-only summary of Profile, Body
  Goal, Workout, Targets, and Source answers,
- a required `Toggle(true)` confirmation for `review.summary` before local
  completion is allowed.

Welcome `Get Started` now opens onboarding and Welcome `Skip` still opens Main.
The user-facing flow can now complete locally, finalize Profile-owned answers,
and synchronise the supported Profile, Nutrition, and Workout answers to the
authenticated Supabase user's owner-scoped rows. It then shows a short local
setup/ready completion path and skips Welcome on future cold starts. This still
must not be described as final product onboarding: guest-to-auth handoff,
source attribution persistence, and remote persistence for answers without an
owning schema remain unimplemented.

## Stable Identity Rules

1. Section IDs use lowercase snake case, such as `body_goal`.
2. Step IDs are namespaced strings, such as `body_goal.current_weight`.
3. Persisted progress must store `flowVersion`, `sectionId`, and `stepId`.
4. Enum ordinal and numeric step index must never be persisted as identity.
5. Adding a step must not rename unrelated existing IDs.
6. Removing or replacing an ID requires an explicit progress migration.
7. Flow version changes invalidate or migrate saved progress deliberately.

These rules allow a later flow to insert `nutrition`, `health`, `targets`,
`ai_coach`, or permission sections without rewriting existing positions.

## Current Runtime Shape

```text
OnboardingRoute
  -> OnboardingScreen
  -> OnboardingViewModel
  -> OnboardingUiState + OnboardingAction
  -> OnboardingStateMachine + flow/navigation use cases
  -> flow and validation use cases
  -> OnboardingRepository
  -> app-owned local/backend implementation
```

Section screens receive state and emit actions. They do not receive a
repository, Supabase client, database object, or mutable business owner.

## Local Persistence

The current local slice uses two distinct concepts:

- `OnboardingProgress`: current flow version and stable position.
- `OnboardingDraft`: entered answers that survive process restart.

They are written together as one `OnboardingCheckpoint`, preventing draft and
position writes from diverging. The repository contract belongs to the
onboarding feature. `DataStoreOnboardingRepository` and its Hilt binding belong
to `apps/app`.

`OnboardingCheckpointResolver` accepts a checkpoint only when its flow version,
position, completed section IDs, and answer step IDs belong to the active flow.
Otherwise it returns an empty checkpoint at the first active step. This is a
deliberate reset policy; a future flow version can replace it with an explicit
migration.

The checkpoint is currently device-local and is not scoped to an authenticated
account. Completed onboarding finalizes Profile-owned fields into the active
profile and marks it onboarded, then maps the current schema-supported
Nutrition and Workout answers to the authenticated user's remote rows. Entry
and future finalization work must still define guest-to-account handoff and
clear behavior. A future backend implementation may compose with the local
repository without changing screens.

Flow version `16` resets earlier local checkpoints so the current completion
and remote-sync behavior starts from a valid current-flow position.

When a signed-in or partially prefilled profile enters onboarding, checkpoint
preparation can now seed name, dob, gender, mobile, height, current weight,
and target weight into the draft before rendering. The effective local flow can
also hide `intro` and `mobile` when that bootstrap context already exists,
while keeping the current single intro screen and the same stable section/step
IDs for fresh users.

Finalization will map answers to their owning domain repositories. It must not
create one permanent onboarding mega-table or make Onboarding the owner of
Profile, Nutrition, Workout, Health, or Recovery data.

## Delivery Stages

1. Stable flow contracts and tests. Completed.
2. Local draft/progress repository and resume tests. Completed.
3. Onboarding graph, container, ViewModel, state, and actions. Completed.
4. Profile, Body Goal, Workout, and Review sections delivered one at a time.
   Profile, Body Goal, Workout, and Review completed for the local flow.
5. Guest-to-auth handoff, backend finalization, and optional dynamic flow only
   after Auth/API contracts are approved.
