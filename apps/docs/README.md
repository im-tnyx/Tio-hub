# TNYX Android Documentation

This folder is the canonical documentation home for the TNYX Android and Wear OS app under `apps/`.

The goal of these docs is simple:

- keep architecture decisions visible,
- keep feature ownership clear,
- avoid hardcoded source-of-truth data,
- make future 100+ screen development safer,
- and help contributors understand what is implemented, what is planned, and what is only a skeleton.

## Truth Boundary

When sources disagree, use this order:

1. Runtime source/config wins for actual behavior.
2. Product and ownership docs win for feature ownership and product status.
3. Platform-local docs win for Android/Wear implementation details.
4. `.ai/` is only a short orientation layer, not a canonical replacement.

## Start Here

Use this path for a new contributor, AI assistant, or future maintainer:

1. [Root README](../../README.md)
2. [Apps README](../README.md)
3. [Android App Progress](ANDROID_APP_PROGRESS.md)
4. [Architecture](ARCHITECTURE.md)
5. [Navigation Guide](NAVIGATION_GUIDE.md)
6. [Profile / Settings Guide](PROFILE_SETTINGS_GUIDE.md)
7. [Workout Product And UX Blueprint](WORKOUT_PRODUCT_BLUEPRINT.md)
8. [Engineering Guidelines](ENGINEERING_GUIDELINES.md)
9. [Definition Of Done](DEFINITION_OF_DONE.md)

## Documentation Map

| Document | Owner / Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Canonical Android module architecture, shell rules, design-system ownership, feature folder pattern, and long-term modular shape |
| [NAVIGATION_GUIDE.md](NAVIGATION_GUIDE.md) | Type-safe navigation, root/main/modal graph policy, nested feature graphs, deep-link direction, and chrome policy |
| [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md) | Canonical ownership reference for Profile, Settings, Progress, Subscription entry, Personal Information, Health, Recovery, Resources, and Rewards |
| [WORKOUT_PRODUCT_BLUEPRINT.md](WORKOUT_PRODUCT_BLUEPRINT.md) | Planned Workout product and UX target, Lyfta reference boundary, gender-aware exercise media contract, architecture gates, and 90-day delivery order |
| [SUPABASE_INCREMENTAL_SETUP_PLAN.md](SUPABASE_INCREMENTAL_SETUP_PLAN.md) | Plan for replacing hardcoded data with repository-backed Supabase slices, RLS, seed data, and future TypeScript/Turborepo boundaries |
| [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md) | Current Android implementation status: completed foundation, skeletons, placeholders, known validation, and next recommended slices |
| [NUTRITION.md](NUTRITION.md) | Nutrition runtime behavior, Meal Diary/Editor flow, UI/data boundary, hardcoded-data status, and persistence roadmap |
| [ONBOARDING_FLOW_DETAILED.md](ONBOARDING_FLOW_DETAILED.md) | Full onboarding reference, section order, data collection, resume expectations, and future integration direction |
| [TNYX_MODULAR_ONBOARDING.md](TNYX_MODULAR_ONBOARDING.md) | Modular onboarding implementation guide and migration direction |
| [WEAR_OS_PLAN.md](WEAR_OS_PLAN.md) | Wear OS product and architecture plan for workout/nutrition companion workflows |
| [WEAR_OS_PROGRESS.md](WEAR_OS_PROGRESS.md) | Wear OS implementation progress tracking |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | Testing strategy and expectations by risk level |
| [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md) | Production engineering rules, module boundaries, UI rules, data/security rules, and review expectations |
| [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md) | Merge-readiness checklist for feature, UI, navigation, data, docs, and security work |
| [ARCHITECTURE_CHANGELOG.md](ARCHITECTURE_CHANGELOG.md) | Durable architecture change log for module boundaries, navigation policy, data flow, and engineering practice changes |
| [adr/README.md](adr/README.md) | Architecture Decision Records index |

## ADRs

Architecture Decision Records are used for durable decisions that should not be rediscovered during implementation.

Current ADRs:

- [ADR-0001: Android Modular Clean Architecture](adr/0001-android-modular-clean-architecture.md)
- [ADR-0002: Profile Settings Progress Ownership](adr/0002-profile-settings-progress-ownership.md)
- [ADR-0003: Type-Safe Navigation And Chrome Policy](adr/0003-type-safe-navigation-and-chrome-policy.md)
- [ADR-0004: Incremental Supabase Setup](adr/0004-incremental-supabase-setup.md)

Add or update an ADR when a change affects:

- module boundaries,
- feature ownership,
- navigation architecture,
- data ownership,
- Supabase/RLS strategy,
- shared contracts,
- Wear OS reuse,
- or long-term engineering workflow.

## Current Architecture Snapshot

The Android Gradle root is `apps/`.

Current checked-in modules:

```text
apps/
|-- app/                 # Android phone entry point, root navigation, app shell wiring
|-- core/                # Design system, reusable UI, route contracts, app shell primitives
|-- shared/              # Pure Kotlin domain-safe contracts and models for phone/watch reuse
|-- wear/                # Wear OS companion app foundation
`-- features/
    |-- auth/            # Auth graph foundation; real UI still needed
    |-- onboarding/      # Splash/welcome/onboarding foundation
    |-- workout/         # Workout graph and secondary-nav areas
    |-- nutrition/       # Nutrition diary/editor screens; persistence still incremental
    |-- profile/         # Fitness Hub + Account Launcher skeleton
    |-- settings/        # App/account configuration skeleton
    `-- progress/        # Progress bottom-tab skeleton
```

Do not create future modules just because they are listed in roadmap docs.

Future modules should be created only when runtime code needs them:

- `:features:health`
- `:features:recovery`
- `:features:billing`
- `:features:rewards`
- `:features:learn`
- `:features:coach`

## Ownership Freeze

These rules are currently frozen:

- Profile is not a business domain.
- Profile is a Fitness Hub + Account Launcher.
- Progress owns Journey, Progress Photos, Measurements, Weight, Achievements, and Progress Analytics.
- Nutrition owns Nutrition Targets, Calories, Macros, Water Goal, and Glass Size.
- Workout owns Workout Settings, Rest Timer, Plate Calculator, Graph Settings, RPE, and Previous Workout Values.
- Health is future ownership for Health Connections, Samsung Health, Health Connect, Garmin, Fitbit, and Apple Health.
- Recovery is future ownership for Sleep, HRV, Readiness, and Recovery Score.
- Subscription business logic belongs to future Billing / Entitlement ownership.
- Settings is the primary UI entry for Subscription.
- Profile may show a current plan shortcut only.
- Personal Information belongs to Profile domain and must use one repository across Onboarding, Profile, and Settings.

For details, use [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md).

## Navigation Freeze

Root graph direction:

```text
RootGraph
|-- AuthGraph
|-- OnboardingGraph
|-- MainGraph
`-- ModalGraph
```

Main Graph tabs:

```text
Home
Workout
Nutrition
Coach
Progress
```

Profile opens from the avatar.

Settings opens from the gear icon.

Cross-feature navigation must happen through public route contracts.

Every destination declares one chrome policy:

- `MainChrome`
- `NoBottomBar`
- `FullScreen`
- `BottomSheet`
- `Dialog`

For details, use [NAVIGATION_GUIDE.md](NAVIGATION_GUIDE.md).

## Supabase And Hardcoded Data Policy

Supabase should be introduced incrementally.

Do not create the full database upfront.

Recommended feature-slice flow:

1. Build the smallest useful UI slice.
2. Identify the real data shape.
3. Create the minimum table or RPC.
4. Add RLS.
5. Add demo seed data.
6. Connect through repositories.
7. Test the feature end to end.

Hardcoded data is allowed only as temporary scaffolding.

Do not let hardcoded sample data become the source of truth.

For details, use [SUPABASE_INCREMENTAL_SETUP_PLAN.md](SUPABASE_INCREMENTAL_SETUP_PLAN.md).

## Reading Paths By Task

### If You Are Adding A New Screen

Read:

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [NAVIGATION_GUIDE.md](NAVIGATION_GUIDE.md)
- [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md)
- [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)

Confirm:

- UI owner
- navigation owner
- business logic owner
- repository owner
- chrome policy
- loading/empty/error behavior

### If You Are Changing Profile, Settings, Or Progress

Read:

- [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md)
- [NAVIGATION_GUIDE.md](NAVIGATION_GUIDE.md)
- [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md)

Hard rule:

- Profile launches other feature areas.
- Profile must not own their business logic.

### If You Are Removing Hardcoded Data

Read:

- [SUPABASE_INCREMENTAL_SETUP_PLAN.md](SUPABASE_INCREMENTAL_SETUP_PLAN.md)
- the feature-specific doc, such as [NUTRITION.md](NUTRITION.md)
- [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md)

Confirm:

- repository contract,
- demo seed strategy,
- RLS ownership,
- signed-in/signed-out behavior,
- and whether the table is truly needed now.

### If You Are Working On Onboarding

Read:

- [ONBOARDING_FLOW_DETAILED.md](ONBOARDING_FLOW_DETAILED.md)
- [TNYX_MODULAR_ONBOARDING.md](TNYX_MODULAR_ONBOARDING.md)
- [PROFILE_SETTINGS_GUIDE.md](PROFILE_SETTINGS_GUIDE.md)
- [SUPABASE_INCREMENTAL_SETUP_PLAN.md](SUPABASE_INCREMENTAL_SETUP_PLAN.md)

Keep onboarding minimal until a slice needs full runtime logic.

Onboarding should collect data, but ownership belongs to the owning domains:

- Personal Information -> Profile
- Nutrition Targets -> Nutrition
- Workout Preferences -> Workout
- Health Connections -> Health
- Sleep/HRV/Readiness -> Recovery

### If You Are Working On Wear OS

Read:

- [WEAR_OS_PLAN.md](WEAR_OS_PLAN.md)
- [WEAR_OS_PROGRESS.md](WEAR_OS_PROGRESS.md)
- [ARCHITECTURE.md](ARCHITECTURE.md)

Do not claim real sync, sensors, tiles, complications, notifications, or workout runtime unless the source proves it.

### If You Are Working On Workout

Read:

- [WORKOUT_PRODUCT_BLUEPRINT.md](WORKOUT_PRODUCT_BLUEPRINT.md)
- [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md)
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [WEAR_OS_PROGRESS.md](WEAR_OS_PROGRESS.md) when the change touches Watch

Keep Lyfta as a behavior and UX reference only. Tio owns the visual UI, architecture, data, media provenance, and release boundary. Treat Tnyx Flutter as a screen comparison only.

### If You Are Opening A PR

Read:

- [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md)
- [DEFINITION_OF_DONE.md](DEFINITION_OF_DONE.md)
- [.github/PULL_REQUEST_TEMPLATE.md](../../.github/PULL_REQUEST_TEMPLATE.md)

Include validation commands actually run.

## Documentation Update Rules

Update docs when a change affects:

- architecture,
- module ownership,
- navigation behavior,
- chrome policy,
- persistence,
- Supabase/RLS,
- hardcoded data replacement,
- implementation status,
- or long-term roadmap boundaries.

Update [ANDROID_APP_PROGRESS.md](ANDROID_APP_PROGRESS.md) when implementation status changes.

Update [ARCHITECTURE_CHANGELOG.md](ARCHITECTURE_CHANGELOG.md) when project structure, data flow, module boundaries, navigation policy, or engineering practice changes.

Add or update an ADR when a decision should remain binding across future work.

## Current Recommended Next Implementation Order

The current practical order is:

1. Keep this docs foundation clean and committed.
2. Add minimum functional Auth screens so `onAuthSuccess` is reachable.
3. Move Nutrition hardcoded read paths behind repository boundaries.
4. Add Supabase tables only when the data shape is known.
5. Wire Profile avatar and Settings gear graph entries.
6. Expand Progress screens one slice at a time.

Do not build full onboarding, full Supabase schema, or future modules before the vertical slice needs them.
