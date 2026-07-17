# Workout Product And UX Blueprint

Status: Canonical planned product and implementation target

Implementation status: Planned. This document does not mean that the Workout runtime is implemented.

Last updated: 2026-07-16

## Purpose

This document defines how Tio will build its Phone and Wear Workout experience from the audited Lyfta behavior reference without copying Lyfta's visual identity, code, content, or assets.

The product rule is:

> Tio may use a different visual UI, but the core workout UX, task flow, state transitions, recovery behavior, and user outcomes should remain equivalent to the audited Lyfta behavior unless this document explicitly improves or removes a behavior.

Tio makes one planned capability explicit and user-controlled: an exercise can show male, female, or neutral image and video presentation without creating separate exercise identities.

## Truth Boundary

Use these sources in this order:

1. Checked-in runtime source and configuration are actual behavior truth.
2. [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md) is current Android implementation-status truth.
3. This document is the canonical planned Workout product, UX, architecture, and delivery target.
4. `design/references/lyfta/lyfta_1.577` is behavior research material only.
5. `G:\projects\Tnyx-hub\apps\flutter` is an external screen and interaction comparison only.

Current source now proves that:

- Phone Workout is one placeholder destination in `apps/features/workout/`.
- `apps/shared/` contains Workout contract v2, the deterministic reducer, gender-aware media resolution, and repository boundaries.
- Phone Room database v1, atomic snapshot/outbox persistence, `RoomWorkoutRepository`, Hilt composition, and targeted recovery tests exist in `apps/app/`.
- Production Workout screens do not consume the repository yet. The Stage 2 persistence/recovery exit gate now passes on the current Android branch, and the next engineering gate is Stage 3 (`First thin offline vertical slice`).
- Wear contains prototype screens, local JSON parsing, simulated sync text, and a boolean gender preference. It is not the target Phone/Watch runtime.
- The checked-in Wear exercise catalog and its third-party media remain blocked for release until provenance and licence clearance are explicit.

Do not mark a stage complete in this document. Record implementation progress in [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md) and [WEAR_OS_PROGRESS.md](WEAR_OS_PROGRESS.md).

## Reference Hierarchy

| Source | What Tio may learn from it | What it cannot decide |
|---|---|---|
| Lyfta 1.577 reference | Capabilities, user tasks, screen purpose, flow order, information hierarchy, interaction outcomes, state transitions, validation, feedback, timer behavior, and recovery expectations | Tio branding, source architecture, data ownership, security, copied strings, copied code, copied assets, or release-ready media |
| Tio runtime and canonical docs | Architecture, module ownership, data boundaries, navigation, design tokens, accessibility, privacy, offline authority, and implementation status | They must not silently claim unfinished Workout behavior |
| Tnyx Flutter comparison | Useful screen arrangements, interaction ideas, and prototypes that can shorten design exploration | Tio roadmap order, backend truth, database truth, production readiness, or Kotlin architecture |
| Tio design system | Colors, typography, components, spacing, motion language, responsive layout, and brand identity | It must not remove required task states or make the UX flow incomplete |

## UI May Differ, UX Must Remain Equivalent

### Required UX equivalence

For each retained Lyfta capability, Tio must preserve or deliberately improve:

- the user's goal and the shortest understandable path to complete it;
- the purpose and information hierarchy of each screen;
- navigation and back behavior;
- selection, edit, save, cancel, discard, and confirmation semantics;
- validation and prevention of invalid workout state;
- loading, empty, error, offline, and retry behavior;
- active-session continuity after process death, restart, or temporary disconnection;
- set completion, rest timer, exercise replacement, reorder, skip, and finish behavior;
- visible feedback after every durable mutation;
- history and previous-performance continuity;
- accessibility semantics and non-media instructions.

### Tio-owned UI decisions

Tio may independently change:

- color, typography, icons, cards, surfaces, spacing, and component styling;
- exact screen composition when the task order and information priority remain intact;
- animation style and duration, subject to reduced-motion support;
- copywriting, terminology, and localization;
- Phone and Wear layout adaptations;
- navigation presentation when Tio's type-safe graph and chrome policy require it.

This is behavioral parity, not pixel parity. A screen that looks different but completes the same job reliably is acceptable. A visually similar screen that loses validation, recovery, feedback, or an important state is not acceptable.

### Prohibited transfer

Do not ship copied Lyfta code, datasets, media, body maps, branding, strings, identifiers, or remote asset dependencies without explicit provenance and licence approval. Product concepts must be independently implemented using Tio contracts and design components.

## Product Dependency Order

The product must be understood in this dependency order:

```text
Exercise Definition
    -> Exercise Library
    -> Workout Setup / Routine Builder
    -> Active Workout
    -> Finish / History
    -> Plan / Schedule
    -> Progress Analytics
    -> Wear Companion / Integrations
    -> Optional Coaching / Monetization only after separate approval
```

The engineering proof order is slightly different because persistence and recovery must exist before broad UI work:

```text
Shared contracts
    -> Phone Room + transactional outbox
    -> Thin offline workout slice
    -> Exercise library + media resolver
    -> Routine builder
    -> Full active workout engine
    -> History + schedule
    -> Conditional Wear runtime
    -> Conditional backend-mediated cloud sync
```

## Core UX Scope

### Exercise Library

The user can browse, search, filter, inspect, and select an exercise. Exercise detail provides the movement name, target muscles, equipment, instructions, approved media, and relevant alternatives. Text instructions must remain usable when media is missing or offline.

### Workout Setup

The user can start a blank workout or select a routine, add or remove exercises, reorder exercises, configure planned sets, reps, rest, and set types, then start without losing the draft.

### Active Workout

The active session is the authoritative working state. The user can log and edit sets, complete sets, run or adjust rest, view previous values, replace or skip an exercise, add notes, and finish or discard with explicit confirmation. Process recreation must restore the last durable state.

### Finish And History

Finishing a workout creates one durable completed session. Summary and history use the same session identity and set data. Repeated taps, retries, or reconnects must not create duplicate sessions or duplicate sets.

### Plan And Schedule

Planning uses stable routine identities and scheduled occurrences. It does not create a second routine truth. Reminders are derived from saved schedule state and remain optional.

### Wear Companion

Phone owns canonical workout definitions, routines, history, and reconciliation. Wear owns its local active-session cache and can record durable offline mutations. Wear sends identified, ordered, retryable mutations through Wear Data Layer. It never writes directly to Supabase.

## Gender-Aware Exercise Media

### Product behavior

One exercise keeps one canonical `exerciseId`. Male, female, and neutral presentation are media variants of that exercise, not separate exercises.

Changing the media preference must change only the approved image, thumbnail, or video presentation. It must not change:

- routine identity;
- exercise identity;
- instructions or tracking semantics;
- sets, reps, weight, RPE, rest, history, or progress calculations;
- prior workout records.

Variant-specific coaching text is allowed only when separately authored, reviewed, and linked to the same canonical exercise. The app must not infer different biomechanics, difficulty, or programming solely from gender.

### User preference

Workout settings expose these options:

| Preference | Resolution |
|---|---|
| `AUTO` | Use the Profile gender only as the default media preference |
| `MALE` | Prefer approved male presentation media |
| `FEMALE` | Prefer approved female presentation media |
| `NEUTRAL` | Prefer approved neutral or non-gendered presentation media |

Rules:

- `AUTO` is the initial setting.
- A user-selected Workout media preference overrides the Profile value.
- Profile `Male` maps to `MALE`; Profile `Female` maps to `FEMALE`.
- Profile `Other`, blank, unavailable, or unsupported values map to `NEUTRAL`.
- A Profile change may update `AUTO` resolution but must never overwrite an explicit Workout override.
- The preference is private user configuration. Do not expose the raw Profile gender in media URLs, analytics labels, logs, or cache folder names.

### Shared contract v2

Stage 1 implements this core contract under `apps/shared/src/main/java/com/tnyx/shared/workout/`. Runtime source remains authoritative for the complete field list:

```kotlin
import kotlinx.serialization.Serializable

const val WORKOUT_CONTRACT_VERSION: Int = 2

@Serializable
enum class ExerciseMediaPreference {
    AUTO,
    MALE,
    FEMALE,
    NEUTRAL
}

@Serializable
enum class ExerciseMediaVariant {
    MALE,
    FEMALE,
    NEUTRAL
}

@Serializable
enum class ExerciseMediaReleaseStatus {
    BLOCKED,
    APPROVED
}

@Serializable
data class ExerciseMediaAsset(
    val id: String,
    val variant: ExerciseMediaVariant,
    val imageRef: String? = null,
    val videoRef: String? = null,
    val thumbnailRef: String? = null,
    val mediaVersion: Int = 1,
    val provenanceId: String,
    val releaseStatus: ExerciseMediaReleaseStatus = ExerciseMediaReleaseStatus.BLOCKED
)

@Serializable
data class ExerciseDefinition(
    val id: String,
    val name: String,
    val schemaVersion: Int = WORKOUT_CONTRACT_VERSION,
    val aliases: List<String> = emptyList(),
    val primaryMuscleGroups: List<String> = emptyList(),
    val secondaryMuscleGroups: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val mediaAssets: List<ExerciseMediaAsset> = emptyList()
)
```

`imageRef`, `videoRef`, and `thumbnailRef` are stable media references, not an instruction to put arbitrary third-party URLs in shared domain code. `ExerciseMediaResolver` ignores blocked, unprovenanced, empty, or invalid-version assets. Platform data implementations will resolve approved references to bundled, cached, signed, or backend-provided locations.

### Resolution and fallback

Use one pure, unit-tested resolver:

```text
explicit Workout preference
    -> Profile-derived preference when setting is AUTO
    -> exact approved media variant
    -> approved NEUTRAL media
    -> local generic exercise placeholder
```

Do not silently switch `MALE` to `FEMALE` or `FEMALE` to `MALE` when an exact variant is missing. The neutral or placeholder fallback keeps behavior predictable.

The resolver returns a resolved asset plus a reason such as `EXACT`, `NEUTRAL_FALLBACK`, or `PLACEHOLDER`. Reason codes support testing and diagnostics without logging sensitive profile data.

### Media delivery rules

- Each asset requires a durable provenance record and approved licence state before release.
- A catalog row is not release-ready merely because its URL loads.
- Cache keys include `exerciseId`, `variant`, `mediaVersion`, and media type.
- Offline mode uses only the last approved cached asset for the resolved variant.
- A failed or corrupt video falls back to the approved image, then the local placeholder.
- Captions or text instructions remain available when video cannot play.
- Media playback follows reduced-motion, autoplay, network, and data-saver preferences.
- Watch receives only the selected compact thumbnail metadata needed for its current flow. It does not download the full video catalog.

### Current prototype gap

The current Wear `loadAllExercisesFromJson` implementation uses `pref_is_female: Boolean`, defaults to male, and falls back to the opposite gender. A legacy local thumbnail also takes priority without a gender-specific cache key. That behavior is a prototype, not this target contract. It cannot represent `AUTO`, `NEUTRAL`, Profile `Other`, explicit override ownership, provenance state, or a safe fallback reason.

The audited Wear catalog currently has 433 exercise rows with male and female media objects. Those rows and remote media are reference evidence only until provenance and licence gates pass.

## Target Architecture

| Owner | Responsibility |
|---|---|
| `apps/shared/workout/` | Stable pure Kotlin workout identities, session/routine/event contracts, media preference and variant types, reducers, and repository interfaces shared by Phone and Wear |
| `apps/features/workout/` | Feature routes, screens, `ViewModel`, `UiState`, actions, use cases, UX orchestration, and Workout settings |
| `apps/app/` | Phone Room entities/DAOs, repository implementations, media reference resolution, cache, outbox, DI, and backend adapter |
| `apps/core/` | Feature-agnostic design tokens, reusable UI primitives, accessibility helpers, and shell contracts |
| `apps/wear/` | Wear presentation, local active-session cache, compact media use, and Wear Data Layer transport |

Required presentation flow:

```text
Route + Screen + ViewModel + UiState + Action
    -> UseCase / Reducer
    -> WorkoutRepository
    -> Room transaction + mutation outbox
```

Compose screens render state and emit actions. They do not parse the exercise catalog, read Profile storage, select gender media, call repositories, or mutate session state directly.

## 90-Day Delivery Plan

Every stage is a gate. Do not expand the next broad surface until the previous stage's exit criteria pass.

### Stage 0, Days 1-5: Freeze product behavior and provenance

Build first:

- Lyfta-derived capability and UX flow matrix;
- keep, adapt, defer, and skip decisions;
- Tio-owned screen inventory and route map;
- exercise identity and provenance policy;
- male, female, and neutral media coverage matrix;
- explicit privacy and offline rules.

Exit when each retained flow has an owner, state list, error/recovery behavior, and acceptance criteria, and no third-party asset is assumed releasable.

### Stage 1, Days 6-12: Shared Workout contract v2

Build:

- stable IDs and versioned models;
- exercise definitions and media variants;
- routine, session, exercise-entry, set, timer, and mutation contracts;
- reducer/state-machine transitions;
- `ExerciseMediaResolver` and preference mapping;
- serialization and reducer unit tests.

Exit when Phone and Wear can consume the same pure Kotlin contracts and invalid transitions are covered by tests.

### Stage 2, Days 13-20: Phone Room and recovery boundary

Build:

- Room entities, DAOs, migrations, and transactions;
- repository implementation;
- active-session snapshot and transactional mutation outbox;
- crash/process-death recovery;
- idempotency and ordering tests.

Exit when a session and its last completed set survive process death without duplication or data loss.

### Stage 3, Days 21-25: First thin offline vertical slice

Build only:

```text
Workout placeholder
    -> Start blank workout
    -> Add one exercise
    -> Complete one set
    -> Finish
    -> Open history
    -> Restart app and verify recovery
```

Use Tio visual components and the approved behavioral flow. Exit when the slice works offline and has UI, reducer, repository, and persistence tests appropriate to the risk.

### Stage 4, Days 26-35: Exercise library and media

Build:

- catalog ingestion behind a repository;
- browse, search, filter, detail, and selection UX;
- `AUTO / MALE / FEMALE / NEUTRAL` Workout setting;
- exact, neutral, image, and placeholder fallbacks;
- approved caching and media error states;
- provenance enforcement and forbidden-host checks.

Exit when every exercise has a stable ID, text-only usable detail, deterministic media resolution, and no unapproved production dependency.

### Stage 5, Days 36-45: Workout setup and routine builder

Build:

- blank and routine entry paths;
- add, remove, reorder, replace, and configure exercise behavior;
- sets, reps, rest, set type, notes, save, duplicate, discard, and draft recovery;
- routine list and detail.

Exit when a routine has one durable source of truth and every edit survives recreation without duplicate exercises.

### Stage 6, Days 46-58: Full active workout engine

Build:

- previous values and set editing;
- warm-up, normal, drop, failure, and superset behavior;
- rest timer controls and notification/vibration boundary;
- exercise skip/replace/reorder during a session;
- elapsed time, notes, finish/discard, and recovery;
- full-screen Phone UX and accessibility.

Exit when state transitions are reducer-tested and interruption, backgrounding, rotation, process death, and repeated actions cannot corrupt a session.

### Stage 7, Days 59-66: Finish, history, and basic metrics

Build:

- idempotent finish transaction;
- summary, history list, history detail, and permitted correction flow;
- duration, volume, completed sets, and previous-performance derivation;
- empty, loading, error, and large-history states.

Exit when finish produces exactly one durable session and all displayed metrics derive from persisted records.

### Stage 8, Days 67-73: Plan, schedule, and reminders

Build:

- plan and routine assignment;
- scheduled workout occurrences;
- today/upcoming state;
- optional reminders and reschedule/skip behavior.

Exit when scheduling references stable routine IDs, handles timezone/date changes, and never duplicates routine truth.

### Stage 9, Days 74-82: Conditional Wear MVP

Start only after Phone engine, contract, persistence, and reconciliation gates pass.

Build:

- routine/session transfer from Phone;
- local active-session cache;
- offline set logging and rest timer;
- identified mutation acknowledgement, retry, ordering, and conflict handling;
- selected compact exercise thumbnail variant.

Exit when disconnect/reconnect, duplicate delivery, out-of-order delivery, Phone restart, and Watch restart tests pass. Simulated sync copy must be removed before claiming real sync.

### Stage 10, Days 83-90: Hardening and scoped release decision

Validate:

- Room migrations and backup/restore behavior;
- offline/reconnect and process-death matrix;
- performance with the full approved catalog and long history;
- accessibility, reduced motion, captions, and text-only exercise use;
- gender-media preference, fallback, cache, privacy, and Watch matrix;
- provenance, licence, forbidden-host, secret, and generated-artifact scans;
- analytics events without sensitive gender leakage.

Exit with an explicit `PASS`, `REVIEW`, or `BLOCKED` result. Cloud sync stays feature-flagged off unless a backend-mediated production path exists.

## Acceptance Criteria

The Workout blueprint is satisfied only when:

- Tio uses its own design system while retained Lyfta task flows meet the approved UX behavior matrix.
- Every production screen has loading, empty, error, offline, retry, and recovery behavior where applicable.
- One exercise keeps one `exerciseId` across male, female, neutral, Phone, Wear, routine, session, and history use.
- Changing media preference changes presentation only and never duplicates or rewrites workout data.
- `AUTO`, explicit override, Profile `Other`/blank, exact match, neutral fallback, corrupt video, offline cache, and placeholder paths have tests.
- Missing male media does not silently show female media, and missing female media does not silently show male media.
- Text instructions remain usable without image or video.
- Every shipped media asset has approved provenance and licence evidence.
- Phone session state survives process death and finish is idempotent.
- Wear mutation delivery is durable, ordered, acknowledged, retryable, and reconciled.
- No client receives service-role keys, admin credentials, private tokens, or unrestricted media administration capability.

## Deferred Beyond The 90-Day Core

- trainer marketplace and coaching commerce;
- AI-generated workouts or generated exercise media;
- subscription, advertising, and monetization surfaces;
- advanced recovery and readiness programming;
- third-party gym, health, or wearable integrations beyond the approved Wear MVP;
- gender-based biomechanics or programming claims without product, clinical, and content review.

## Explicitly Excluded Product Scope

Lyfta Community is a product `SKIP` for Tio. Do not create Community routes, tabs, feeds, posts, comments, follows, friends, groups, social profiles, community challenges, or leaderboards as part of Workout or a later Workout stage unless the product owner explicitly reverses this decision.

## Related Documents

- [Android App Progress](ANDROID_APP_PROGRESS.md)
- [Architecture](ARCHITECTURE.md)
- [Profile And Settings Ownership](PROFILE_SETTINGS_GUIDE.md)
- [Supabase Incremental Setup Plan](SUPABASE_INCREMENTAL_SETUP_PLAN.md)
- [Wear OS Plan](WEAR_OS_PLAN.md)
- [Wear OS Progress](WEAR_OS_PROGRESS.md)
- [Documentation Index](README.md)
