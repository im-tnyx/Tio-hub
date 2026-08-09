# Supabase Incremental Setup Plan

Last updated: 2026-07-30

This document is the memory anchor for moving TNYX away from hardcoded demo data
toward verified Supabase schema foundations that a future backend can consume,
one feature slice at a time. Android/Wear client data access follows the
backend-mediated boundary in `BACKEND_TRANSITION_PLAN.md`.


## Why This Document Exists

This plan exists to stop hardcoded data from becoming product architecture.

Hardcoded/demo values are allowed only as temporary UI scaffolding while a slice is not wired yet. They are not the source of truth. Each feature should graduate to:

- a repository contract
- a local development implementation or future backend-backed implementation
- local/dev seed data
- RLS validation
- app integration tests or manual read/write validation

If a value affects user-owned behavior, persistence, entitlement, onboarding resume, nutrition targets, workout history, health data, or progress history, it must eventually come from a repository rather than a permanent ViewModel/sample object.
This is a planning document only.

- No executable SQL lives here.
- No migration is created by this document.
- No Supabase apply, push, or remote schema change is implied.
- No service key belongs in Android, Flutter, web public code, or admin client bundles.

## Current Repo Reality

Current checkout is Android/Wear focused:

- `apps/app`: Android app entry, routing, DI glue.
- `apps/features/*`: feature-owned presentation and navigation.
- `apps/shared`: pure Kotlin domain models and repository interfaces.
- `apps/wear`: Wear OS app foundation.
- `apps/docs`: current checked-in documentation location.

The root `.env` exists locally, but env values must never be documented or committed.

## Current Live Baseline

The connected Tio-hub Supabase project now has an initial Profile vertical-slice
baseline:

- `users`
- `user_nutrition_profiles`
- `user_workout_profiles`
- locked `auth_identities`
- security-invoker `profile_overview`
- `tio-profile` storage bucket

This baseline does not make Auth, Nutrition Diary, Workout cloud sync, or the
full Profile flow complete. The verified object inventory, applied migration
versions, current security state, and future table backlog are maintained in
[SUPABASE_SCHEMA_STATUS.md](SUPABASE_SCHEMA_STATUS.md).

Android now has an active Supabase-backed Auth/Profile path for the currently
authenticated user, and the current nutrition bootstrap path reads live
`user_nutrition_profiles` targets instead of permanent fake goal values. This
still does not mean every feature is fully synced yet: nutrition meal logs,
workout history, progress logs, and future backend-owned contracts still need
their own runtime slices.

## Future Turborepo / TypeScript Boundary

This checkout is Android/Wear focused today, but TNYX is expected to move toward a larger TypeScript/Turborepo monorepo later.

Future target ownership should be:

```text
apps/
├── android/ or mobile Android app       # Kotlin client
├── flutter/                             # Cross-platform client if retained
├── web/                                 # Next.js user-facing app/dashboard
└── admin-panel/                         # Operational admin UI

backend/                                 # Express/TypeScript APIs, services, jobs
shared/                                  # TypeScript contracts, constants, Zod schemas
    └── packages/contracts/              # API DTOs, route contracts, generated DB types when approved

database/                                # Supabase/Postgres migrations, RLS, RPCs, seed policy
infra/                                   # Docker, env templates, CI/deploy config
```

Rules for the future TypeScript repo:

- Database truth lives in `database/`, not in mobile, web, or admin UI code.
- Backend service-role operations live in `backend/` only.
- Public clients never receive service keys, admin keys, or direct privileged SQL access.
- Shared API DTOs and validation schemas live in `shared` / contracts, not copied per client.
- Android, Flutter, web, and admin consume repository/API contracts instead of hardcoding table shapes.
- Demo data remains local/dev seed data, not production default data.
- A feature table is created only when a runtime slice needs it.
- Supabase RLS and grants are part of the slice, not a later cleanup task.

This means Android repository contracts built today must accept a backend API
implementation later without rewriting Screens.


## Source Of Truth Rules

Use these rules when replacing hardcoded values:

- `Screen` renders `UiState`; it never owns sample data as business truth.
- `ViewModel` may expose temporary mock state only before a repository exists.
- Once a slice has a repository, ViewModel data must come from that repository.
- Demo data lives in local/dev seed files or dev-only scripts.
- Runtime feature ownership follows `PROFILE_SETTINGS_GUIDE.md`.
- Schema/table ownership follows the same feature owner that owns the business logic.
- If Android and Wear both need the model, put the pure Kotlin contract in `apps/shared` now.
- If future TypeScript clients need the model, mirror/derive the API contract in future `shared` TypeScript contracts instead of copying ad hoc shapes.
## Core Decision

Do not create the entire database upfront.

Create tables only when a feature needs them, and ship each slice with:

- migration
- RLS
- grants
- indexes
- seed/demo data
- repository/API contract
- app integration
- validation steps

This keeps the schema easy to reason about and prevents stale tables that no runtime path uses.

## Security Rules

Use these rules for every Supabase slice:

- Production mobile business-data flows call backend APIs rather than protected
  Supabase tables or storage directly.
- `SUPABASE_SERVICE_ROLE_KEY` is server/local tooling only.
- Every `public` table must have RLS enabled.
- User-owned rows must include a stable owner column, normally `user_id uuid references auth.users(id)`.
- Policies must combine role and ownership checks.
- `TO authenticated` alone is not authorization.
- `UPDATE` policies need both `USING` and `WITH CHECK`.
- Do not use user-editable metadata for authorization.
- Avoid `SECURITY DEFINER` unless there is a reviewed reason and a tight execution boundary.
- Demo seed data is allowed for local/dev only, not production by default.

## Slice Workflow

Use this workflow for each feature.

1. Define the app behavior.
2. Identify the minimum tables needed.
3. Define ownership and RLS rules.
4. Create the migration with Supabase CLI or MCP tooling.
5. Add local/dev seed data.
6. Add repository interfaces under `apps/shared` when the model must be shared by phone and watch.
7. Add a local fake/in-memory implementation until the approved backend
   repository exists.
8. Update ViewModels to consume repositories instead of hardcoded state.
9. Keep Screens dumb: render `UiState`, emit `Action`.
10. Validate app contracts with compile/tests and validate schema/RLS through
    approved database tooling; do not add direct client reads to prove a table.
11. Update `SUPABASE_SCHEMA_STATUS.md` with only the objects actually applied
    and verified.


## Contract Boundary

Each slice must define the contract before wiring UI to persistence.

Android/Wear now:

- `apps/shared`: pure Kotlin domain models and repository interfaces for phone/watch reuse.
- `apps/features/<feature>`: Route, Screen, ViewModel, UiState, Action, Effect.
- `apps/app`: DI and platform repository wiring.

Future TypeScript/Turborepo:

- `shared` or `packages/contracts`: TypeScript DTOs, Zod validators, API route contracts, generated DB types when approved.
- `backend`: API services, repositories, jobs, and server-only Supabase clients.
- `database`: migrations, RLS, grants, RPCs, seed policy.
- `web` / `admin-panel`: UI clients that consume API/contracts, not service-role database access.

Do not duplicate contract definitions manually across Android, Flutter, web, and backend. Pick a canonical contract per slice and generate or map carefully from it.
## Feature Order

Recommended sequence:

1. Auth and Profile
2. Nutrition
3. Onboarding
4. Workout
5. Wear sync and offline support

Nutrition can be first if the immediate goal is removing current hardcoded meal data. Auth/Profile must still exist before user-owned RLS can be fully tested with real users.

## Slice 1: Auth And Profile

Purpose:

- Define the future backend Auth/Profile contract and identity mapping.
- Keep local Profile UI testable behind the stable repository contract.
- Establish the database ownership/RLS pattern the backend will enforce.

Likely tables:

- `users`

Demo data:

- Optional local fake profile state in Android.
- No live demo user or profile row is required.

Validation:

- Database policies prevent cross-user Profile access.
- Android Profile UI reads and updates through `ProfileRepository`.
- Future backend contract defines token verification and internal user mapping.

Done when:

- Auth screen is not a TODO destination.
- Profile data comes from the approved backend repository in production.
- No service key is present in client code.

## Slice 2: Nutrition

Purpose:

- Replace hardcoded nutrition ViewModel data with repository-backed data.
- Provide demo meals for local/dev testing.

Likely tables:

- `nutrition_targets`
- `meal_logs`
- `meal_log_items`
- `food_items` if demo/catalog lookup is needed

Demo data:

- Daily nutrition target.
- Seven days of meal logs.
- Breakfast, lunch, dinner, and snack examples.
- A small reusable food catalog.

Validation:

- Meal diary loads from repository.
- Add meal works.
- Edit meal works.
- Delete item works.
- User A cannot read or mutate User B meal rows.

Done when:

- `MealDiaryViewModel`, `MealEditorViewModel`, and `MealItemEditorViewModel` no longer construct permanent sample meals as their source of truth.
- Screens remain dumb UI.
- Nutrition domain models intended for reuse are in `apps/shared`.

## Slice 3: Onboarding

Purpose:

- Persist onboarding answers and resume progress after app restart.
- Avoid storing long-lived onboarding state only in Compose or ViewModel memory.

Likely tables:

- `onboarding_progress`
- `onboarding_answers`
- `health_profile`
- `user_goals`

Demo data:

- One fully completed onboarding user.
- One partially completed onboarding user.

Validation:

- App can resume from the last completed step.
- User can update answers.
- User cannot access another user's onboarding data.

Done when:

- Onboarding flow can survive app restart.
- Resume logic is driven by repository data.
- The route/screen pattern stays `Route + ViewModel + UiState + Action`.

## Slice 4: Workout

Purpose:

- Move workout plans and sessions from placeholders toward persisted data.
- Prepare future phone/watch shared behavior.

Likely tables:

- `exercise_catalog`
- `workout_plans`
- `workout_plan_days`
- `workout_sessions`
- `workout_sets`

Demo data:

- Public exercise catalog.
- One sample workout plan.
- One sample completed workout session.

Validation:

- Workout tabs show real data instead of placeholders.
- Workout session save is atomic.
- User-owned sessions are private.
- Public catalog remains readable without leaking user data.

Done when:

- Workout History, Explore, and Routines are no longer static placeholder screens.
- Shared workout contracts remain usable by phone and watch.

## Demo Data Policy

Demo data is useful, but it must be controlled.

Allowed:

- `seed.sql` for local Supabase.
- dev-only seed scripts using service role keys outside client apps.
- small realistic datasets for UI testing.

Not allowed:

- production demo inserts by default.
- real user health data in seed files.
- service role keys in repo, Android resources, BuildConfig, web public env, or screenshots.

## Validation Checklist Per Slice

Run only checks that apply to the slice:

- `supabase --version`
- `supabase migration list`
- `supabase db reset` for local migration plus seed validation
- `supabase db advisors` when available
- `./gradlew.bat :app:compileDebugKotlin`
- feature-specific unit tests when repository logic is added
- manual signed-in and signed-out RLS checks

## File Ownership

Use these ownership boundaries:

- SQL migrations, RLS, grants, RPCs, and seed data: future `database/` folder, or `supabase/` until the larger repo layout is created.
- Android/Wear shared domain contracts: `apps/shared`.
- Android app wiring and repository implementations: `apps/app`.
- Feature ViewModels, Routes, Screens: `apps/features/<feature>`.
- Wear-specific behavior: `apps/wear`.
- Documentation: `apps/docs` in this checkout.
- Future TypeScript contracts: `shared` / `packages/contracts` after Turborepo migration.
- Future backend Supabase service-role code: `backend`, never client apps.

## Open Decisions

Resolved decisions:

- Android/Wear protected business data will use backend APIs.
- Live Supabase objects remain server-side database/storage foundations.
- Fake/in-memory repositories do not claim persistence or synchronization.

Open decisions before backend implementation:

- Will local Supabase be the default dev workflow?
- Which auth method ships first: email/password, phone OTP, or magic link?
- Where will migration files live: `supabase/migrations` or `database/migrations`?
- What is the canonical repo name for this checkout: `Tio-hub` or `tnyx-hub`?
- Which package owns shared TypeScript contracts after Turborepo migration?
- Which backend runtime, hosting, token, and identity mapping strategy will be
  authoritative?

## Current Practical Focus

Recommended current task:

Add the first truthful post-bootstrap persistence slice instead of restoring
fake local/demo data.

Why:

- Auth and Profile already have an active Supabase-backed client path.
- Nutrition targets already read from live `user_nutrition_profiles`.
- The next highest-value hardcoded gap is date-specific meal persistence.
- Backend runtime can still remain deferred if Android repositories keep a
  stable contract boundary.

Minimum outcome:

- `MealDiaryViewModel` stays repository-driven.
- `meal_logs` and related rows become the first real nutrition diary truth.
- Future backend Profile/Nutrition endpoints can replace client-direct
  Supabase access without rewriting Screens.
