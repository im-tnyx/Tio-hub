# Android Supabase Sync Baseline

Status: In Progress
Updated: 2026-07-30

## Required Context

- `.ai/core/architecture-summary.md`
- `.ai/core/coding-rules.md`
- `.ai/core/supabase-rules.md`
- `.ai/task-playbooks/android.md`
- `.ai/task-playbooks/supabase.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`

## Objective

Remove fake/local-only runtime truth from the currently visible Android
Auth/Profile/Nutrition baseline so Supabase-backed data becomes the primary
source where the app already has screens.

## Why This Task Exists

- `FakeAuthRepository` and local-only profile wiring were still the active
  runtime source even after Supabase schema foundations existed.
- `MealDiaryViewModel` and related nutrition flows still relied on fake or
  sample bootstrap data.
- The live schema already contains `profiles`, `user_nutrition_profiles`, and
  `user_workout_profiles`, so visible user-owned surfaces should stop behaving
  like demo-only screens.
- `meal_logs`, `meal_log_items`, and `food_items` are still planned, so this
  task must stop at a truthful bootstrap boundary rather than inventing fake
  persistence.

## Runtime Goal

Deliver this boundary:

```text
Login / Signup / OTP
  -> AuthRepository
  -> app-owned SupabaseAuthRepository
       -> Supabase Auth session truth

Profile / Personal Information / Avatar
  -> ProfileRepository
  -> app-owned SupabaseProfileRepository
       -> live `profiles`
       -> live `user_nutrition_profiles`
       -> live `tio-profile` storage

MealDiaryViewModel
  -> NutritionRepository
  -> app-owned NutritionBootstrapRepository
       -> live `user_nutrition_profiles` targets when an authenticated
          Supabase session exists
       -> empty diary content until real meal-log tables exist
```

## Current Checkpoint

- Active `AuthRepository` binding now uses `SupabaseAuthRepository`.
- Login email/password, signup, OTP verification, resend OTP, Google OAuth
  start, and sign-out now route through Supabase-backed auth code.
- Active `ProfileRepository` binding now uses `SupabaseProfileRepository`.
- Profile reads now come from remote `profiles` and
  `user_nutrition_profiles` rows for the authenticated user.
- Avatar upload/remove now updates the live `tio-profile` bucket and
  `profiles.avatar_url`.
- Added migration `20260730193000_add_profiles_mobile_column.sql` and applied
  it to the connected Tio-hub project so profile mobile data can persist
  remotely.
- `MealDiaryViewModel` now loads through `NutritionRepository` instead of
  owning hardcoded sample truth.
- `NutritionBootstrapRepository` now reads live
  `user_nutrition_profiles` targets when a real Supabase session exists and
  otherwise falls back to zero/default values.
- `NutritionTargetsViewModel` now loads and saves through
  `NutritionRepository`, so visible nutrition target edits no longer stop at
  local-only UI state.
- Fake seeded diary meals, water, vitamin, and mineral progress are removed;
  meal diary content is intentionally empty until a real meal-log slice exists.
- Profile and nutrition target reads refresh while observed, using a polling
  boundary rather than claiming realtime subscriptions.

## Boundary Notes

- This task does not create `meal_logs`, `meal_log_items`, or `food_items`.
- This task does not make nutrition cloud persistence complete.
- This task does not claim realtime DB subscriptions; current refresh is
  polling-driven.
- This task does not expose service-role or admin credentials to Android.
- This task does not replace the future backend-mediated architecture.
- `user_workout_profiles` remains a live schema foundation, but current visible
  UI does not yet surface a full workout-profile sync slice.

## Next

1. Add the first real nutrition persistence slice:
   `meal_logs`, `meal_log_items`, and optional `food_items`.
2. Move Meal Editor and Meal Item Editor onto the same repository boundary.
3. Decide whether profile/nutrition refresh should stay polling-based or move
   to an approved realtime/backend contract later.
4. Extend Supabase-backed runtime truth into onboarding-owned remote writes
   without breaking the future backend boundary.

## Validation

- `./gradlew.bat :features:nutrition:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

## Truth Boundary

- This file records the Android Supabase sync baseline slice only.
- Runtime source remains the actual behavior truth.
