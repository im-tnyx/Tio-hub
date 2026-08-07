# Task: Material 3 (M3) Official Surface Container Color Role Refactoring

- **Status**: In Progress
- **Created**: 2026-08-07
- **Target**: `apps/core/` and `apps/features/workout/`
- **Scope**: Android Design System & Library TopAppBars

## Objective
Refactor `apps/core` theme tokens and library screens (`ExerciseLibraryScreen`, `SearchExercisesScreen`, `CreateExerciseScreen`) to strictly follow Google's **Material 3 (M3) Official Surface Container Color Role System**.

## Key Steps
1. **Core Theme Token Upgrade**:
   - Add M3 surface container roles to `TnyxColors.kt`: `surfaceContainerLow`, `surfaceContainerHigh`, `surfaceContainerHighest`.
   - Update `TnyxDarkColors.kt` and `TnyxLightColors.kt` palette mappings.

2. **TopAppBar M3 Standardization**:
   - Update `TopAppBar` `containerColor` to `TnyxTheme.colors.surface`.
   - Configure `scrolledContainerColor` to `TnyxTheme.colors.surfaceContainerHigh` for dynamic M3 scrolling tinting.

3. **Validation**:
   - Run `:core:compileDebugKotlin` and `:features:workout:compileDebugKotlin`.
   - Run `:app:compileDebugKotlin` to ensure 0 build errors across the repository.

## Checkpoints & History
- [x] Initial audit of M3 surface roles vs `TnyxColors` tokens.
- [x] Created `implementation_plan.md` artifact.
- [ ] Implement M3 tokens in `TnyxColors.kt`, `TnyxDarkColors.kt`, `TnyxLightColors.kt`.
- [ ] Apply M3 `TopAppBarDefaults.topAppBarColors()` across library screens.
- [ ] Verify full build compilation.
