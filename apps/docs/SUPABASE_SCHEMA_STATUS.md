# Supabase Schema Status

Last verified: 2026-08-08

This document records which Supabase objects currently exist for Tio-hub and
which data areas may need tables later. Update it after every approved schema,
RLS, grant, view, function, or storage change.

## Truth Boundary

- Checked-in files under `supabase/migrations/` are migration source truth.
- The connected Supabase database is live-state truth after a migration is
  applied and verified.
- This document is an inventory and planning record. It contains no executable
  SQL and does not apply database changes.
- `LIVE` means the object exists remotely. It does not mean the complete
  feature, authentication flow, synchronization, or production rollout exists.

## Connected Project

| Field | Current value |
|---|---|
| Project name | `Tio-hub` |
| Project ref | `ublwxylwdqjdykqcncuv` |
| Region | `ap-south-1` |
| Profile image bucket | `tio-profile` |

API keys, database passwords, connection strings, and service-role credentials
must not be added to this document.

## Applied Migrations

| Version | Migration | Status | Purpose |
|---|---|---|---|
| `20260729150842` | `bootstrap_user_profiles` | `LIVE` | Creates the initial user profile, nutrition profile, workout profile, auth identity, profile view, RLS, grants, indexes, and profile image storage policy baseline. |
| `20260729151026` | `deny_direct_auth_identity_access` | `LIVE` | Explicitly denies direct authenticated access to provider identity mapping rows. |
| `20260730112203` | `add_profiles_mobile_column` | `LIVE` | Adds `profiles.mobile` so active Android profile sync can persist phone-number profile data remotely. |
| `20260801084033` | `nutrition_meal_logs` | `LIVE` | Creates `meal_logs` and `meal_log_items` with owner-scoped RLS for the Android nutrition diary slice. |
| `20260808130626` | `workout_custom_exercises` | `LIVE` | Creates `custom_exercises` with owner-scoped RLS for authenticated custom-exercise save and read paths. |

Migration source:

- `supabase/migrations/20260729150842_bootstrap_user_profiles.sql`
- `supabase/migrations/20260729151026_deny_direct_auth_identity_access.sql`
- `supabase/migrations/20260730193000_add_profiles_mobile_column.sql`
- `supabase/migrations/20260731000000_nutrition_meal_logs.sql`
- `supabase/migrations/20260808113000_workout_custom_exercises.sql`

## Current Live Objects

| Object | Type | Owner | Status | Current role |
|---|---|---|---|---|
| `public.profiles` | Table | Profile | `LIVE` | User identity and public profile fields, including `username`, display name, contact metadata, avatar path, onboarding state, streak counters, and referral linkage. |
| `public.user_nutrition_profiles` | Table | Nutrition | `LIVE` | User-owned body, goal, diet, target, and onboarding snapshot data. Sleep fields are currently input snapshots only; future Recovery calculations do not belong here. |
| `public.user_workout_profiles` | Table | Workout | `LIVE` | User-owned workout preferences such as experience, location, equipment, duration, training days, split, focus areas, and health concerns. |
| `public.auth_identities` | Table | Backend/Auth | `LIVE` | Server-controlled mapping point for current Supabase identity and a possible future Firebase/provider identity. Direct authenticated access is denied. Firebase runtime is not implemented yet. |
| `public.meal_logs` | Table | Nutrition | `LIVE` | One meal entry per user, date, and meal type for the nutrition diary repository path. |
| `public.meal_log_items` | Table | Nutrition | `LIVE` | User-owned food items attached to a `meal_logs` row, with macro fields used by Android meal CRUD. |
| `public.custom_exercises` | Table | Workout | `LIVE` | User-owned custom exercise definitions for the Android Workout library create/search flow. |
| `public.profile_overview` | View | Profile | `LIVE` | Security-invoker read model that combines the current profile, nutrition snapshot, and workout preference data. |
| `storage.buckets:tio-profile` | Storage bucket | Profile | `LIVE` | Public profile images only; current limit is 5 MB and allowed MIME type is `image/jpeg`. |

## Current Security And Data State

- RLS is enabled on all seven public tables.
- `profiles`, `user_nutrition_profiles`, `user_workout_profiles`, `meal_logs`,
  `meal_log_items`, and `custom_exercises` use
  owner-scoped authenticated policies.
- Direct authenticated access to `auth_identities` is explicitly denied.
- `profile_overview` uses the caller's permissions through
  `security_invoker`.
- The Supabase Security Advisor returned zero findings after the current
  migrations were applied.
- No demo user or profile row was inserted.
- One manual verification meal log and one meal-log item were inserted on
  2026-08-01 for an existing authenticated profile to prove the live nutrition
  diary schema and write path.
- The connected project reports `custom_exercises` live with `authenticated`
  owner-scoped `ALL` policy on 2026-08-08. Table-level grant metadata still
  shows default `anon` privileges, but no `anon` RLS policy exists for this
  table, so row access remains denied for anonymous callers.
- The fresh database currently reports two expected unused-index information
  notices for `profiles_referred_by_id_idx` and
  `auth_identities_user_id_idx`. Re-evaluate them after real workload exists.

## Runtime Integration Status

| Area | Status | Boundary |
|---|---|---|
| Profile read/write repository | `ACTIVE` | Android binds `ProfileRepository` to `SupabaseProfileRepository`; Personal Information writes profile and supported nutrition fields, and remote write failures reach the UI. |
| Profile image upload | `ACTIVE` | Android avatar updates now write to the live `tio-profile` bucket and update `profiles.avatar_url`. |
| Username editing | `ACTIVE` | Personal Information saves an optional, validated username with the other profile identity fields. |
| Real auth source | `ACTIVE CLIENT SOURCE` | Android now binds `AuthRepository` to a Supabase-backed implementation for email/password, Google OAuth start, email OTP verification, and sign-out. Backend authority is still a future contract question, but fake local auth is no longer the active source. |
| Firebase identity linking | `NOT IMPLEMENTED` | `auth_identities` reserves a safe server-owned mapping boundary only. |
| Nutrition diary persistence | `SCHEMA LIVE / CLIENT PATH PARTIAL` | Android source targets live `meal_logs` and `meal_log_items`, and the connected project now contains both tables plus one verified write. Device-side Meal Diary and Meal Editor route validation still remains a separate runtime check. |
| Onboarding owner-row sync | `ACTIVE` | Onboarding completion writes schema-supported profile, nutrition, and workout preference answers to the authenticated user's owner-scoped rows. Source attribution and answers without owner columns remain local only. |
| Workout custom exercise persistence | `SCHEMA LIVE / CLIENT PATH ACTIVE` | Android now targets live `custom_exercises` rows through an app-owned repository that merges bundled catalog data with owner-scoped Supabase rows and Room cache. Media upload and broader workout sync remain separate work. |
| Workout cloud sync | `PARTIAL` | Onboarding preferences sync to `user_workout_profiles`, and custom exercise library rows now persist remotely; workout plans, sessions, and set history remain local-only/not implemented. |

## Future Table Backlog

These names are candidates, not approved schema and not created objects.
Confirm the real feature contract before choosing final names or columns.

| Feature slice | Candidate objects | Status | Creation trigger |
|---|---|---|---|
| Auth/session | Provider-link RPCs or server API around `auth_identities` | `PLANNED` | Real Supabase/Firebase/backend auth source is approved. |
| Onboarding | `onboarding_progress`, `onboarding_answers` | `PLANNED` | Resume-after-restart and multi-step persistence are implemented. Reuse existing profile tables instead of duplicating owned fields. |
| Nutrition targets | A dedicated `nutrition_targets` table only if the current profile snapshot no longer fits | `PLANNED` | Nutrition needs versioned or independently managed targets. |
| Nutrition diary catalog/search | `food_items` | `PLANNED` | Food catalog/search still needs its approved runtime contract after the live `meal_logs` base. |
| Workout library | `exercise_catalog` plus licensed/provenance-safe media metadata | `PLANNED` | Full first-party/shared exercise catalog truth and approved media metadata still need their final runtime contract beyond user-owned `custom_exercises`. |
| Workout planning | `workout_plans`, `workout_plan_days`, related exercise ordering | `PLANNED` | Routine creation requires cloud persistence. |
| Workout execution | `workout_sessions`, `workout_sets` | `PLANNED` | Phone workout runtime requires sync or server history. Existing local Room behavior remains the current runtime boundary until then. |
| Progress | `weight_logs`, `body_measurements`, `progress_photos`, `user_achievements` | `PLANNED` | Each Progress screen gains a repository-backed vertical slice. |
| Health | `health_connections`, provider sync metadata | `PLANNED` | A Health module and provider integration are approved. |
| Recovery | `sleep_records`, `recovery_snapshots` | `PLANNED` | Recovery owns sleep, HRV, readiness, and recovery calculations. |
| Billing | `subscriptions`, `entitlements` or backend-owned equivalents | `PLANNED` | Billing provider and server-side entitlement authority are selected. |
| Referrals/rewards | `referrals`, `referral_rewards` | `PLANNED` | Reward qualification, fraud controls, and entitlement grant flow are specified. |
| Social/community | `friendships`, `activity_posts`, moderation/reporting objects | `FUTURE` | Community scope, privacy, moderation, and safety requirements are approved. |

Do not create all backlog tables upfront. The table list must shrink, change, or
split when the actual feature contract proves a better model.

## Table Addition Gate

Before adding a new table or storage object:

1. Approve the feature owner and concrete runtime use case.
2. Define the repository/API contract and minimum data shape.
3. Check whether an existing table already owns the data.
4. Add a timestamped migration under `supabase/migrations/`.
5. Add least-privilege grants, RLS policies, required constraints, and justified
   indexes in the same slice.
6. Keep service-role access and provider identity linking outside public
   clients.
7. Apply only to the explicitly approved project.
8. Verify owner access, cross-user denial, anonymous denial, and advisor output.
9. Avoid production demo data and personal health data in seed files.
10. Update this document and `ANDROID_APP_PROGRESS.md` with the verified result.

## Update Checklist

When the schema changes, update all applicable sections in this file:

- `Last verified`
- `Applied Migrations`
- `Current Live Objects`
- `Current Security And Data State`
- `Runtime Integration Status`
- `Future Table Backlog`

Record only what was actually applied and verified. Keep planned objects marked
`PLANNED` or `FUTURE` until live database evidence confirms them.
