# Architecture Changelog

This changelog records architecture decisions that affect project structure, data flow, navigation policy, module boundaries, or engineering practices.

Do not use this file for ordinary feature changes, bug fixes, or copy edits.

## 2026-07-29 - Backend-mediated client data boundary

- Superseded the earlier Supabase-first client transition decision with
  ADR-0006.
- Kept live Supabase migrations, RLS, grants, views, and storage as server-side
  schema foundations rather than active Android synchronization proof.
- Bound the active Android `ProfileRepository` to a non-persistent
  `InMemoryProfileRepository` with empty defaults for the Fake Auth development
  phase.
- Kept Screens and ViewModels unchanged behind the existing repository
  contract.
- Deferred real account creation, identity mapping, profile persistence, and
  synchronization until a backend runtime/auth ADR and minimum Auth/Profile API
  slice are approved.
- Kept the existing direct Supabase implementation inactive rather than
  deleting schema or migration work.

## 2026-07-18 — Workout thin offline presentation boundary

- Replaced the Workout placeholder with feature-owned typed Home and History destinations.
- Added `WorkoutSessionCoordinator` as the Stage 3 use-case boundary between `WorkoutViewModel` actions and the shared `WorkoutRepository`.
- Kept mutation creation, outbox-backed monotonic Phone sequencing, first-party starter exercise construction, input validation, and finish idempotency outside Compose.
- Reused `TnyxTheme`, `TnyxScreenHeader`, `TnyxCard`, `TnyxTextField`, and Tnyx button primitives without adding a feature-local design system.
- Extended the mandatory component-token chain with Header tokens and tokenized Button, Input, and Card state colors so feature consumers do not carry literal dimensions, alpha values, or typography overrides.
- Mapped completed Workout state to the semantic success color instead of the primary brand color.
- Added additive `ExerciseTrackingType` and durable tracking snapshots so catalog, routine, and active-session presentation can select metric fields without branching on names or media.
- Replaced the singular Workout presentation model with keyed exercise/set/metric UI state and a feature-owned `WorkoutExerciseEditor` supporting Active, Routine-edit, and Read-only policies; only Active mode is currently wired.
- Replaced nested set cards with a dense tracking-type-aware table and kept the visual implementation feature-owned while reusing Tio theme tokens and core sheet/button primitives.
- Derived `Previous` draft suggestions from completed Room history by stable exercise ID and set number; no duplicate previous-performance table or third-party dataset was introduced.
- Reused the existing nullable set-level RPE contract for a 5-10 strength selector and kept duration/cardio layouts free of irrelevant RPE state.
- Kept Workout editor semantics under `:features:workout`; it composes core Tnyx primitives rather than promoting domain components into `:core`.
- Kept full catalog/media, routines, advanced workout execution, Wear runtime, remote outbox delivery, and cloud sync outside this slice.

## 2026-07-17 — Dynamic navigation catalog

- Accepted Settings-only user customization for bottom-navigation visibility and order.
- Kept the exact reset/default order as `Home | Nutrition | Tio | Workout | Progress`.
- Expanded the optional catalog with `Meal Plan`, `Library`, and `You` while retaining the three-to-six tab limit.
- Assigned Meal Plan to Nutrition ownership and Library to Workout ownership.
- Promoted You to a true MainGraph destination that renders the Profile experience inside persistent shell chrome.
- Kept root ProfileGraph as an avatar fallback when You is not enabled.
- Added deterministic Home modes: Nutrition, Workout, Balanced, and Custom, derived from enabled domain tabs.
- Confirmed that Home adaptation affects summary priority only and never silently changes saved navigation.
- Kept Explore/Discover outside the current catalog until a distinct production route exists.

## 2026-07-17 — Configurable navigation foundation

- Proposed Settings-only user customization for eligible bottom-navigation destinations.
- Kept the reset/default order as `Home | Nutrition | AI | Workout | Progress` at the original stable-ID level; the user-facing AI label is now Tio.
- Required Home to remain first and limited valid configurations to three through six tabs.
- Defined the route graph as the destination source of truth while preferences control only the rendered eligible list and order.
- Required stable destination identifiers, DataStore-backed local persistence, normalization, migration, and safe fallback to defaults.
- Deferred detailed Home section composition because Home is a cross-domain summary surface.

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
