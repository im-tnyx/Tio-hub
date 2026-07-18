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
3. Inspect `apps/core/src/main/java/com/tnyx/core/ui/components/` for an existing button, card, input, header, tab, calendar, picker, avatar, or sheet.
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

Existing reusable components include `TnyxPrimaryButton`, `TnyxSecondaryButton`, `TnyxGhostButton`, `TnyxCard`, `TnyxTextField`, `TnyxScreenHeader`, `TnyxDynamicHeader`, `TnyxTabSwitcher`, `TnyxWeeklyCalendar`, `TnyxUserAvatar`, and `TnyxModalBottomSheet`. Treat this as a starting list and inspect the directory because the inventory can grow.

Do not duplicate a `Tnyx*` component inside a feature. Keep domain-specific widgets under the owning feature. Promote a widget to `apps/core/` only after it is feature-agnostic and actually reused across features.

## User Avatar Rule

- Every current-user photo surface must use `TnyxUserAvatar`.
- Feature screens must not independently decide Free, Plus, or Premium avatar shapes, rings, gradients, or badges.
- `MembershipTier` is the canonical plan input for avatar presentation.
- `avatarUrl` and display name come from the current profile state; Coil owns memory and disk image caching.
- Generic Person icons used as form-field symbols are not user avatars and should remain normal icons.
- Photo upload and edit flows remain feature-owned; the shared avatar component owns presentation only.

## Chrome Policy

Every destination must declare its expected chrome behavior:

- `MainChrome`
- `NoBottomBar`
- `FullScreen`
- `BottomSheet`
- `Dialog`

`MainShell` must never contain feature-specific UI logic.

## Main Tabs

The default bottom navigation is:

- Home
- Nutrition
- Tio
- Workout
- Progress

Optional supported destinations include Meal Plan, Library, and You. Home remains first and users may configure three through six tabs.

When You is enabled, the avatar selects You. When You is not enabled, the avatar opens standalone Profile. Re-selecting the avatar while You is active is a no-op.

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
