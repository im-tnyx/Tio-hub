---
task_id: local-20260804-search-exercise-library
title: Load Workout Exercise Library into SearchExercises Screen
status: completed
priority: high
owner: codex
branch: feat/android-search-exercise-library
github_issue:
github_pr:
platforms: [android, wear]
created: 2026-08-04
updated: 2026-08-07
---

# Task: Load Workout Exercise Library into SearchExercises Screen

Allowed status values: `planned`, `ready`, `in-progress`, `blocked`, `review`, `completed`, `cancelled`.

## Goal

Load the canonical Workout Exercise Library JSON dataset (supporting gender-specific media URLs for video, thumbnail, and image, localized titles/instructions, muscle groups, equipment, and tracking types) into `SearchExercisesScreen` (`apps/features/workout/presentation/library/exercises/`) for Phone and Wear OS integration.

## Acceptance Criteria

- [x] Exercise JSON DTO schema matches `exerciseData.json` format (`id`, `title`, `muscle_group`, `other_muscles`, `equipment_category`, `thumbnail_url`, `thumbnail_url_female`, `url`, `url_female`, `instructions`, etc.).
- [x] Shared domain model (`ExerciseDefinition`, `ExerciseMediaAsset`, `ExerciseMediaResolver`, `ExerciseCatalogParser`) maps the JSON dataset in `apps/shared/`.
- [x] `SearchExercisesScreen` and ViewModel load, search, filter, and display the exercise list cleanly using `TnyxTheme` and design system primitives.
- [x] Media URLs (video, thumbnail, image) resolve correctly for male/female variants.
- [x] Persist user's `ExerciseViewType` (GRID/LIST) choice via Jetpack DataStore (`DataStoreExerciseViewPreferencesRepository`).
- [x] Empirical build verification (`:shared:test`, `:features:workout:test` & `:app:assembleDebug`) succeeds.

## Scope

- Shared exercise JSON schema DTO mapping in `apps/shared/` (preserving `instructions`, `url`, `thumbnail_url`, `url_female`, `thumbnail_url_female` for future AboutExercise screen)
- Workout exercise repository / catalog loader integration
- `SearchExercisesScreen`, `SearchExercisesViewModel`, and `SearchExercisesContract` exercise list, search, and filter integration
- Gender-aware media URL resolution (Male/Female video, thumbnail, image)
- Persistence of user's List/Grid view mode preference via DataStore

## Out Of Scope

- Detailed `AboutExercise` / Exercise Info screen (video loop, history tab, full localized instructions presentation will be specified in a future plan)
- Modifying checked-in Supabase DB schema migrations
- Copying third-party visual branding into `TnyxTheme`
- Non-workout features (Auth, Profile, Settings, Onboarding, Nutrition)

## Current State

- Repository: `G:\projects\Tio-hub`
- Active branch: `feat/android-search-exercise-library`.
- Target presentation screen: `apps/features/workout/src/main/java/com/tnyx/features/workout/presentation/library/exercises/SearchExercisesScreen.kt`.

## Workstreams

### Primary Owner

- [x] Create task file and update `.ai/CURRENT.md`
- [x] Add JSON serialization model and catalog loader for exercise library
- [x] Connect `SearchExercisesViewModel` to load and filter exercise catalog
- [x] Implement List/Grid view toggle with scrollable section header and custom drawables
- [x] Implement DataStore persistence for `ExerciseViewType` preference
- [x] Verify UI rendering, video/thumbnail resolution, and unit tests

## Validation Plan

- Command: `./gradlew.bat :shared:test :features:workout:test`
- Command: `./gradlew.bat :app:assembleDebug`

