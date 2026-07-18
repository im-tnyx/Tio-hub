---
task_id: local-20260718-muscle-map-registry-v1
title: Workout reusable gender-aware muscle map registry
status: in_progress
priority: high
owner: android
branch: codex/muscle-map-registry-v1
github_issue:
github_pr:
platforms: [android]
created: 2026-07-18
updated: 2026-07-18
---

# Task: Workout reusable gender-aware muscle map registry

## Goal

Create a feature-owned Android muscle-map boundary that composes a gender-specific body base with one or more front/back muscle overlays without leaking asset filenames or reference taxonomy IDs into Workout domain data.

## Acceptance Criteria

- [x] Existing muscle-map assets have a recorded pair, naming, dimension, and alpha audit.
- [x] Debug builds package the local muscle-map source without duplicating it into release resources.
- [x] `ExerciseMediaVariant.MALE` and `FEMALE` resolve independently; `NEUTRAL` never silently becomes male.
- [x] Canonical Tio muscle-group names and aliases resolve to typed region keys.
- [x] Front and back overlay lists are deterministic and deduplicated.
- [x] Missing assets degrade to a release-safe placeholder instead of crashing.
- [x] A reusable feature-owned Compose component renders base plus overlays.
- [x] Focused tests cover aliases, multi-overlay behavior, gender variants, neutral fallback, and unknown values.
- [ ] Workout feature compile/test gate passes.
- [ ] Device preview verifies quadriceps male/female compositing on light and dark Tio surfaces.

## Scope

- `apps/assets/final/musclemap/`
- `apps/features/workout/build.gradle.kts`
- `apps/features/workout/.../components/musclemap/`
- Focused registry tests and task checkpoints

## Out Of Scope

- Exercise catalog ingestion or numeric reference-ID migration.
- Profile-owned gender preference storage or UI.
- Wiring muscle maps into an Exercise Detail screen that does not yet exist.
- Shipping reference-derived artwork in release builds without provenance and licence clearance.
- Moving Workout taxonomy into `apps/core/`.

## Decisions

- Adapt the supplied base-plus-overlay concept, but do not copy its Flutter API or opaque numeric IDs.
- Reuse shared `ExerciseMediaVariant` rather than creating a second gender enum.
- Treat `AUTO` resolution as upstream Profile/Workout policy; this renderer receives only a resolved variant.
- Package `apps/assets/final/musclemap` through the Workout debug asset source set only.
- Keep asset filenames inside the presentation registry; domain models remain resource-agnostic.
- Normalize minor canvas differences at render time with `ContentScale.FillBounds`; validate visually before broader UI wiring.
- Preserve user-owned trapezius renames already present when the branch started.

## Audited Baseline

- 95 WebP files: 48 `_male`, 45 `_female`, and 2 unsuffixed/typo names before normalization.
- Every file is RGBA with transparency.
- Dimensions vary from 299-304 px wide and 732-739 px high; overlays are not one uniform canvas.
- Known naming mismatches include `deltoid/deltoids`, `feamale`, `oliques/obliques`, `wris/wrist`, and `tensor_fasciae_femoris/latae`.
- Male back trapezius uses upper/middle/lower layers while female back trapezius is one combined layer.
- After safe filename normalization: 49 male, 46 female, zero unsuffixed files, and five intentional asymmetric pairs.

## Data Flow

```text
ExerciseDefinition.primaryMuscleGroups
             +
resolved ExerciseMediaVariant
             |
             v
MuscleMapAssetRegistry
             |
             v
front/back base + deduplicated overlays
             |
             v
TioMuscleMap -> async debug asset loading
             |
       loaded | unavailable / neutral / unknown
             v
layered map   Tio placeholder
```

## Validation

- Asset inventory and visual sampling: PASS.
- Dimension/mode/alpha audit: PASS with normalization caveat recorded above.
- Registry references 62 asset files; missing-reference scan: PASS with zero missing.
- Debug-only source-set path resolution: PASS.
- Diff and changed Compose hardcode scans: PASS.
- Kotlin/Gradle and device gates: pending.

## Next Action

Run the focused Workout compile/test gate, then validate the Bodyweight Squat quadriceps male/female layer composition on-device before expanding UI integration.

## Canonical References

- `AGENTS.md`
- `apps/AGENTS.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/model/ExerciseDefinition.kt`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/model/ExerciseMedia.kt`
