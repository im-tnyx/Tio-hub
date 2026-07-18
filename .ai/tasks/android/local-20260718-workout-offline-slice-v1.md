---
task_id: local-20260718-workout-offline-slice-v1
title: Workout first thin offline vertical slice
status: in_progress
priority: high
owner: android
branch: codex/workout-offline-slice-v1
github_issue:
github_pr:
platforms: [android]
created: 2026-07-18
updated: 2026-07-18
---

# Task: Workout first thin offline vertical slice

## Goal

Replace the Workout placeholder with the smallest production-shaped Phone flow that starts a blank session, adds one first-party starter exercise, completes one set, finishes once, shows history, and restores durable state after process recreation.

## Acceptance Criteria

- [x] Workout follows `Route + Screen + ViewModel + UiState + Action -> UseCase -> Repository`.
- [x] Empty state can start one blank session through `WorkoutRepository`.
- [x] Active state can add one provenance-safe starter exercise without a third-party catalog or media.
- [x] The user can enter valid reps and complete exactly one durable set.
- [x] Finish writes one completed history session and repeated taps cannot duplicate it.
- [x] Active and completed state render from repository flows after recreation.
- [x] UI reuses `TnyxTheme` and existing core components without duplicated primitives.
- [x] Unit tests cover mutation order, input rejection, recovery-facing state, and finish idempotency.
- [ ] Shared, Phone, and Wear compile gates must be rerun after the reusable-editor refactor.

## Scope

- `apps/features/workout/`
- Workout navigation integration only where required
- Stage 3 progress and architecture checkpoints

## Out Of Scope

- Full exercise catalog, search, filters, detail, or catalog ingestion.
- Third-party code, datasets, media, branding, strings, or remote assets.
- Routine builder, advanced set types, rest timer UX, notes, reordering, or replacement.
- Gender-aware media presentation; this begins with the Stage 4 catalog/media slice.
- Wear runtime, Data Layer, sensors, backend, Supabase, cloud sync, or community.
- Commit, push, Pull Request, merge, or deployment unless separately requested.

## Decisions

- Use one feature-owned first-party starter definition for the Stage 3 proof; it is not a catalog claim.
- Read the next per-device Phone mutation sequence from the persisted Room outbox so sequencing remains monotonic across process recreation and multiple sessions.
- Keep all mutations behind a feature use case/coordinator; Compose only renders `UiState` and emits `Action`.
- Render history from the existing Room-backed repository flow and keep the completed session identity stable.
- Keep visual state values in the mandatory `TnyxTheme.components` chain: core Button, Input, Card, and Header consumers now read tokenized colors, dimensions, borders, and typography; Workout uses the semantic success color for completion state.
- Replace the singular fixed-reps proof UI with a feature-owned reusable exercise editor: keyed exercise/set/metric state, multiple-exercise rendering, expanded/collapsed cards, tracking-type-driven metric fields, and explicit Active/Routine/Read-only modes. Only Active mode is wired in this slice.
- Extend contract v2 additively with defaulted tracking snapshots and `steps`; no Room schema migration is required because these values are serialized inside existing JSON payloads.

## Validation

- `:features:workout:testDebugUnitTest`: PASS; 7 tests, 0 failures, 0 errors, 0 skipped.
- `:shared:test :features:workout:testDebugUnitTest :app:testDebugUnitTest :app:compileDebugKotlin :wear:compileDebugKotlin --no-configuration-cache`: PASS on 2026-07-18 with `JAVA_HOME=G:\dev\jdk-17`.
- Phone Room persistence/recovery suite: PASS; 7 repository tests, 0 failures, including outbox sequence recovery after database reopen.
- Theme compliance scan: PASS; no raw `Color(...)`, hex color, `.dp`, `.sp`, literal alpha, or `FontWeight` remains in Workout UI or the reused Button, Input, Card, and Header consumers.
- `:core:compileDebugKotlin :features:workout:compileDebugKotlin :app:compileDebugKotlin --no-configuration-cache`: PASS after token-chain cleanup.
- Reusable-editor focused gate first run: `:shared:test` PASS; feature compile reported one nullable-call error and one invalid Compose import, both corrected.
- Corrected focused gate rerun: BLOCKED by Codex external Gradle-cache usage limit before Gradle execution. Final feature tests/compile have not been re-proven after the editor refactor.
- GitHub Actions was not used as a validation gate because the user reported an account/action limit issue.

## Next Action

Rerun `:shared:test :features:workout:testDebugUnitTest :features:workout:compileDebugKotlin`, then the full Phone/Wear gate. After it passes, reinstall and repeat start -> add -> edit metric -> complete -> collapse/expand -> finish -> history -> force-stop/relaunch against the reusable editor. Commit/push only on explicit user request.

## Canonical References

- `AGENTS.md`
- `.ai/CURRENT.md`
- `apps/AGENTS.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/shared/src/main/java/com/tnyx/shared/workout/`
