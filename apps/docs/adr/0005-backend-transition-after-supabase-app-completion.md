# ADR-0005: Backend Transition After Supabase-backed App Completion

## Status

Superseded by
[ADR-0006: Backend-Mediated Client Data Access](0006-backend-mediated-client-data-access.md)

## Context

Tio Hub is currently an Android/Wear focused repo. Several app surfaces are still skeletons or partially wired, and many features still need repository-backed persistence.

The repo already has an incremental Supabase setup plan. That plan allows feature slices to move from hardcoded scaffolding to repository-backed Supabase data one slice at a time.

A future custom backend is still expected, but starting it before the app's core product flows are stable would likely create premature API contracts and service boundaries.

## Decision

Use Supabase to complete the current app phase first.

Do not start a custom backend implementation yet.

The accepted sequence is:

1. Finish core Android/Wear app slices using repository contracts.
2. Back persistent slices with Supabase incrementally.
3. Keep all Supabase details behind platform/data repository implementations.
4. Keep `:shared` pure Kotlin and platform-neutral.
5. Start custom backend planning only after product flows and API boundaries are clear.
6. Choose the backend runtime in a future ADR before writing backend code.

This ADR does not choose Ktor, Express, NestJS, Spring Boot, or any other backend runtime.

## Rules

- Supabase is the current app backend/data layer for feature completion.
- Custom backend is deferred until the app has stable flows and contracts.
- Screens must not depend on Supabase tables, DTOs, SQL, or backend transport details.
- Repository interfaces should remain stable enough to swap Supabase implementations for backend API implementations later.
- Service-role keys and private server secrets must never be exposed to Android, Wear, public web, screenshots, or committed docs.
- Backend references in planning docs should be runtime-neutral unless a later ADR chooses the runtime.

## Backend Start Triggers

Create the backend runtime ADR when one or more of these become true:

- Multiple clients need the same protected business logic.
- AI coaching requires server-side secrets, background work, or shared long-term context.
- Mobile direct Supabase access becomes too limiting or too coupled.
- Admin/web workflows require server-owned operations.
- Workout, Nutrition, Profile, Progress, and Onboarding contracts are stable enough to define external APIs.

## Consequences

Positive:

- Android/Wear development can continue without premature backend architecture work.
- Supabase schemas follow real product slices instead of guessed future APIs.
- Repository boundaries preserve a migration path to a custom backend.
- Backend runtime can be chosen later with more product information.

Negative:

- Some Supabase-backed repositories may need to be replaced by backend API implementations later.
- Direct Supabase client dependencies may exist in `:app` data wiring during the current phase.
- Documentation must clearly distinguish current Supabase-backed runtime from future custom backend plans.

## Non-Goals

- No backend service is created by this ADR.
- No Ktor, Express, NestJS, Spring Boot, worker, or deployment setup is added by this ADR.
- No Supabase schema or migration is created by this ADR.
- No Android runtime code is changed by this ADR.
