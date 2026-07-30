# Android Onboarding Foundation

Status: In Progress
Updated: 2026-07-30

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
- Added typed `RootRoute.Onboarding` registration and feature-owned navigation.
- Added `OnboardingRoute`, generic `OnboardingScreen`, `OnboardingViewModel`,
  `OnboardingUiState`, actions, and navigation effects.
- Added serialized load/save operations, required-answer validation,
  next/back/skip behavior, local completion, and persistence retry handling.
- Added ViewModel tests for fresh start, resume, answer persistence, boundaries,
  skip, exit, completion, validation, and read/write recovery.
- Added usable Profile steps for name, gender, and date of birth using core
  Tnyx input, card, button, theme, and date-picker primitives.
- Added Profile-specific validation and stable persisted values: trimmed-length
  name validation, gender IDs, and ISO date-of-birth answers.
- Extended `TnyxDatePickerDialog` with reusable supporting text and bounded
  minimum/maximum date inputs.
- Split Welcome navigation so `Get Started` enters onboarding while `Skip`
  continues directly to Main.
- Added Profile validation and Welcome routing tests.

## Next

- Implement Body Goal steps: primary goal, height, current weight, target
  weight, and activity level.
- Keep Workout and Review delivery as later focused slices.

## Truth Boundary

- Profile name, gender, and date-of-birth forms are implemented.
- Welcome `Get Started` now enters onboarding, but the flow becomes incomplete
  at the first Body Goal step because that section is not implemented yet.
- Welcome `Skip` retains the existing direct-to-Main behavior.
- Local checkpoint persistence is device-owned and is not connected to an
  authenticated account or final business repositories.
- No backend, Supabase synchronization, remote config, analytics, or business
  repository finalization is implemented by this checkpoint.

## Validation

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL; 57 tests, 0 failures, 0 errors.
