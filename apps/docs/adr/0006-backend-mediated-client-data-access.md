# ADR-0006: Backend-Mediated Client Data Access

## Status

Accepted

## Context

Tio-hub has live Supabase schema foundations and repository contracts, but its
real authentication source and custom backend runtime are not implemented yet.
The Android app must remain easy to develop without making direct database
access the long-term client architecture.

The earlier Supabase-first transition decision allowed active Android
repositories to call Supabase directly. The current product decision replaces
that posture: protected business data will be mediated by a backend API.

## Decision

Use repository contracts in Android and Wear, but route production data through
a future backend API.

Until that backend and its auth strategy exist:

1. `FakeAuthRepository` remains the current development/test auth source.
2. Fake Auth publishes a shared app-owned DataStore session without storing a
   password. Profile uses a non-persistent `InMemoryProfileRepository` keyed by
   that active user ID; identity values come from submitted Auth data and
   Profile field edits remain process-memory only.
3. Live Supabase tables, views, RLS, grants, migrations, and storage remain
   server-side schema foundations, not proof of active Android synchronization.
4. Existing direct Supabase repository code is not the active Profile binding.
5. Screens and ViewModels continue to depend only on domain repository
   contracts.

When real synchronization starts, add a separate backend runtime ADR and the
smallest Auth/Profile API slice before switching Android Hilt bindings to
backend repository implementations.

## Target Shape

```text
Android / Wear
    -> ViewModel / UseCase
    -> Repository contract
    -> Backend API repository
    -> Authenticated backend
    -> Supabase/Postgres/Storage
```

## Rules

- No service-role, admin, provider, or database credential belongs in a client.
- Protected client data must not depend on direct table names, SQL, storage
  policies, or Supabase DTOs.
- Fake/in-memory implementations are development scaffolding only and must not
  claim persistence or synchronization.
- Remote DTOs and transport errors must be mapped inside backend repository
  implementations.
- Backend authentication, identity mapping, idempotency, audit, and deployment
  choices require an explicit runtime ADR before server code is added.
- Live database objects do not imply a shipped client feature.

## Consequences

Positive:

- Android UI and domain work can continue before the backend exists.
- Future backend adoption changes repository implementations and composition,
  not Screens or ViewModels.
- Server-only database credentials and provider identity mapping stay outside
  clients.
- Supabase can remain the managed database/storage platform behind the backend.

Negative:

- Real account creation and remote user-data synchronization remain unavailable
  while Fake Auth and in-memory repositories are active.
- Existing direct Supabase client code must remain inactive or be removed when
  the backend adapter replaces it.
- API contracts, token verification, identity mapping, and offline behavior
  still require implementation and validation later.

## Supersedes

This ADR supersedes
`0005-backend-transition-after-supabase-app-completion.md`.

It does not supersede ADR-0004's incremental schema and migration discipline.
