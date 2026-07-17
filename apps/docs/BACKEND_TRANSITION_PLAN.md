# Backend Transition Plan

Last updated: 2026-07-17

This document defines how Tio Hub should move from the current Android/Wear implementation toward a future custom backend without slowing down the mobile app build.

## Current Decision

Build the app with Supabase-backed slices first. Start the custom backend only after the core app experience is complete enough to justify a stable server API.

This means:

- Supabase remains the active backend/data layer for the current Android/Wear phase.
- Feature persistence is added incrementally through repository contracts.
- A custom backend is planned, but not implemented in this phase.
- Backend runtime/framework choice is deferred to a later ADR.
- No backend folder, server runtime, worker service, or deployment config should be added only for planning.

## Why Supabase First

The app still needs several runtime slices before a custom backend shape is stable:

- Auth/Profile source of truth
- Nutrition repository and meal log persistence
- Onboarding persistence and resume flow
- Workout UI consumption of the repository-backed runtime
- Progress/Journey/Photos source of truth
- Wear sync and offline behavior

Creating a full custom backend before these flows settle would risk wrong API shapes, premature service boundaries, and migration churn. Supabase is acceptable now because it lets each feature slice prove its real data shape with tables, RLS, seed data, repository wiring, and validation.

## Boundary Rules For The Current Phase

1. Screens never depend on Supabase, table names, DTOs, or SQL concepts.
2. ViewModels call repository/domain contracts only.
3. Supabase access stays in platform/data implementations such as `:app` data modules.
4. `:shared` contains only pure Kotlin domain models, use cases, and repository interfaces.
5. Service-role keys, admin keys, and private AI keys never go into Android, Wear, public web, screenshots, or committed docs.
6. Every persistent user-owned slice must include RLS and ownership validation.
7. Direct Supabase usage is allowed only behind a repository boundary that can later be swapped for a backend API client.
8. Demo data remains local/dev seed data, not production source of truth.

## Backend Later

The custom backend should begin when the app has enough real product shape to freeze server contracts.

Start backend planning when at least these are true:

- Auth/Profile flow is real and repository-backed.
- Nutrition read/write flow is real enough to define API contracts.
- Workout runtime and history needs are clear.
- Progress/Journey/Photos ownership is clear.
- AI coaching needs server-side secrets, background work, or shared context beyond mobile-only state.
- More than one client surface, such as Android, Wear, web, or admin, needs the same protected business logic.

## Backend Runtime Decision

This document does not choose Ktor, Express, NestJS, Spring Boot, or any other runtime.

Before backend implementation starts, create a separate ADR that decides:

- backend language/runtime
- API style
- hosting target
- database ownership
- auth/session strategy
- file storage strategy
- background worker/cron strategy
- contract generation or DTO sharing strategy

Until that ADR exists, references to future backend should stay runtime-neutral.

## Expected Future Shape

```text
Android / Wear / future clients
        ↓
Repository or API client boundary
        ↓
Custom backend API and workers    # runtime TBD by ADR
        ↓
Postgres / Supabase / Storage / AI providers
```

Supabase may continue as managed Postgres/Auth/Storage behind the backend, or parts of it may be replaced later. That decision belongs to the backend runtime ADR and the first backend implementation slice.

## Non-Goals

Do not do these in the current Android/Wear phase:

- Do not create a backend service just because it is planned.
- Do not add Ktor/Express/Nest/Spring server code yet.
- Do not move Android feature work into a server repository prematurely.
- Do not expose service-role operations to mobile clients.
- Do not let UI screens know whether data came from Supabase or a backend API.

## Relationship To Existing Supabase Plan

`SUPABASE_INCREMENTAL_SETUP_PLAN.md` remains the canonical guide for current persistence slices.

This document adds timing clarity: Supabase-backed app completion comes first; custom backend starts later after product flows and contracts are stable.
