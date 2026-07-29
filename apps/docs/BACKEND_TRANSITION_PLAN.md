# Backend Transition Plan

Last updated: 2026-07-29

This document defines how Tio Hub should move from the current Android/Wear implementation toward a future custom backend without slowing down the mobile app build.

## Current Decision

Protected business data will be backend-mediated in the production architecture.
The custom backend is still deferred until its runtime, auth, identity, API, and
deployment decisions are approved.

This means:

- Repository contracts remain the Android/Wear data boundary.
- `FakeAuthRepository` and local repositories support current UI development
  without claiming real accounts or remote synchronization.
- Fake Auth currently publishes a shared app-owned DataStore session without
  persisting passwords. Local Profile state is keyed by that stable fake user
  ID and isolated across account switches. Profile fields and app-internal
  avatar files persist locally through Room, while logout switches the visible
  state to a clean guest Profile.
- Live Supabase migrations remain database/schema foundations.
- Direct Supabase Profile access is not the active Android runtime binding.
- No backend folder, server runtime, worker service, or deployment config is
  added only for planning.

## Why This Split

The app can stabilize feature state and repository contracts without choosing
premature server APIs. At the same time, making direct database access the
active client architecture would create a second migration when the backend is
introduced.

The current split keeps UI development moving while making the limitation
explicit: real account creation and remote user-data sync start only with an
approved backend/auth slice.

## Boundary Rules For The Current Phase

1. Screens never depend on Supabase, table names, DTOs, or SQL concepts.
2. ViewModels call repository/domain contracts only.
3. Transport and provider details stay in platform/data implementations such as
   `:app` data modules.
4. `:shared` contains only pure Kotlin domain models, use cases, and repository interfaces.
5. Service-role keys, admin keys, and private AI keys never go into Android, Wear, public web, screenshots, or committed docs.
6. Every persistent user-owned slice must include backend authorization,
   database ownership validation, and negative cross-user tests.
7. Direct Supabase repositories are not active production bindings.
8. Fake/in-memory data is local development scaffolding, not a persistence or
   synchronization claim.

## Backend Later

The custom backend should begin when the app has enough real product shape to freeze server contracts.

Start the first backend implementation only after a backend runtime ADR defines
the auth and identity boundary. The initial vertical slice should remain small:

- health/readiness endpoint
- auth middleware and identity mapping
- bootstrap/profile read
- profile update
- profile image upload

Nutrition, Workout cloud sync, Progress, AI, Billing, and operational APIs should
remain separate later slices.

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
Custom backend API and workers    # runtime and auth TBD by ADR
        ↓
Postgres / Supabase / Storage / AI providers
```

Supabase may continue as managed Postgres/Auth/Storage behind the backend, or
parts of it may be replaced later. Android/Wear must not depend on that provider
choice.

## Non-Goals

Do not do these in the current Android/Wear phase:

- Do not create a backend service just because it is planned.
- Do not add Ktor/Express/Nest/Spring server code yet.
- Do not move Android feature work into a server repository prematurely.
- Do not expose direct protected table/storage access or service-role operations
  to mobile clients.
- Do not let UI screens know whether data came from Supabase or a backend API.

## Relationship To Existing Supabase Plan

`SUPABASE_INCREMENTAL_SETUP_PLAN.md` remains the canonical guide for migrations,
RLS, grants, storage, and schema verification. It no longer implies that live
objects are active Android data paths.

[ADR-0006](adr/0006-backend-mediated-client-data-access.md) is the binding
client-data access decision.
