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

## Next

- Add app-owned local draft and progress persistence behind repository
  contracts.
- Add the Onboarding graph/container and render state through actions.
- Implement one simple section at a time, starting with Profile.

## Truth Boundary

- No onboarding section screen is implemented by this checkpoint.
- No draft/resume repository is implemented by this checkpoint.
- No backend, Supabase synchronization, remote config, analytics, or completion
  finalization is implemented by this checkpoint.

## Validation

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :features:onboarding:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL.
