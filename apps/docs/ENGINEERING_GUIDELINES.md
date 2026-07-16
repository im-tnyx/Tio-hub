# Engineering Guidelines

These rules apply to production work in Tio-hub.

All production changes should satisfy the applicable items in [Definition Of Done](DEFINITION_OF_DONE.md) before merge.

## General

- Prefer readable code over clever code.
- Keep changes small, intentional, and reviewable.
- Follow existing module and package patterns before introducing a new pattern.
- Avoid unnecessary abstractions.
- Do not commit generated files, build outputs, IDE metadata, local env files, secrets, keystores, or private credentials.

## Architecture

- Respect feature ownership.
- Keep the app shell thin.
- Keep `core` feature-agnostic.
- Keep `shared` pure Kotlin unless a documented platform boundary changes.
- Do not put feature business logic in `app`, `core`, or navigation shell code.
- Do not duplicate domain rules across feature modules.
- Add or update an ADR when a durable architecture decision changes.

## Android Presentation

Use the feature-screen pattern:

- `Route`
- `Screen`
- `ViewModel`
- `UiState`
- `Action`

Rules:

- `Screen` renders state and emits actions only.
- `Screen` must not call repositories, APIs, databases, `DataStore`, `SharedPreferences`, or `NavController`.
- `Route` wires ViewModel, effects, and navigation.
- `ViewModel` owns action handling and calls domain/repository layers.
- Feature widgets stay inside the feature unless genuinely reused.

## Navigation

- Use type-safe Compose Navigation routes for new work.
- Cross-feature navigation must use public route contracts.
- Every destination should declare the expected chrome policy.
- `MainShell` must not contain feature-specific UI logic.
- Profile launches from the avatar.
- Settings launches from the gear icon.
- Main bottom tabs remain Home, Workout, Nutrition, Coach, and Progress.

## Data And Supabase

- Follow [Supabase Incremental Setup Plan](SUPABASE_INCREMENTAL_SETUP_PLAN.md).
- Do not create the full database upfront.
- Create tables only when a feature slice needs them.
- Add RLS for every client-accessible table.
- Never expose service-role keys to mobile, web, admin, or public clients.
- Hardcoded sample data is temporary scaffolding only.
- Move persistent data behind repositories as each slice becomes testable.

## UI

- Before implementing a screen, inspect `:core` theme tokens and reusable UI components and record the reuse decision.
- Use `TnyxTheme` and existing design-system components.
- Do not recreate an existing `Tnyx*` component inside a feature. Prefer composition or a feature-owned wrapper.
- Keep domain-specific widgets feature-local. Promote a widget to `:core` only when it is feature-agnostic and has demonstrated cross-feature reuse.
- Avoid random hardcoded colors, typography, spacing, radius, and alpha values.
- Keep text fitting inside its container across mobile viewports.
- Provide loading, empty, and error states when the workflow needs them.
- Keep health data screens calm, scannable, and trustworthy.

## Testing

- Testing depth should match risk and blast radius.
- Compile affected Android modules for UI/navigation/runtime changes.
- Add focused unit tests for ViewModel, reducer, domain, repository, or parser logic.
- For Supabase work, validate signed-in/signed-out behavior and user-owned data boundaries.
- For navigation work, verify start destination, back behavior, restore state, and chrome policy.

## Documentation And Review

- Update relevant docs when architecture, navigation, persistence, ownership, or product behavior changes.
- Update [Android App Progress](ANDROID_APP_PROGRESS.md) when implementation status changes.
- Update [Architecture Changelog](ARCHITECTURE_CHANGELOG.md) when project structure, data flow, module boundaries, or engineering practice changes.
- Do not merge a feature until applicable Definition Of Done items are complete or explicitly scoped out.

## AI Development Rules

- Inspect actual repo files before suggesting code changes.
- Do not invent APIs, tables, routes, modules, or business rules.
- If docs and code conflict, call out the stale source clearly.
- Keep canonical docs as source of truth.
- Treat `.ai/` as orientation only, not as canonical replacement.
