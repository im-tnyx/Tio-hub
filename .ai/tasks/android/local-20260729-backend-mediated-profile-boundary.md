# Backend-Mediated Profile Boundary

Status: In Progress
Updated: 2026-07-29

## Objective

Keep Android Profile usable without a backend today while preserving a stable
repository boundary for a future backend-mediated Auth/Profile implementation.

## Decisions

- Production business data will use backend APIs.
- `FakeAuthRepository` and `RoomProfileRepository` are local development
  sources, not real-account or remote synchronization claims.
- Live Supabase Profile objects are future backend schema/storage foundations.
- Screens and ViewModels depend on repository contracts, not Supabase clients.

## Completed

- Added and bound `RoomProfileRepository` for per-user local Profile
  persistence.
- Added shared Auth session contracts and stable Fake Auth identities.
- Added per-user Profile isolation, account restore, and logout behavior.
- Added app-owned `DataStoreAuthSessionStore`; Fake Auth sessions now survive
  process restart without persisting passwords.
- Added a Splash session gate for persisted-session Main routing and signed-out
  Welcome routing.
- Wired Personal Information name/username updates and app-internal avatar
  files to the active local Profile.
- Added Auth, Profile, Settings, and app integration behavior tests.
- Accepted ADR-0006 and superseded the direct-to-backend transition assumption.
- Aligned Profile, Supabase, progress, and backend transition documentation.
- Validated app/profile tests and app Kotlin compilation.

## Validation Note

- Shared, Auth, Onboarding, Profile, Settings, and app tests pass.
- App Kotlin/Hilt compilation passes with the Room Profile binding.
- Room recreation and internal avatar-file behavior have focused Robolectric
  coverage.

## Next

- Define the future backend Auth/Profile API packet after runtime, hosting,
  token, and identity mapping decisions are approved.
- Keep email/mobile writes and remote Profile/avatar synchronization deferred
  until their authoritative backend contracts exist.
