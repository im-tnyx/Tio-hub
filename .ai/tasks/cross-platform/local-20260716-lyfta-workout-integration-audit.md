---
task_id: local-20260716-lyfta-workout-integration-audit
title: Tio Lyfta workout integration audit and staged plan
status: completed
priority: high
owner: cross-platform
branch: codex/profile-supabase-data-boundary
github_issue:
github_pr:
platforms: [android, watch, design-reference, flutter-comparison]
created: 2026-07-16
updated: 2026-07-16
---

# Task: Tio Lyfta workout integration audit and staged plan

## Goal

Audit the Lyfta 1.577 behavior reference, the actual Tio-hub Android/Wear workout runtime, and the separately developed Tnyx-hub Flutter workout work; then produce a Tio-hub-only staged implementation plan.

## Acceptance Criteria

- [x] Phone Android workout runtime is audited from checked-in `Tio-hub` source.
- [x] Wear workout runtime and Phone/Watch sync truth are audited.
- [x] Lyfta capability inventory is separated into keep, adapt, defer, and skip decisions.
- [x] Tnyx-hub Flutter work is treated only as an explicit implementation comparison.
- [x] Asset/data provenance and remote-media dependencies are identified as release gates.
- [x] Final roadmap names Tio-hub modules, dependencies, validations, and stage exit criteria.

## Scope

- `G:\projects\Tio-hub` as the product and implementation target.
- `design/references/lyfta/lyfta_1.577` as a behavior reference.
- `G:\projects\Tnyx-hub\apps\flutter` as a separate comparison requested by the user.
- Native Android, shared Kotlin contracts, Wear OS, workout data boundary, and staged delivery order.

## Out Of Scope

- Implementing workout code during the audit.
- Copying Lyfta code, datasets, media, body maps, strings, or branding.
- Treating Tnyx-hub backend/contracts/database status as Tio-hub truth.
- Applying Supabase migrations or changing external systems.
- Committing, pushing, merging, or opening a Pull Request.

## Current State

- `Tio-hub` is a native Kotlin Android/Wear repository; there is no Flutter runtime in this checkout.
- Phone Workout is currently an intentional placeholder in `apps/features/workout/.../WorkoutNavGraph.kt`.
- `apps/shared` contains initial `WorkoutSet`, `WorkoutSession`, `WorkoutRoutine`, and `WorkoutRepository` contracts, but no checked-in phone repository implementation has been found.
- Wear has static workout/history UI, simulated sync text, and no checked-in workout Room/Data Layer runtime.
- `apps/wear/src/main/res/raw/app_exercises.json` contains 433 rows; every row declares `source.provider = "lyfta"`.
- The Wear catalog contains verified third-party remote dependencies: 619 `apilyfta.com` image links and 866 CloudFront video links plus 866 CloudFront thumbnail links.
- The bundled Wear catalog is not byte-identical to Lyfta's raw `exercises.json`; it is a transformed derivative with source metadata. Provenance and licence clearance remain mandatory.
- `G:\projects\Tnyx-hub\apps\flutter` contains substantial workout work but belongs to another repository. It may inform behavior and modeling only.
- The current branch contains unrelated uncommitted Profile/Supabase changes. They must remain untouched by this audit.

## Workstreams

### Android Phone

- [x] Audit navigation, screens, ViewModels, use cases, repositories, persistence, and tests.
- [x] Confirm placeholder versus implemented workout behavior.
- [x] Map the minimum first vertical slice.

### Wear OS

- [x] Audit workout/history UI, local state, catalog loading, listener services, and sync claims.
- [x] Separate mock/static presentation from working runtime.
- [x] Define Phone/Watch ownership and offline authority.

### Reference And Transfer

- [x] Inventory major Lyfta workout capabilities.
- [x] Confirm Lyfta is a decompiled APK reference, not buildable source.
- [x] Correct the repository scope: Tio-hub is target; Tnyx-hub Flutter is comparison only.
- [x] Map reusable Flutter concepts to Kotlin modules without copying implementation blindly.
- [x] Complete licence/provenance risk matrix.

### Delivery Plan

- [x] Produce Tio-hub-specific stages with dependencies and exit gates.
- [x] Separate reliable workout core from deferred social, marketplace, AI, and monetization scope.

## Decisions

- The target repository is `G:\projects\Tio-hub`.
- `G:\projects\Tnyx-hub` is not canonical truth for this task.
- The plan will use selective expansion: independently build the reliable workout core first, not clone the whole Lyfta product at once.
- Lyfta-derived assets/data and remote URLs are release-blocking until explicit provenance and licence clearance.
- Phone Room snapshots plus a transactional mutation outbox are the local-first runtime boundary.
- The first implementation milestone is a thin offline flow: blank workout -> one set -> finish -> history -> restart recovery.
- Watch follows the stable Phone engine and sends durable mutations through Wear Data Layer; it does not write directly to Supabase.
- Production cloud sync stays feature-flagged off until a backend-mediated write path exists.
- Existing unrelated Profile changes will be preserved.

## Release Gates

- Exercise catalog, media, standards, and artwork licensing/provenance are not established.
- Tio-hub has no checked-in production workout backend; remote sync cannot be claimed until that boundary exists.
- Phone/Watch event identity, sequencing, acknowledgement, retry, and reconciliation must be implemented and tested before Wear beta.
- Social, coaching marketplace, challenges, AI generation, subscription, ads, advanced recovery, and third-party integrations remain outside the 90-day core.

## 90-Day Delivery Order

1. Days 1-5: scope, provenance, stable IDs, privacy, and architecture gates.
2. Days 6-12: shared workout contract v2 and reducer tests.
3. Days 13-20: Phone Room database, repositories, transactions, outbox, and recovery.
4. Days 21-25: first complete offline workout vertical slice.
5. Days 26-45: exercise library and routine builder.
6. Days 46-66: full active workout, finish, history, edit, and basic metrics.
7. Days 67-73: plan, schedule, and reminders.
8. Days 74-82: conditional Wear MVP after Phone gates pass.
9. Days 83-90: migrations, offline/reconnect QA, accessibility, performance, provenance scan, and scoped release decision.

## Files Touched

- `AGENTS.md`
- `.ai/CURRENT.md`
- `.ai/README.md`
- `.ai/doc-map.md`
- `.ai/project-context.md`
- `.ai/workflow.md`
- `.ai/tasks/README.md`
- `.ai/templates/task-template.md`
- `.ai/tasks/cross-platform/local-20260716-lyfta-workout-integration-audit.md`

## Validation

- Command: `git status --short --branch`
- Result: current branch and unrelated dirty Profile work recorded.
- Command: source inspection of `WorkoutNavGraph.kt`, shared workout contracts, Android progress docs, Wear catalog, and explicit repository boundaries.
- Result: phone placeholder and initial shared-contract state confirmed.
- Command: SHA256 comparison of Wear `app_exercises.json` and Lyfta raw `exercises.json`.
- Result: different hashes; Wear file is a transformed catalog.
- Command: parsed Wear catalog provenance/media counts.
- Result: 433 Lyfta-attributed rows, 619 `apilyfta.com` image links, 866 CloudFront video links, and 866 CloudFront thumbnail links.
- Command: independent module-ownership, feasibility, and architecture challenge passes.
- Result: sequencing converged on shared contracts -> Room -> thin Phone slice -> catalog/routines -> full engine -> history/schedule -> conditional Wear/cloud.

## Next Action

Publish the existing Profile and AI-continuity work, then start workout implementation on a dedicated branch with shared contract v2 and the thin offline vertical slice.

## Canonical References

- `AGENTS.md`
- `apps/AGENTS.md`
- `.ai/CURRENT.md`
- `apps/docs/ANDROID_APP_PROGRESS.md`
- `apps/docs/ARCHITECTURE.md`
- `apps/docs/PROFILE_SETTINGS_GUIDE.md`
- `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`
- `apps/docs/WEAR_OS_PROGRESS.md`
- `apps/features/workout/src/main/java/com/tnyx/features/workout/navigation/WorkoutNavGraph.kt`
- `apps/shared/src/main/java/com/tnyx/shared/workout/`
- `apps/wear/src/main/java/com/tnyx/wear/`
