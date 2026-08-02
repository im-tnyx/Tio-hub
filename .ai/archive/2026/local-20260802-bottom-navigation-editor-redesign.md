# Task: Android Bottom Navigation Editor Redesign

- Platform Scope: android-settings-navigation
- Status: Completed
- Branch: codex/android-bottom-navigation-editor
- Created: 2026-08-02
- Merged PR: `#30`
- Merge Commit: `977c4784b95f9efb95342dc62c56e4d657c823dd`

## Primary Objective

Redesign the Settings `Bottom navigation` editor so users can preview their
navigation, apply Workout/Nutrition/Hybrid presets, add or remove supported
tabs, and long-press-drag visible tabs left or right while `Home` remains fixed
first. Preserve the existing draft/save persistence boundary and render the
screen with the checked-in Tio/TNYX design system.

## Approved UX

1. Keep `Preview` at the top and update it immediately from draft state.
2. Place a horizontal mode selector below Preview with three presets:
   `Workout`, `Nutrition`, and `Hybrid`; show derived `Custom` only when active.
3. Applying a preset updates only the draft until `Save` succeeds.
4. Any tab membership that does not match a preset is displayed as `Custom`;
   reordering the same preset icons does not activate Custom, and Custom itself
   does not overwrite the draft.
5. Keep all eight supported destinations visible in a compact four-column,
   two-row `Available tabs` grid; each destination keeps a permanent circular
   icon treatment and active Preview destinations use a muted state.
6. `+` inserts an inactive tab into the draft Preview. Active non-Home tabs
   expose `X` as a tap removal fallback, while `Home` remains pinned.
7. Available circular icons can be long-pressed and dropped at a Preview
   position; Preview icons can be dropped into the Tabs area to remove them.
   `+`/`X` actions remain as accessible tap fallbacks.
8. Enforce the existing 3-to-6 visible-tab limit. `Home` cannot be removed.
9. Long-press a non-Home Preview item and drag it left/right to reorder.
   `Home` cannot move from index 0.
10. Preserve accessible non-gesture reorder actions for assistive technology.
11. Intercept toolbar and system Back when draft changes exist and ask whether
    to discard them.

## Preset Drafts

- Workout: `Home`, `Workout`, `Ai`, `WorkoutLibrary`, `Progress`
- Nutrition: `Home`, `Nutrition`, `Ai`, `MealPlan`, `Progress`
- Hybrid: `Home`, `Nutrition`, `Ai`, `Workout`, `Progress`
- Custom: derived from any other valid membership/order; no destructive preset

## Architecture And Truth Boundary

- Follow `Route + Screen + ViewModel + UiState + Action`.
- Keep editor-specific preset semantics under
  `apps/features/settings/.../bottom_navigation/`.
- Keep `ShellTab`, persisted stable IDs, `normalizeBottomNavTabs`, and shell
  rendering ownership unchanged unless correctness requires a narrow update.
- Reuse `TnyxTheme`, `TnyxCard`, existing navigation icons, and current
  DataStore repository. Do not add a new core visual primitive for this slice.
- Do not modify or archive the previous onboarding task. It remains separate
  paused context and can be restored after this primary objective is closed.
- Preserve unrelated uncommitted changes in `.ai/`, `AGENTS.md`, and
  `apps/AGENTS.md`.

## Expected File Scope

- `apps/features/settings/src/main/java/com/tnyx/features/settings/presentation/bottom_navigation/BottomNavigationContract.kt`
- `apps/features/settings/src/main/java/com/tnyx/features/settings/presentation/bottom_navigation/BottomNavigationViewModel.kt`
- `apps/features/settings/src/main/java/com/tnyx/features/settings/presentation/bottom_navigation/BottomNavigationRoute.kt`
- `apps/features/settings/src/main/java/com/tnyx/features/settings/presentation/bottom_navigation/BottomNavigationScreen.kt`
- `apps/features/settings/src/test/java/com/tnyx/features/settings/presentation/bottom_navigation/BottomNavigationEditorTest.kt`

## Checkpoints

- [x] Existing editor, shell constraints, theme components, and relevant tests audited.
- [x] UX hierarchy and preset semantics agreed with the user.
- [x] Preset resolution and arbitrary reorder contract implemented and tested.
- [x] Preview long-press drag and unified Tabs UI implemented.
- [x] Unsaved-change Back handling implemented.
- [x] `:features:settings:test` passes.
- [x] `:app:compileDebugKotlin` passes.

## 2026-08-02 Checkpoint

- Added Workout, Nutrition, and Hybrid draft presets with exact-match Custom
  derivation; preset choice does not persist before Save.
- Rebuilt the editor hierarchy as Preview, horizontal mode selector, and one
  all-destinations Tabs section with Added/remove and max-limit states.
- Added long-press horizontal Preview reordering with haptics, Home pinning,
  and TalkBack `Move left` / `Move right` actions.
- Added toolbar and system Back interception with an unsaved-discard dialog.
- Added pure editor and ViewModel coverage for presets, multi-position reorder,
  unsaved Back handling, and saved preset persistence.
- Validation passed: `:features:settings:test`, `:app:compileDebugKotlin`, and
  `git diff --check`.
- User visual review found the per-slot Preview background and long-press
  background distracting; it was removed. Long-press now highlights only the
  active icon while retaining drag haptics and reorder behavior.
- Corrected preset semantics after user review: Workout, Nutrition, and Hybrid
  all place Tio in the center; Custom derives only from icon membership changes,
  not from reordering the same icons.
- Improved drag identification after user review: the active Preview icon now
  scales up smoothly during long-press/reorder and returns to normal on release,
  without adding a slot background.
- Added a compact circular `surfaceVariant` behind only the active Preview icon
  during long-press/reorder; the full tab slot remains background-free.
- Added Samsung Quick Panel-style cross-zone draft editing while retaining the
  Save/Reset bar: Preview remains fixed above the scrollable controls, available
  circular icons can be dragged into a positional Preview slot, Preview icons can
  be dragged into the Tabs card to remove them, and a circular drag overlay
  follows the pointer. Existing add/remove buttons remain available.
- Replaced the long all-tabs row list with a compact four-column, two-row
  Available tabs grid after user review. All eight supported tabs remain visible;
  Preview tabs use a muted state with an `X` removal fallback, inactive tabs use
  `+`, and pinned Home cannot be removed. Mode selector behavior remains frozen
  and unchanged; Preview also keeps its TalkBack Remove action.
- Revalidated the all-tabs grid slice with `:features:settings:test`,
  `:app:compileDebugKotlin`, and `git diff --check`; all passed.
- Compacted the Available grid after user review by moving each `+`/`X` into a
  small circular badge at the main icon's top-right corner. The separate action
  row was removed, while the 48dp circular icon remains the accessible tap
  target and long-press drag behavior remains unchanged.
- Corrected the Available-tab interaction shape after visual review: the tap
  target and ripple are now clipped to the circular icon container instead of
  using the rectangular icon-and-label tile.
- Styled the unsaved-changes `AlertDialog` directly with TNYX shape, surface,
  typography, elevation, and action-color tokens. It intentionally remains a
  semantic dialog rather than nesting a `TnyxCard` inside another surface.
- Remaining review action before merge was device/emulator gesture and
  narrow-width visual smoke; that follow-up moved to the stabilization audit.

## Risks And Validation Notes

- Gesture-only reordering is not sufficient; semantics actions must remain
  available for TalkBack users.
- Drag calculations must use the live draft list so repeated moves within one
  long press do not operate on stale indices.
- Preset selection must not persist until the existing Save action succeeds.
- No database, backend, Supabase, Wear, or external-system changes are in scope.
