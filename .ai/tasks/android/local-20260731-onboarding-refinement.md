# Task: Android Onboarding Flow Audit & Refinement

- Platform Scope: android-onboarding
- Status: In Progress
- Branch: feat/android-onboarding-refinement
- Created: 2026-07-31

## Primary Objective

Audit and refine the Android Onboarding flow (`apps/features/onboarding/`) using `ONBOARDING_FLOW_DETAILED.md` and `ONBOARDING_ARCHITECTURE.md` as flow, behavior, and architecture requirements. Render all UI through the checked-in Tio/TNYX design system; reference files or images do not override `TnyxTheme`, existing core components, or product-owned visual styling. Ensure all onboarding steps, step transitions, state persistence, design system UI alignment, and target calculations operate smoothly without crashes or broken state transitions.

## Key Focus Areas

1. **Step Sequence & Navigation Audit**: Verify all onboarding steps (Welcome, Goal, Experience, Gender, DOB, Height, Weight, Activity, Targets Review, etc.) transition cleanly.
2. **State & Draft Persistence**: Ensure user selections are saved correctly to draft and Supabase profile repository.
3. **UI/Design System Parity**: Check that all onboarding screens use `TnyxTheme` tokens, `TnyxTextField`, `TnyxPrimaryButton`, and core design system components. Treat screenshots, images, mockups, and other visual references as structural or behavioral guidance only; do not copy their visual theme.
4. **Validation & Edge Cases**: Validate input bounds (Height, Weight, Target Weight, Target Date, DOB) and smooth next/back button behavior.

## Key Checkpoints

- [ ] Audit Onboarding state machine & step definitions.
- [ ] Verify screen transitions and back-stack handling.
- [ ] Fix any visual alignment or input handling bugs.
- [ ] Empirical build verification (`:app:assembleDebug`).

## 2026-08-01 Checkpoint

- Anonymous Supabase Auth and profile-image Storage were verified live.
- Fixed Personal Information saves for anonymous profiles whose optional
  `username` is blank; remote profile write failures now surface to the UI.
- Completed onboarding now syncs current schema-supported Nutrition and Workout
  owner fields in addition to Profile-owned fields.
- Validation passed: `:features:onboarding:test`, `:features:settings:test`,
  and `:app:compileDebugKotlin`.
- Remaining boundary: source attribution and answers without existing owner
  columns are intentionally not persisted remotely; no schema migration was
  created or applied.
- Bumped the local onboarding flow to version `16` so older checkpoints reset
  and the explicit Review confirmation cannot be accidentally cleared before
  Finish.
- Fixed the runtime `Finish` failure after using `Skip` in the Workout section:
  skipping that section now records `workout_intro.choice = false`, so final
  validation no longer requires omitted Workout answers before the Supabase
  profile/nutrition sync starts.
- Validation passed: `:features:onboarding:test` and `:app:assembleDebug`.
- Completion now trusts the completed state emitted by the step-level flow
  validator and always starts the Supabase write path from the final Review
  step; it no longer blocks `Finish` on a second full-draft validation pass.
- Local recovery-checkpoint writes are now best-effort at completion, so a
  DataStore persistence fault cannot prevent the Profile/Nutrition/Workout
  Supabase write from starting.
- Anonymous Supabase sessions now remain on the `GetStarted` onboarding route
  rather than being treated as permanent signed-in sessions. This preserves
  the Intro step and the full Back path for guest onboarding.
- Nutrition Sleep Schedule now uses the core reusable
  `SleepScheduleBottomSheet`, which edits bedtime and wake-up time together
  and delegates persistence to the caller.
- Validation passed: `:features:onboarding:test` and `:app:assembleDebug`.
  The verified APK was reinstalled over the connected test device without
  clearing app data and launched to Home without a crash.

## 2026-08-02 Checkpoint

- Clarified the reference adaptation boundary: onboarding documents and any
  supplied files or images guide content, hierarchy, behavior, and flow only.
  Runtime UI must continue to use the current Tio/TNYX theme, core components,
  and product-owned visual language rather than copying reference styling.
