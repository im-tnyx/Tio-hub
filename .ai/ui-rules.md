# UI Rules

Use the existing TNYX Android UI system.

## Compose Rules

- Use `TnyxTheme`.
- Use existing design tokens and components where available.
- Do not hardcode feature-specific UI logic in the app shell.
- Do not put business logic in composable screens.
- Keep screens readable, stable, and responsive.
- Text must fit inside its container on mobile viewports.

## Mandatory Pre-Screen Core Audit

Before writing or redesigning a Compose screen:

1. Inspect `apps/core/src/main/java/com/tnyx/core/theme/TnyxTheme.kt`.
2. Inspect `apps/core/src/main/java/com/tnyx/core/theme/tokens/` for applicable foundation, semantic, effect, and component tokens.
3. Inspect `apps/core/src/main/java/com/tnyx/core/ui/components/` for an existing button, card, input, header, tab, calendar, picker, or sheet.
4. Reuse the existing API or compose a feature-owned wrapper around it.
5. Create a new visual primitive only when no existing core API can express the required behavior, and record that reason in the implementation summary or review.

Available theme accessors include:

- `TnyxTheme.colors`
- `TnyxTheme.dimens`
- `TnyxTheme.insets`
- `TnyxTheme.elevation`
- `TnyxTheme.typography`
- `TnyxTheme.textStyles`
- `TnyxTheme.motion`
- `TnyxTheme.shapes`
- `TnyxTheme.gradients`
- `TnyxTheme.shadows`
- `TnyxTheme.components`

Existing reusable components include `TnyxPrimaryButton`, `TnyxSecondaryButton`, `TnyxGhostButton`, `TnyxCard`, `TnyxTextField`, `TnyxScreenHeader`, `TnyxDynamicHeader`, `TnyxTabSwitcher`, `TnyxWeeklyCalendar`, and `TnyxModalBottomSheet`. Treat this as a starting list and inspect the directory because the inventory can grow.

Do not duplicate a `Tnyx*` component inside a feature. Keep domain-specific widgets under the owning feature. Promote a widget to `apps/core/` only after it is feature-agnostic and actually reused across features.

## Chrome Policy

Every destination must declare its expected chrome behavior:

- `MainChrome`
- `NoBottomBar`
- `FullScreen`
- `BottomSheet`
- `Dialog`

`MainShell` must never contain feature-specific UI logic.

## Main Tabs

The main bottom navigation is:

- Home
- Workout
- Nutrition
- Coach
- Progress

Do not add Profile to the main bottom navigation.

Profile opens from the avatar.

Settings opens from the gear icon.

## Production Screen Checklist

Before a production screen is considered ready, define:

- UI owner
- Navigation owner
- Business logic owner
- Repository owner
- Chrome policy
- Empty state
- Loading state
- Error state
- Demo or real data source
- Core components and tokens reused
- Reason for every new feature-local visual primitive
