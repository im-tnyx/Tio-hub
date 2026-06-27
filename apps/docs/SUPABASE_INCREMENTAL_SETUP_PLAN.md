# Supabase Incremental Setup Plan

Last updated: 2026-06-27

This document is the memory anchor for moving TNYX away from hardcoded demo data and toward a Supabase-backed app, one feature slice at a time. It also defines how this Android/Wear checkout should evolve toward a future TypeScript/Turborepo backend without leaking database assumptions into clients.


## Why This Document Exists

This plan exists to stop hardcoded data from becoming product architecture.

Hardcoded/demo values are allowed only as temporary UI scaffolding while a slice is not wired yet. They are not the source of truth. Each feature should graduate to:

- a repository contract
- a Supabase or backend-backed implementation
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

This means the Android repository contract built today should be easy to swap from direct Supabase access to backend API access later, without rewriting Screens.


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

- Mobile clients only get `SUPABASE_URL` and a publishable or anon client key.
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
7. Add Supabase or backend repository implementation in the platform-owned layer.
8. Update ViewModels to consume repositories instead of hardcoded state.
9. Keep Screens dumb: render `UiState`, emit `Action`.
10. Validate with compile checks and at least one manual Supabase read/write test.


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

- Prove Supabase Auth session flow.
- Create user-owned profile data.
- Establish RLS pattern for the rest of the app.

Likely tables:

- `profiles`

Demo data:

- One local/dev test user.
- One profile row for that user.

Validation:

- Signed-out users cannot read profiles.
- A signed-in user can read and update only their own profile.
- Android auth route can reach a logged-in session.

Done when:

- Auth screen is not a TODO destination.
- Profile data comes from Supabase-backed repository.
- No service key is present in client code.

## Slice 2: Nutrition

Purpose:

- Replace hardcoded nutrition ViewModel data with Supabase-backed data.
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

Before first implementation, decide:

- Will Android use Supabase directly, or call a backend API for writes?
- Will local Supabase be the default dev workflow?
- Which auth method ships first: email/password, phone OTP, or magic link?
- Where will migration files live: `supabase/migrations` or `database/migrations`?
- What is the canonical repo name for this checkout: `Tio-hub` or `tnyx-hub`?
- Which package owns shared TypeScript contracts after Turborepo migration?
- Will backend APIs become the only write path for mobile/web/admin clients?

## First Practical Task

Recommended first task:

Nutrition read-only vertical slice.

Why:

- Current nutrition ViewModels already contain hardcoded sample meals.
- A small schema can prove the migration, seed, repository, RLS, and UI flow.
- It gives the app realistic data without waiting for the whole backend.

Minimum outcome:

- local/dev Supabase has nutrition tables and demo data.
- Android nutrition diary reads from repository.
- hardcoded meals are no longer the permanent source of truth.
