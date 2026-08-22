# Task: Dynamic Ring-Based Weekly Calendar System & First Day of Week Settings

## Status
- State: Completed
- Primary Owner: Android (Clean Architecture + Tnyx UI)
- Created: 2026-08-08
- Merged: 2026-08-09 via PR #35
- Merge Commit: `dc09c48b04553e97ee736b8fa6cb22ceaa578025`

## Objective
Enhance `TnyxWeeklyCalendar` with configurable starting day of week (Default: Sunday) driven by App Settings BottomSheet, day/date layout swap (Day name on top, Date number below inside ring badge), 3-tier dynamic rings (Planned Outline, Calorie Progress Arc, Workout Solid Achievement Fill), multi-schedule dots below the date badge, and Samsung Health-inspired `Pumpkin` `#EB7B36` color token for planned targets.

## Scope & Architectural Alignment
- Add `TnyxPalette.Pumpkin = Color(0xFFEB7B36)` foundation token.
- App Preferences:
  - Add `FirstDayOfWeekBottomSheet` in `AppPreferencesScreen.kt` (Sunday Default, Monday, Saturday).
  - Pass configured `firstDayOfWeek` to `TnyxWeeklyCalendar`.
- Update `TnyxWeeklyCalendar.kt`:
  - Support `firstDayOfWeek` parameter (Default: Sunday).
  - Day of week on top, Date number inside 3-tier dynamic ring below.
  - Multi-schedule dots container below the date ring for days with multiple scheduled sessions.
  - Support `Pumpkin` `#EB7B36` planned outline rings & scheduled workout dots.
- Integrate with `WorkoutScreen.kt` and `MealDiaryScreen.kt`.

## Verification
- Aggregate Android validation passed with `:shared:test`, core/workout/onboarding/settings unit tests, `:app:testDebugUnitTest`, and `:app:assembleDebug`.
- Repository diff checks passed before merge.
- User accepted the integrated runtime state before PR merge.
