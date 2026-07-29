# Android Onboarding Foundation

Status: In Progress
Updated: 2026-07-29

## Objective

Build a small section-based Android onboarding foundation that can grow into a
dynamic flow without replacing stable section and step identities.

## Decisions

- Tio-hub runtime source is authoritative; Tnyx-hub is reference material only.
- Version 1 uses `PROFILE`, `BODY_GOAL`, `WORKOUT`, and `REVIEW` sections.
- Persisted positions use explicit string IDs, never enum ordinals or numeric
  step indexes.
- Screens will follow `Route + Screen + ViewModel + UiState + Action`.
- Onboarding collects answers, but final business ownership remains with
  Profile, Nutrition, Workout, Health, and Recovery repositories.
- Backend synchronization remains deferred.

## Completed

- Added validated `OnboardingSectionId` and `OnboardingStepId` contracts.
- Added a versioned flow definition with deterministic next/previous behavior.
- Added the simple version 1 section/step definition.
- Added tests for ordering, section boundaries, serialization, insertion-safe
  positions, and invalid definitions.
- Added canonical Tio onboarding architecture documentation.
- Added typed `OnboardingDraft`, versioned `OnboardingProgress`, and atomic
  `OnboardingCheckpoint` contracts.
- Added `OnboardingRepository` in the feature and an app-owned Preferences
  DataStore implementation with Hilt binding.
- Added deterministic resume validation that preserves compatible checkpoints
  and resets stale versions, unknown positions, or unknown IDs.
- Added feature serialization/resume tests and app Robolectric persistence
  recreation tests.

## Next

- Add the Onboarding graph/container and render state through actions.
- Implement one simple section at a time, starting with Profile.

## Truth Boundary

- No onboarding section screen is implemented by this checkpoint.
- Local checkpoint persistence is device-owned and is not yet connected to a
  ViewModel, route, authenticated account, or final business repositories.
- No backend, Supabase synchronization, remote config, analytics, or completion
  finalization is implemented by this checkpoint.

## Validation

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL; 39 tests, 0 failures, 0 errors.
