# Tio Android Onboarding Architecture

Status: Active foundation
Last verified: 2026-07-29
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
- deterministic first/next/previous position handling,
- insertion-safe serialized positions,
- the simple version 1 flow definition,
- typed `OnboardingDraft` answers and versioned `OnboardingProgress`,
- atomic `OnboardingCheckpoint` storage behind `OnboardingRepository`,
- app-owned Preferences DataStore persistence,
- compatible-checkpoint resume and stale-checkpoint reset behavior.

Not implemented:

- onboarding section screens,
- onboarding navigation graph/container,
- ViewModel integration and completion finalization,
- backend or Supabase synchronization,
- conditional remote-config paths,
- analytics.

## Version 1 Flow

| Section ID | Initial steps | Business destination |
|---|---|---|
| `profile` | `name`, `gender`, `date_of_birth` | Profile |
| `body_goal` | `primary_goal`, `height`, `current_weight`, `target_weight`, `activity_level` | Profile/Nutrition contracts as approved |
| `workout` | `experience`, `location`, optional `equipment`, `training_days`, `duration` | Workout |
| `review` | `summary` | Onboarding orchestration only |

The Workout section is skippable in version 1. A section being skippable does
not transfer ownership of its answers to Onboarding.

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

## Planned Runtime Shape

```text
OnboardingRoute
  -> OnboardingScreen
  -> OnboardingViewModel
  -> OnboardingUiState + OnboardingAction
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
account. The graph integration must define guest-to-account handoff and clear
behavior before backend synchronization is added. A future backend
implementation may compose with the local repository without changing screens.

Finalization will map answers to their owning domain repositories. It must not
create one permanent onboarding mega-table or make Onboarding the owner of
Profile, Nutrition, Workout, Health, or Recovery data.

## Delivery Stages

1. Stable flow contracts and tests. Completed.
2. Local draft/progress repository and resume tests. Completed.
3. Onboarding graph, container, ViewModel, state, and actions.
4. Profile, Body Goal, Workout, and Review sections delivered one at a time.
5. Backend finalization and optional dynamic flow only after Auth/API contracts
   are approved.
