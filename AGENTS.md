# Tio-hub AI Agent Instructions

Truth Boundary: This file defines repository-wide working rules for `Tio-hub`. Runtime source/config remains executable truth. Canonical Android/Wear documents remain product, ownership, and implementation-status truth for their scopes.

## Session Start Order

For every task in this repository:

1. Read this `AGENTS.md`.
2. Read `.ai/CURRENT.md`.
3. If `Active Task` is not `none`, read only the referenced task file.
4. For work under `apps/`, read `apps/AGENTS.md`.
5. Read only the canonical documents and runtime source relevant to the requested work.

Do not preload every file under `.ai/tasks/`, `.ai/archive/`, `apps/docs/`, or `design/references/`.

## Repository Identity

- `G:\projects\Tio-hub` is this repository and currently owns the native Kotlin Android and Wear OS applications under `apps/`.
- `G:\projects\Tnyx-hub` is a separate repository. Its Flutter, backend, contracts, database, docs, branches, and task state are not runtime truth for `Tio-hub`.
- Use another repository only as an explicit comparison/reference when the user requests it.
- Never silently transfer implementation status, architecture claims, backend availability, or database truth between repositories.

## Truth Precedence

When sources conflict:

1. Checked-in runtime source/config is actual behavior truth.
2. `apps/docs/ANDROID_APP_PROGRESS.md` is Android implementation-status truth.
3. `apps/docs/PROFILE_SETTINGS_GUIDE.md` is feature-ownership truth.
4. Other relevant `apps/docs/*` files are platform implementation-detail truth.
5. GitHub Issues and Pull Requests are durable collaboration history.
6. `.ai/tasks/*` contains compact execution checkpoints only.

Report stale or conflicting documentation explicitly. Do not invent missing architecture or feature status.

## Architecture Boundaries

- Android uses feature-owned Clean Architecture.
- Follow `Route + Screen + ViewModel + UiState + Action`.
- Compose screens render state and emit actions; they do not own repositories, network calls, database calls, or mutable business logic.
- `apps/app/` owns Android composition, DI, and platform data implementations.
- `apps/core/` owns feature-agnostic design system, routing contracts, and shell primitives.
- `apps/features/<feature>/` owns feature presentation and feature-specific logic.
- `apps/shared/` contains pure Kotlin contracts only when Phone and Wear or multiple owners reuse them.
- `apps/wear/` owns Wear-specific runtime behavior.
- `supabase/` owns checked-in migrations until an explicitly approved repository migration changes that boundary.

Before creating or materially redesigning any Compose screen, inspect `apps/core/src/main/java/com/tnyx/core/theme/` and `apps/core/src/main/java/com/tnyx/core/ui/components/`. Reuse `TnyxTheme` tokens and an existing core component before introducing a feature-local visual primitive. Do not duplicate an existing `Tnyx*` component inside a feature.

Keep feature semantics in the owning feature. A component may move to `apps/core/` only when it is domain-neutral and has demonstrated cross-feature reuse; otherwise keep it under the feature's `widgets/` package and compose it from core primitives.

## Reference And Provenance Rules

- `design/references/` is reference material, not product-owned runtime source.
- Decompiled apps may explain behavior and UX patterns, but must not be copied as architecture or implementation source.
- Do not ship copied third-party code, datasets, media, body maps, branding, strings, or remote asset dependencies without explicit provenance and licence clearance.
- Product concepts may be independently implemented using Tio contracts, design system, and security boundaries.

## Working Rules

- Inspect relevant source and existing user changes before making edits.
- State what will change, why, assumptions, risks, and validation.
- Preserve unrelated uncommitted work.
- Prefer small vertical slices over broad rewrites.
- Never expose service-role keys, admin credentials, private tokens, keystores, or secrets in clients or task files.
- Do not commit, push, merge, open a Pull Request, apply database changes, or modify external systems unless the user explicitly requests it.
- Do not claim production readiness unless runtime source and canonical documents prove it.

## Active Task And Continuity

- Keep one primary active task in `.ai/CURRENT.md`.
- Put a task under `.ai/tasks/cross-platform/` only when one objective genuinely spans Android, Wear, data/contracts, or an explicit external comparison.
- Update task checkpoints after meaningful milestones, decisions, blockers, validation changes, or next-action changes—not after every edit.
- Move completed tasks to `.ai/archive/<year>/` only after merge or explicit closure.
- Never store secrets, raw private prompts, user data, or environment values in `.ai/`.

## Communication

- Reply in Hindi unless the user explicitly requests another language.
- Keep code, paths, commands, APIs, classes, functions, and technical terms in English.
- Be direct about what is implemented, partial, planned, blocked, risky, or unknown.
