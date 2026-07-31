# Task: Android Onboarding Flow Audit & Refinement

- Platform Scope: android-onboarding
- Status: In Progress
- Branch: feat/android-onboarding-refinement
- Created: 2026-07-31

## Primary Objective

Audit and refine the Android Onboarding flow (`apps/features/onboarding/`) according to `ONBOARDING_FLOW_DETAILED.md` and `ONBOARDING_ARCHITECTURE.md`, ensuring all onboarding steps, step transitions, state persistence, design system UI alignment, and target calculations operate smoothly without crashes or broken state transitions.

## Key Focus Areas

1. **Step Sequence & Navigation Audit**: Verify all onboarding steps (Welcome, Goal, Experience, Gender, DOB, Height, Weight, Activity, Targets Review, etc.) transition cleanly.
2. **State & Draft Persistence**: Ensure user selections are saved correctly to draft and Supabase profile repository.
3. **UI/Design System Parity**: Check that all onboarding screens use `TnyxTheme` tokens, `TnyxTextField`, `TnyxPrimaryButton`, and core design system components.
4. **Validation & Edge Cases**: Validate input bounds (Height, Weight, Target Weight, Target Date, DOB) and smooth next/back button behavior.

## Key Checkpoints

- [ ] Audit Onboarding state machine & step definitions.
- [ ] Verify screen transitions and back-stack handling.
- [ ] Fix any visual alignment or input handling bugs.
- [ ] Empirical build verification (`:app:assembleDebug`).
