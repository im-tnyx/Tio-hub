# Definition Of Done

A feature or change is considered complete only when all applicable checklist items are satisfied.

If an item is not applicable, explain why in the PR description, implementation notes, or review summary.

## Functional

- Requirements implemented.
- User-facing behavior verified.
- Business rules verified.
- Edge cases considered.
- Loading, empty, and error states handled where applicable.

## Architecture

- Correct feature owns the UI.
- Correct feature owns business logic.
- Repository ownership matches canonical docs.
- Module boundaries are respected.
- No feature logic is added to the app shell.
- Cross-feature navigation uses public route contracts.
- Chrome policy is correct for changed destinations.

## Android Quality

- `Screen` composables remain dumb UI.
- `ViewModel` handles actions and state transitions.
- Domain and repository logic stays out of composables.
- Immutable UI state is preferred.
- No new compiler warnings are introduced.
- No `TODO`, `FIXME`, or placeholder production paths are left behind unless explicitly tracked.

## Data And Security

- No secrets, service-role keys, keystores, or private credentials are committed.
- No generated/cache/build artifacts are committed.
- Supabase tables, RLS, RPCs, and seeds are included only when the feature slice needs them.
- Client-accessible data paths respect user ownership and RLS.
- Hardcoded data is temporary scaffolding or replaced by repository-backed data.

## UI And Accessibility

- Core UI reuse audit is complete for every new or materially redesigned screen.
- UI uses `TnyxTheme` and existing components where possible.
- No existing `Tnyx*` component or token is duplicated in a feature without a documented reason.
- Layout works on expected mobile viewports.
- Touch targets are usable.
- Text does not overlap or overflow incoherently.
- Important icons have accessible labels where needed.
- Light/dark behavior is checked when the affected screen supports both.

## Testing And Validation

- Relevant compile/build command run.
- Relevant unit tests run or added.
- Navigation behavior manually verified when navigation changes.
- Supabase/RLS behavior validated when persistence changes.
- Wear OS impact considered when shared contracts change.
- Docs-only changes run `git diff --check` at minimum.

## Documentation

- Relevant canonical docs updated.
- Android progress doc updated when implementation status changes.
- ADR added or updated when a durable architecture decision changes.
- Architecture changelog updated when module boundaries, data flow, navigation policy, or engineering practice changes.

## Merge Rule

Do not merge production work that knowingly violates module ownership, leaks secrets, breaks compile, bypasses RLS, or leaves unclear hardcoded source-of-truth data in place.
