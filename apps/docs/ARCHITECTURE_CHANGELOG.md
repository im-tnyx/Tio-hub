# Architecture Changelog

This changelog records architecture decisions that affect project structure, data flow, navigation policy, module boundaries, or engineering practices.

Do not use this file for ordinary feature changes, bug fixes, or copy edits.

## 2026-07-17

- Proposed Settings-only user customization for eligible bottom-navigation destinations.
- Kept the reset/default order as `Home | Nutrition | AI | Workout | Progress`.
- Required Home to remain first and limited valid configurations to three through six tabs.
- Defined the route graph as the destination source of truth while preferences control only the rendered eligible list and order.
- Required stable destination identifiers, DataStore-backed local persistence, normalization, migration, and safe fallback to defaults.
- Deferred Home screen section customization to a separate future decision because Home is a cross-domain summary surface.
- Kept Explore unavailable until its production route exists and Profile unavailable as a persistent tab until its graph behavior is explicitly approved.

## 2026-07-16

- Added versioned pure Kotlin Workout contract v2 for Phone/Wear exercise, routine, session, set, timer, and mutation state.
- Added `WorkoutReducer` as the deterministic shared transition owner; UI and platform persistence must not duplicate session mutation rules.
- Defined one canonical exercise identity with `MALE`, `FEMALE`, and `NEUTRAL` presentation media variants.
- Added approved-release and provenance gates to exercise media resolution with exact -> neutral -> placeholder fallback.
- Replaced command-specific repository methods with catalog, engine-state, history, and atomic mutation boundaries; Phone now has a Room implementation while Wear remains pending.
- Added a mandatory pre-screen `:core` reuse gate: agents and contributors must inspect `TnyxTheme`, tokens, and existing UI components before introducing a new visual primitive.
- Added Phone Room persistence v1 under `apps/app` with engine-state snapshot, mutation outbox, completed-session history, catalog/routine rows, and Hilt composition.
- Implemented `RoomWorkoutRepository` so reducer application, snapshot persistence, outbox insertion, and completed-history writes share one Room transaction.
- Added explicit mutation-ID conflict and per-device sequence rejection to prevent silent idempotency collisions and out-of-order local writes.
- Kept outbox delivery, Workout Compose consumption, remote sync, and Wear runtime behavior outside this persistence stage.

## 2026-06-29

- Documented Profile as a Fitness Hub + Account Launcher, not a business domain.
- Documented Progress ownership for Journey, Progress Photos, Measurements, Weight, Achievements, and Progress Analytics.
- Documented Settings as the app/account configuration entry point.
- Documented Main Graph bottom tabs as Home, Workout, Nutrition, Coach, and Progress.
- Documented Profile launch from avatar and Settings launch from gear icon.
- Documented destination chrome policy: `MainChrome`, `NoBottomBar`, `FullScreen`, `BottomSheet`, and `Dialog`.
- Documented incremental Supabase setup: create tables only when a feature slice needs them.
- Added Android mobile progress tracking through `ANDROID_APP_PROGRESS.md`.
- Added `apps/docs/README.md` as the canonical Android/Wear documentation map.
- Added `.ai/` as a concise AI/contributor orientation layer.
- Added GitHub governance templates and Android CI workflow.

## Future Entries

Record only changes that affect durable architecture:

- Module boundaries.
- Feature ownership.
- Navigation graph policy.
- Data ownership or persistence strategy.
- Supabase/RLS strategy.
- Shared contract strategy.
- Wear OS reuse strategy.
- Engineering workflow or Definition Of Done.
