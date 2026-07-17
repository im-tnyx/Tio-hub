---
task_id: local-20260716-workout-persistence-v1
title: Workout Phone persistence and recovery boundary
status: review
priority: high
owner: android
branch: codex/workout-persistence-v1
github_issue:
github_pr:
platforms: [android]
created: 2026-07-16
updated: 2026-07-17
---

# Task: Workout Phone persistence and recovery boundary

## Goal

Persist the shared Workout contract on Phone so an active session and its last completed set survive process death without duplication, reordering, or partial snapshot/outbox writes.

## Acceptance Criteria

- [x] Room is configured in `apps/app` with exported schemas and an explicit database version.
- [x] Workout snapshot and mutation outbox entities preserve stable shared identifiers and contract version.
- [x] Snapshot and accepted mutation writes are committed in one Room transaction.
- [x] Duplicate mutation IDs are idempotent and mutation sequence ordering is enforced.
- [x] `RoomWorkoutRepository` implements the shared persistence boundary without leaking Room types.
- [x] Hilt provides the database, DAO, and repository implementation from `apps/app`.
- [x] Tests prove active-session recovery, last-completed-set recovery, duplicate protection, ordering rejection, finish/discard behavior, and transaction rollback.
- [x] Phone and Wear compile against the narrowly extended shared rejection contract.

## Scope

- `apps/app/build.gradle.kts`
- `apps/gradle/libs.versions.toml`
- `apps/app/src/main/java/com/tnyx/data/workout/`
- `apps/app/src/main/java/com/tnyx/di/`
- `apps/app/src/test/` and `apps/app/src/androidTest/` where the persistence risk requires them
- Canonical Workout progress and architecture checkpoints

## Out Of Scope

- Workout Compose screens, navigation redesign, or production UI.
- Exercise catalog import, media download/cache, or third-party assets.
- Backend, Supabase, cloud sync, or remote mutation delivery.
- Wear Data Layer, sensors, notifications, or Wear workout execution.
- Community/social capability.
- Commit, push, Pull Request, merge, or deployment unless separately requested.

## Current State

- Shared Workout contract v2, deterministic reducer, media resolver, and unit tests are merged on `main`.
- `apps/app` now contains Phone Room persistence v1, Hilt composition, and the `RoomWorkoutRepository` implementation on this branch.
- Full shared/Phone/Wear validation gate passed on 2026-07-17 with the local JDK at `G:\dev\jdk-17`.
- `docs/apps/PROFILE_SETTINGS_GUIDE.md` is unrelated untracked user content and must remain untouched.

## Workstreams

### Android Owner

- [x] Add Room dependencies, schema export, and database foundation.
- [x] Add snapshot/outbox entities, DAO queries, and atomic transaction boundary.
- [x] Map serialized shared state and mutations without Android types crossing into `apps/shared`.
- [x] Implement and bind `RoomWorkoutRepository`.
- [x] Add recovery, idempotency, ordering, rollback, and migration tests.

## Decisions

- Store the canonical shared state and mutation payload as versioned JSON inside Room-owned rows; stable IDs, sequence, status, timestamps, and contract version remain queryable columns.
- Apply the pure `WorkoutReducer` before persistence, then atomically write the resulting snapshot and accepted mutation row.
- Treat the local outbox as durable local intent only; remote delivery is explicitly out of scope.
- Do not create Workout UI until the persistence/recovery exit gate passes.

## Blockers And Open Questions

- No blocking issue remains for the Stage 2 exit gate.
- Room schema v1 exports successfully under the ignored app build directory; version-to-version migration coverage begins when schema v2 is introduced.

## Files Touched

- `.ai/CURRENT.md`
- `.ai/tasks/android/local-20260716-workout-persistence-v1.md`
- `apps/gradle/libs.versions.toml`
- `apps/app/build.gradle.kts`
- `apps/app/src/main/java/com/tnyx/data/workout/`
- `apps/app/src/main/java/com/tnyx/di/WorkoutDataModule.kt`
- `apps/app/src/test/java/com/tnyx/data/workout/RoomWorkoutRepositoryTest.kt`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/logic/WorkoutReducer.kt`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/docs/ARCHITECTURE_CHANGELOG.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`

## Validation

- Command: `./gradlew.bat :app:compileDebugKotlin`
- Result: PASS after Room/KSP setup and mapper correction.
- Command: `./gradlew.bat :app:clean :app:testDebugUnitTest --tests com.tnyx.data.workout.RoomWorkoutRepositoryTest`
- Result: PASS; 6 tests, 0 failures, 0 errors, 0 skipped. File-backed reopen and transaction rollback are covered.
- Command: Room schema export inspection.
- Result: PASS; `WorkoutDatabase` schema version 1 generated under `apps/app/build/roomSchemas/` and remains ignored build output.
- Command: `git diff --check`
- Result: PASS at the implementation checkpoint.
- Command: `./gradlew.bat :shared:test :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin`
- Result: PASS on 2026-07-17 with `JAVA_HOME=G:\dev\jdk-17`; full shared, Phone, and Wear validation gate passed.

## Next Action

Stage 2 is review-ready. On explicit user request, commit and push this branch for review; after landing, switch the active task to Stage 3 (`First thin offline vertical slice`).

## Canonical References

- `AGENTS.md`
- `.ai/CURRENT.md`
- `apps/AGENTS.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/shared/src/main/java/com/tnyx/shared/workout/`
