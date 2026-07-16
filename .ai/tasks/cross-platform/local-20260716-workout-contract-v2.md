---
task_id: local-20260716-workout-contract-v2
title: Tio Workout shared contract v2 and reducer
status: completed
priority: high
owner: cross-platform
branch: codex/workout-contract-v2
github_issue:
github_pr:
platforms: [shared, android, watch]
created: 2026-07-16
updated: 2026-07-16
---

# Task: Tio Workout shared contract v2 and reducer

## Goal

Implement the first Workout engineering stage: pure Kotlin, serializable Phone/Watch contracts for exercise identity, gender-aware media, routines, sessions, sets, timer state, durable mutation envelopes, and deterministic reducer transitions.

## Acceptance Criteria

- [x] `WORKOUT_CONTRACT_VERSION` identifies the new serialized contract generation.
- [x] Exercise identity is stable across `MALE`, `FEMALE`, and `NEUTRAL` media variants.
- [x] Media preference supports `AUTO`, explicit override, approved-asset filtering, exact match, neutral fallback, and placeholder fallback.
- [x] Routine, session, exercise-entry, set, timer, and mutation contracts are pure Kotlin and serializable.
- [x] `WorkoutReducer` applies valid mutations deterministically and rejects invalid state transitions without mutating input state.
- [x] `WorkoutRepository` exposes catalog, routine, active state, history, and atomic mutation boundaries without Android dependencies.
- [x] Serialization, resolver, reducer, and shared architecture tests pass.
- [x] Community/social contracts, UI screens, Room, network, Supabase, and Wear runtime changes are not introduced in this stage.

## Scope

- `apps/shared/src/main/java/com/tnyx/shared/workout/`
- `apps/shared/src/test/java/com/tnyx/shared/workout/`
- Canonical Workout docs and implementation-status checkpoint updates required by the contract change.

## Out Of Scope

- Phone Compose screens, components, navigation redesign, or Settings UI.
- Room entities, DAOs, migrations, repositories, outbox implementation, or process-death recovery.
- Exercise catalog import or use of the checked-in Lyfta-attributed Wear catalog.
- Wear Data Layer, real sync, sensors, notifications, or workout execution.
- Backend, Supabase, cloud sync, or external systems.
- Lyfta Community, social feed, posts, comments, follows, friends, groups, challenges, or leaderboards.
- Commit, push, Pull Request, merge, or deployment unless separately requested.

## Current State

- Phone Workout remains a placeholder.
- `apps/shared` has initial `WorkoutSession`, `WorkoutRoutine`, `WorkoutSet`, and `WorkoutRepository` types but no reducer or media contract.
- There are no checked-in callers constructing the current Workout models outside `apps/shared`, so a versioned contract replacement is currently low-risk.
- Wear gender-media behavior is a boolean prototype and is not the target contract.

## Decisions

- Community is an explicit product `SKIP`, not a post-90-day deferred capability.
- Shared contracts remain platform-neutral and KMP-ready.
- One canonical exercise identity owns zero or more approved presentation media variants.
- The resolver never silently switches male media to female media or female media to male media.
- Only assets with explicit approved release status and non-empty provenance participate in resolution.
- Reducer output is deterministic; persistence idempotency and transaction enforcement belong to the next Room stage.
- Existing unrelated Profile commits and untracked `.obsidian/` and `docs/apps/` content remain untouched.

## Validation

- Command: `./gradlew.bat :shared:compileKotlin`
- Result: BUILD SUCCESSFUL.
- Command: `./gradlew.bat :shared:test`
- Result: BUILD SUCCESSFUL; 15 tests, 0 failures, 0 errors, 0 skipped.
- Command: `./gradlew.bat :shared:test :app:compileDebugKotlin :wear:compileDebugKotlin`
- Result: BUILD SUCCESSFUL; shared contracts remain compatible with Phone and Wear compilation.
- Command: forbidden platform-token scan under `apps/shared/src/main/java`.
- Result: PASS; no platform-specific tokens were introduced.

## Files Touched

- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/model/`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/logic/`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/repository/WorkoutRepository.kt`
- `apps/shared/src/test/java/com/tnyx/shared/workout/`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/docs/ARCHITECTURE_CHANGELOG.md`
- `.ai/CURRENT.md`
- `.ai/tasks/cross-platform/local-20260716-workout-contract-v2.md`

## Next Action

Start Workout Stage 2 on a separate checkpoint: Phone persistence schema, atomic state snapshot plus mutation storage, repository implementation, and process-recovery tests. Do not start production Workout screens before that boundary passes.

## Canonical References

- `AGENTS.md`
- `apps/AGENTS.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/shared/src/main/java/com/tnyx/shared/workout/`
