# Coding Rules

Follow the existing repository style before introducing new patterns.

## Kotlin / Android

- Use Clean Architecture boundaries.
- Use `Route + Screen + ViewModel + UiState + Action`.
- Keep Compose screens dumb.
- Keep feature logic inside the owning feature module.
- Keep shell code free of feature-specific logic.
- Complete the mandatory core reuse audit in `.ai/core/ui-rules.md` before creating or redesigning a screen.
- Prefer existing components, theme, and routing helpers; do not duplicate a core component inside a feature.
- Avoid unnecessary abstractions.
- Keep changes small and reviewable.

## Domain Boundaries

- Domain models should not depend on Compose or Android UI APIs.
- Shared domain models belong in shared modules only when multiple features, Wear OS, or future platforms need them.
- Do not duplicate business rules across feature modules.
- Do not invent backend APIs or Supabase tables without a feature slice that needs them.

## Data Rules

- No service keys, admin keys, keystores, private keys, or secrets in client code.
- No generated/cache/build artifacts in commits.
- Hardcoded sample data is temporary and must not become source of truth.
- Persistent data should move behind repositories as each feature slice is implemented.

## Validation

For Android runtime changes, prefer:

- `cd apps`
- `.\gradlew.bat :app:compileDebugKotlin`

For feature tests:

- `.\gradlew.bat :<feature>:testDebugUnitTest --no-configuration-cache`

For docs-only changes, at minimum run:

- `git diff --check`

## Canonical References

- [Engineering Guidelines](../../../apps/docs/ENGINEERING_GUIDELINES.md)
- [Definition Of Done](../../../apps/docs/DEFINITION_OF_DONE.md)
