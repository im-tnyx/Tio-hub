---
task_id: local-20260804-wear-theme-unification
title: Wear OS Theme Unification and Dark Theme Standardization
status: in_progress
priority: medium
owner: watch
branch: feat/wear-theme-unification
github_issue:
github_pr:
platforms: [watch, android]
created: 2026-08-04
updated: 2026-08-04
---

# Task: Wear OS Theme Unification and Dark Theme Standardization

## Goal

Unify `:wear` OS visual theme with TNYX design system core tokens while maintaining Wear OS black (AMOLED dark theme) display standards. Rename legacy `SamsungHealthWearTheme` to `TnyxWearTheme`, clean up all third-party legacy branding references (`Samsung` references & comments), convert static raster WebP icons to tintable vector icons (`ImageVector` / Compose `Icon`) driven by theme tokens, map brand and domain tokens, and eliminate hardcoded UI values.

## Acceptance Criteria

- [x] Rename `SamsungHealthWearTheme` to `TnyxWearTheme` in `:wear`.
- [x] Remove all legacy `Samsung` brand references and code comments across `:wear` (`Theme.kt`, `MainActivity.kt`, `Color.kt`, `HomeDashboardScreen.kt`).
- [ ] Refactor `:wear` icons from pre-baked static WebP images (`ic_routine.webp`, `ic_water.webp`, `ic_food.webp`) to Compose `Icon` / tintable vectors that dynamically consume `TnyxColors` / domain token colors.
- [x] Connect `:wear` to shared core design tokens (`:core` or shared token definitions) without bringing heavy phone-only Compose dependencies.
- [x] Ensure `:wear` uses strict AMOLED pitch black (`#000000`) background for battery efficiency on Wear OS devices.
- [x] Map semantic domain colors (Steps, Water, Heart Rate, Stress, Sleep, Workout) to unified token source.
- [ ] Replace hardcoded UI constants in `HealthCard` and `HomeDashboardScreen` with structured tokens.
- [x] All `:wear` UI screens, tiles, and navigation pass compilation and preview checks.

## Scope

- `apps/wear/build.gradle.kts`
- `apps/wear/src/main/res/drawable/`
- `apps/wear/src/main/java/com/tnyx/wear/theme/Theme.kt`
- `apps/wear/src/main/java/com/tnyx/wear/theme/Color.kt`
- `apps/wear/src/main/java/com/tnyx/wear/presentation/components/HealthCard.kt`
- `apps/wear/src/main/java/com/tnyx/wear/presentation/home/HomeDashboardScreen.kt`
- `apps/wear/src/main/java/com/tnyx/wear/MainActivity.kt`

## Out Of Scope

- Modifying Phone UI navigation or phone screens in `:app` or `:features`.
- Replacing `androidx.wear.compose.material` with non-Wear Material 3 libraries.
- Backend, Supabase, or Health Connect schema changes.

## Canonical References

- `AGENTS.md`
- `apps/AGENTS.md`
- `apps/docs/WEAR_OS_PLAN.md`
- `apps/docs/WEAR_OS_PROGRESS.md`
