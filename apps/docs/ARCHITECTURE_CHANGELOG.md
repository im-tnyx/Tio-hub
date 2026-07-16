# Architecture Changelog

This changelog records architecture decisions that affect project structure, data flow, navigation policy, module boundaries, or engineering practices.

Do not use this file for ordinary feature changes, bug fixes, or copy edits.

## 2026-07-16

- Added versioned pure Kotlin Workout contract v2 for Phone/Wear exercise, routine, session, set, timer, and mutation state.
- Added `WorkoutReducer` as the deterministic shared transition owner; UI and platform persistence must not duplicate session mutation rules.
- Defined one canonical exercise identity with `MALE`, `FEMALE`, and `NEUTRAL` presentation media variants.
- Added approved-release and provenance gates to exercise media resolution with exact -> neutral -> placeholder fallback.
- Replaced command-specific repository methods with catalog, engine-state, history, and atomic mutation boundaries; platform implementations remain pending.
- Added a mandatory pre-screen `:core` reuse gate: agents and contributors must inspect `TnyxTheme`, tokens, and existing UI components before introducing a new visual primitive.

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
