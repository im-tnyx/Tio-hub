# Contributing to Tio-hub

Welcome to Tio-hub. Yeh repo TNYX/Tio ke Android and Wear OS product ka foundation hai. We care about clean architecture, predictable review process, and a friendly contributor experience.

This guide explains how to set up the project, choose the right module, create branches, open PRs, and keep code aligned with the existing Tio-Ledger-style documentation discipline.

---

## Project Overview

Tio-hub is a modular Kotlin Android/Wear repository for an AI-powered health, fitness, nutrition, workout, progress, and coaching app.

The project is intentionally modular:

```text
apps/
|-- app/        # Android phone app entry, DI, app-level navigation
|-- core/       # Design system, reusable UI, shell, global route definitions
|-- features/   # Feature-owned app areas
|-- shared/     # Pure Kotlin domain contracts for phone/watch reuse
`-- wear/       # Wear OS app
```

Current feature modules include:

- `features/auth`
- `features/onboarding`
- `features/nutrition`
- `features/workout`
- `features/profile`
- `features/settings`
- `features/progress`

Future feature ownership is documented in `apps/docs/ARCHITECTURE.md` and `apps/docs/PROFILE_SETTINGS_GUIDE.md`. Agar doubt ho ki code kahan jaana chahiye, docs ko source of truth maano.

---

## Getting Started

### Requirements

- Android Studio
- JDK 21
- Android SDK matching the project compile SDK
- Git
- Windows PowerShell or any shell that can run Gradle

### Setup

```bash
git clone <repo-url>
cd Tio-hub
```

Open `apps/` in Android Studio and sync Gradle.

From terminal:

```bash
cd apps
./gradlew :app:assembleDebug
```

Windows:

```powershell
cd apps
.\gradlew.bat :app:assembleDebug
```

### Test Commands

Run all available tests:

```bash
cd apps
./gradlew test
```

Useful focused checks:

```bash
./gradlew :shared:test
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugKotlin
./gradlew :wear:compileDebugKotlin
```

If your PR changes only docs, mention that code tests were not required.

---

## Documentation First

Before changing architecture, navigation, module ownership, Supabase boundaries, onboarding flow, or testing strategy, read the relevant docs:

| Area | Read First |
|---|---|
| Module boundaries | `apps/docs/ARCHITECTURE.md` |
| Navigation | `apps/docs/NAVIGATION_GUIDE.md` |
| Profile/Settings ownership | `apps/docs/PROFILE_SETTINGS_GUIDE.md` |
| Onboarding | `apps/docs/ONBOARDING_FLOW_DETAILED.md` and `apps/docs/TNYX_MODULAR_ONBOARDING.md` |
| Nutrition | `apps/docs/NUTRITION.md` |
| Supabase/data migration | `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md` |
| Tests | `apps/docs/TESTING_GUIDE.md` |
| Wear OS | `apps/docs/WEAR_OS_PLAN.md` and `apps/docs/WEAR_OS_PROGRESS.md` |

Rule simple hai: code change agar architecture behavior badalta hai, docs bhi update honge.

---

## Branching Guidelines

Use focused branches. Recommended naming:

```text
feature/<short-description>
fix/<short-description>
docs/<short-description>
refactor/<short-description>
test/<short-description>
chore/<short-description>
```

Examples:

```text
feature/nutrition-meal-editor
fix/workout-secondary-nav-restore
docs/contributor-guide
refactor/shared-workout-contracts
```

Branch rules:

- One branch should solve one meaningful problem.
- Avoid mixing docs, large refactors, and feature work unless they are part of the same change.
- Keep PRs reviewable. Chhota PR fast merge hota hai.
- Do not commit generated files, build outputs, local IDE files, `.env`, APK/AAB artifacts, or keystores.

---

## Pull Request Process

Before opening a PR:

1. Sync with the target branch.
2. Run the most relevant Gradle checks.
3. Confirm module boundaries are respected.
4. Update docs if behavior or architecture changed.
5. Check that no secrets or local files are committed.

Your PR description should include:

- What changed
- Why it changed
- Screenshots or screen recordings for UI changes
- Tests/checks run
- Any known follow-up work

Suggested PR template:

```markdown
## Summary

- 

## Why

- 

## Architecture Notes

- 

## Tests

- [ ] `./gradlew test`
- [ ] `./gradlew :app:compileDebugKotlin`
- [ ] Manual UI check

## Screenshots

Add if UI changed.
```

Review expectations:

- Respond to review comments respectfully.
- Prefer small follow-up commits over force-pushing during active review unless cleanup is requested.
- If a reviewer asks for an architecture reason, answer with module ownership and docs references.
- If you disagree, explain tradeoffs calmly. Healthy technical disagreement is welcome.

---

## Commit Message Style

Use clear, direct commits:

```text
docs: add contributing guide
fix: keep workout secondary nav state from back stack
feature: add meal item editor contract
refactor: move shared workout model to shared module
test: cover nutrition diary state reducer
```

Avoid vague commits like:

```text
update
final changes
bug fix
misc
```

---

## Code Standards

### Clean Architecture

Follow the module ownership rules:

- `apps/shared` contains pure Kotlin domain models, repository interfaces, and use cases that need phone/watch reuse.
- `apps/core` contains reusable design system, shell, global routing primitives, and feature-agnostic UI.
- `apps/features/<feature>` owns feature presentation, feature navigation, and feature-specific logic.
- `apps/app` wires the Android app, DI, root nav host, and platform implementations.
- `apps/wear` owns Wear OS-specific UI and watch entry points.

Do not put feature business logic in `core`. Do not put Android-only APIs in `shared`.

### MVI Contract

Use this pattern for feature screens:

```text
Route -> ViewModel -> Contract -> Screen
```

Expected responsibilities:

- `Route`: collect state/effects, connect ViewModel and navigation.
- `ViewModel`: handle actions, call repositories/use cases, update state, emit effects.
- `Contract`: define `UiState`, `Action`, and `Effect`.
- `Screen`: render state and emit actions only.
- `widgets`: receive explicit callbacks and render UI pieces.

Do not call repositories, APIs, databases, DataStore, or navigation controllers directly inside `Screen` composables.

### Type-safe Navigation

New navigation should use `@Serializable` route models/classes.

```kotlin
@Serializable
sealed interface ProfileRoute {
    @Serializable
    data object Home : ProfileRoute

    @Serializable
    data object PersonalInfo : ProfileRoute
}
```

Avoid raw string routes for new work. Cross-feature navigation should use public route contracts instead of private implementation details.

### Design System

Use `TnyxTheme` for:

- colors
- typography
- spacing/dimensions
- shapes
- component tokens
- motion
- shadows/gradients where applicable

Do not hardcode random `dp`, color, alpha, radius, or typography values in production UI unless there is a reviewed reason and the design system cannot yet express it.

Reusable UI that is used across multiple features belongs in `apps/core/src/main/java/com/tnyx/core/ui/components`. Feature-specific widgets stay inside the feature.

### Supabase Temporary Abstraction

Tio-hub is moving from hardcoded demo data toward Supabase/backend-backed slices. Until a slice is wired, temporary hardcoded data is allowed only as scaffolding.

Rules:

- Screens must not know table shape.
- ViewModels should consume repositories once a slice has persistence.
- Repository interfaces that need phone/watch reuse belong in `apps/shared`.
- Platform implementation and DI wiring belong in `apps/app` or a future data module.
- RLS, seeds, migration strategy, and validation must be considered per slice.
- No service-role key belongs in client code.

Read `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md` before adding persistence work.

### Kotlin And Compose

General expectations:

- Prefer immutable state.
- Keep composables small and readable.
- Avoid business state in `remember`; use it for visual/transient state only.
- Use `collectAsStateWithLifecycle()` in Android routes where appropriate.
- Prefer `Flow`/coroutines for async streams.
- Keep package names aligned with feature/module ownership.
- Use meaningful names; avoid generic `Manager`, `Helper`, or `Util` unless there is a narrow reason.

### Resource And Asset Rules

- Feature-specific graphics should stay with the owning feature where possible.
- Shared UI assets belong in the shared resource layer.
- Do not add large assets without checking whether they are truly needed.
- Do not commit generated image variants unless they are production assets.

---

## Testing Guidelines

Testing depth should match risk.

For docs-only changes:

- No code tests required.
- Mention docs-only in PR.

For UI-only changes:

- Compile the affected module.
- Manually verify relevant screens.
- Add screenshots if visual behavior changed.

For ViewModel/domain logic:

- Add focused unit tests.
- Cover action handling, state transitions, and error paths.

For repository/Supabase work:

- Add contract-level or repository tests where practical.
- Validate signed-in/signed-out behavior.
- Confirm user-owned data cannot leak across accounts.
- Document manual RLS validation if automation is not yet available.

For navigation changes:

- Verify start destination, back behavior, restore state, and chrome policy.
- Check nested graphs and `hasRoute(...)` detection where applicable.

---

## Security Guidelines

Never commit:

- `.env`
- private keys
- keystores
- Supabase service-role keys
- Firebase private credentials
- production secrets
- local database dumps containing real user data
- screenshots showing sensitive user or credential data

Mobile clients may use publishable/anon keys only when the architecture explicitly requires it. Server-only keys stay server-only.

---

## Accessibility And UX Standards

Tio is a health product, so UX should feel calm, reliable, and respectful.

When adding UI:

- Keep touch targets usable.
- Provide meaningful text for icons where needed.
- Do not rely only on color to communicate state.
- Respect reduced motion where supported.
- Keep screens scannable and consistent with the existing Tnyx design system.
- Avoid cluttered flows; health data already has cognitive load.

---

## Definition Of Done

A change is done when:

- It solves the stated problem.
- It compiles or has a clear reason why compile was not run.
- Tests were added or consciously scoped out.
- Module ownership is correct.
- MVI boundaries are respected.
- Navigation remains type-safe.
- Design system tokens are used for UI.
- Docs are updated if architecture/behavior changed.
- No secrets or generated junk are committed.
- PR description explains the change clearly.

---

## Getting Help

If you are unsure where a change belongs:

1. Check `apps/docs/ARCHITECTURE.md`.
2. Check the feature-specific docs.
3. Look at nearby code patterns.
4. Ask in the PR with the options you considered.

Good contribution is not just code likhna; sahi boundary ke saath code likhna is the real quality bar.

Thank you for contributing to Tio-hub.
