---
task_id: local-20260718-body-part-icon-registry-v1
title: Workout reusable body-part icon registry
status: in_progress
priority: high
owner: android
branch: codex/body-part-icon-registry-v1
github_issue:
github_pr: 22
platforms: [android]
created: 2026-07-18
updated: 2026-07-18
---

# Task: Workout reusable body-part icon registry

## Goal

Create a production-safe, feature-owned Android body-part icon boundary that can be reused by workout catalog, filter, routine, and logging UI without leaking resource names into workout domain data.

## Acceptance Criteria

- [x] Fourteen active body-part categories resolve from canonical names and common aliases.
- [x] Unknown, empty, and unsupported values resolve to a release-safe placeholder.
- [x] Runtime resource names use Tio-owned naming rather than copied upstream identifiers.
- [x] Preview artwork is isolated to the `debug` source set.
- [x] `main` and release variants contain only placeholder aliases until provenance and licence clearance.
- [x] Feature UI can render a body-part icon through one reusable composable.
- [x] Workout dashboard carries resource-free catalog metadata into `WorkoutExerciseUi`.
- [x] Workout editor resolves its header icon from the exercise's stable catalog identity.
- [x] Unit tests cover canonical names, normalization, aliases, and fallback behavior.
- [ ] Workout feature tests and compile gate pass.
- [ ] A device preview verifies all fourteen debug icons on light and dark Tio surfaces.

## Scope

- `apps/features/workout/src/main/res/`
- `apps/features/workout/src/debug/res/`
- `apps/features/workout/.../components/bodypart/`
- Registry-focused tests and task checkpoints

## Out Of Scope

- Equipment icon and equipment photo registry.
- Exercise catalog ingestion, catalog persistence, or body-part metadata migration.
- Muscle-map male/female asset naming and resolution.
- Provenance changes beyond the feature-owned starter definition.
- Moving domain-specific body-part semantics into `apps/core/`.
- Production release of reference-derived artwork without approved provenance and licence evidence.

## Decisions

- Keep the body-part registry feature-owned because its taxonomy is workout/catalog domain language, not a domain-neutral core primitive.
- Keep `ExerciseDefinition` resource-agnostic. Presentation resolves its string body-part value to an Android drawable at the feature boundary.
- Resolve editor metadata by stable `exerciseId`; never infer a body part from the display name.
- Keep the Stage 3 first-party `Bodyweight Squat` definition in the feature coordinator until full catalog ingestion owns it.
- Use `debug` resource overlays for the current artwork. The same names resolve to a neutral placeholder in `main` and release builds.
- Normalize the twelve self-contained SVG files to lossless 192x192 transparent WebP for deterministic Android loading.
- Keep cardio and stretching as debug VectorDrawable resources with corrected Tio resource names.
- Do not infer unsupported semantic mappings such as `full body -> abs`; unsupported values must use the placeholder.

## Data Flow

```text
ExerciseDefinition.primaryMuscleGroups
             |
             v
WorkoutDashboard -> WorkoutExerciseUi.bodyPart
             |
             v
BodyPartIconRegistry.normalize + alias lookup
             |
       known | unknown
             |----------------------+
             v                      v
Tio body-part resource       Tio placeholder
             |
             v
TioBodyPartIcon -> Compose Image

debug:   body-part resource -> preview artwork
release: body-part resource -> placeholder alias
```

## Validation

- `aapt2 compile --dir apps/features/workout/src/main/res`: PASS.
- `aapt2 compile --dir apps/features/workout/src/debug/res`: PASS.
- `aapt2 link` with the debug overlay over main placeholder aliases: PASS.
- Twelve generated WebP resources: PASS at 192x192 with alpha channel.
- XML parse: PASS for all four new XML resource files.
- Trailing-whitespace scan: PASS.
- Editor metadata flow and all `WorkoutDashboard` call sites: static scan PASS.
- Changed Compose code introduces no raw `dp` or `Color` hardcodes: PASS.
- Kotlin unit tests and Gradle compile: pending.
- Device light/dark preview: pending.

## Next Action

Run the focused Workout test/compile gate, then verify the editor header and all fourteen debug icons on light and dark Tio surfaces.

## Canonical References

- `AGENTS.md`
- `apps/AGENTS.md`
- `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/model/ExerciseDefinition.kt`
- `apps/shared/src/main/java/com/tnyx/shared/workout/domain/logic/ExerciseMediaResolver.kt`
