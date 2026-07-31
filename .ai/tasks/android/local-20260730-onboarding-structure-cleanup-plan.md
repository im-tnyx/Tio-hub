# Android Onboarding Structure Cleanup Plan

Status: In Progress
Updated: 2026-07-30

## Objective

Define the final onboarding file/setup shape so Tio-hub can keep a smaller
basic UI for now, but upgrade later without another structural rewrite.

## Why This Plan Exists

- Current onboarding logic is usable, but the screen layer still feels
  scaffolded.
- The current `OnboardingScreen` owns too much section switching.
- Section files still hide many steps behind `when(stepId)` branching.
- The current `OnboardingViewModel` owns orchestration, validation,
  persistence, completion, and profile finalization together.
- More sections such as `mobile`, `targets`, or `source` should not be added
  onto the current generic container shape.

## Confirmed Current Truth

- Stable flow IDs and deterministic ordering are already implemented.
- Local checkpoint persistence, resume behavior, and completion retry are
  already implemented.
- The current delivered runtime flow is `intro -> profile -> body_goal ->
  mobile -> workout_intro -> workout -> targets -> source -> review`.
- `intro` is intentionally a single welcome screen right now; the structure
  leaves room for a later multi-screen intro expansion without changing the
  section boundary.
- `mobile` is now a dedicated local section instead of being mixed into
  `profile`, but OTP verification is intentionally deferred.
- Completed onboarding now finalizes Profile-owned local fields and marks the
  active local profile as onboarded.
- Signed-out users with a completed local profile now skip Welcome on cold
  start.
- Checkpoint preparation can now seed existing Profile data into the draft and
  align hidden intro/mobile positions for signed-in or prefilled users without
  adding more intro screens or typing-only source steps.

## Current Checkpoint

- Centralized checkpoint application, initialization-failure handling, and
  completion effect emission inside `OnboardingViewModel`, so remaining glue is
  less repetitive even where it still stays presentation-owned.
- Moved checkpoint save/error/saving UI-state toggles behind
  `OnboardingCheckpointUiStateFactory` so `OnboardingViewModel` no longer
  repeats raw `isSaving` / `hasPersistenceError` render booleans across flows.
- Moved continue-path advance/persist/complete orchestration into
  `ContinueOnboardingSessionUseCase` so `OnboardingViewModel` no longer owns
  that post-validation branching directly.
- Moved retry resolve/persist/complete orchestration into
  `RetryOnboardingSessionUseCase` so `OnboardingViewModel` no longer owns that
  retry branching directly.
- Moved ViewModel initialization read/resolve/persist orchestration into
  `InitializeOnboardingSessionUseCase` so `OnboardingViewModel` no longer owns
  that startup branching directly.
- Expanded `source` from a single discovery channel step into a two-step local
  section by adding a stable `source.reason` step before `review`.
- Added a lightweight `intro` section with a single welcome step before
  `profile` so future multi-screen intro expansion can stay inside the same
  structural boundary.
- Added a dedicated `mobile` section with a local mobile capture step between
  `body_goal` and `workout_intro`, and wired that value into local profile
  finalization.
- Added a dedicated `health_condition` step at the end of `body_goal` so the
  local baseline now captures health context before `mobile` and workout setup.
- Expanded `workout` by adding a dedicated `focus_areas` step between
  `location` and `equipment`, so the local flow now captures training intent
  before optional equipment detail.
- Expanded `workout` again by adding a dedicated `workout_split` step after
  `duration`, so the local flow now captures split preference before leaving
  workout setup.
- Expanded `workout` again by adding an optional
  `workout_health_concerns` text step after `workout_split`, so the local flow
  can capture injuries, movement limits, or recovery notes without forcing
  extra friction.
- Expanded `workout` again by adding an optional `special_event_goal` text
  step after `workout_health_concerns`, so the local flow can capture event or
  deadline-based planning context without forcing setup friction.
- Expanded `targets` with stable sleep-target and nutrition-summary steps so
  the local flow now covers movement, recovery, hydration, pace, and a simple
  nutrition baseline before `source`.
- Expanded `targets` again by adding a required
  `targets.recommendation_summary` bridge step after `water_target`, so the
  user now sees a simple explanation of the seeded steps, sleep, and water
  defaults before selecting goal pace.
- Added a `targets` section between `workout` and `source` with first-pass
  steps target, water target, and goal pace steps.
- Added target validation and review summary mapping so the new section is part
  of the local flow rather than standalone UI.
- Added the first future additive section by inserting `source` between
  `workout` and `review`.
- Added `presentation/sections/source/` with a dedicated source channel step.
- Added source validation and review summary mapping so the new section is part
  of the current local flow rather than dead scaffolding.
- Expanded `source` again by adding an optional
  `source.referral_detail` step after `source.reason`, so referral, coach, or
  discovery context no longer needs to be overloaded into one generic reason
  answer.
- Added dedicated post-review `completion` UI states so successful local
  onboarding no longer exits directly from `review`; the user now sees a short
  setup state followed by a ready/congrats state before entering the app.
- Root onboarding presentation no longer renders section-specific content from
  one large monolithic screen file.
- Added `presentation/shell/` with dedicated shell, progress, footer, and
  error-state files.
- Added `presentation/sections/` so section ownership now starts below the
  shell instead of inside the root screen.
- Split `profile` into `ProfileSectionContent` plus dedicated `steps/`
  composables for name, gender, and date of birth.
- Split `body_goal` into `BodyGoalSectionContent` plus dedicated `steps/`
  composables for primary goal, height, current weight, target weight, and
  activity level.
- Split `workout` into `WorkoutSectionContent` plus dedicated `steps/`
  composables for experience, gym access, location, focus areas, equipment,
  training days, duration, split, workout health concerns, and special event
  goal.
- Split `review` into `ReviewSectionContent` plus `steps/SummaryStep.kt`.
- Moved review summary display mapping into
  `domain/usecase/BuildReviewSummaryUseCase.kt`.
- Moved step validation and profile finalization rules into dedicated domain
  use cases so the ViewModel no longer owns those raw rule tables.
- Moved onboarding progression rules for continue, back, and skip into
  dedicated flow use cases.
- Moved checkpoint save and completion persistence paths into dedicated
  use cases so the ViewModel no longer owns direct try/catch persistence flow.
- Moved initialization branching into a dedicated use case and moved UI-state
  assembly into a dedicated presentation factory.
- Moved retry branching and answer draft mutation into dedicated use cases.
- Added checkpoint-preparation bootstrap use cases so current Profile answers
  can prefill onboarding draft state before recommendation seeding, route
  context sync, and visible-flow alignment.
- Removed the old root placeholder-style section fallback copy from the main
  onboarding screen.

## Target Architecture

The onboarding feature should settle into this shape before more breadth is
added:

```text
presentation/
  OnboardingRoute.kt
  OnboardingViewModel.kt
  OnboardingContract.kt
  shell/
    OnboardingShellScreen.kt
    OnboardingProgressBar.kt
    OnboardingFooter.kt
    OnboardingErrorState.kt
  sections/
    intro/
      IntroSectionContent.kt
      steps/
        IntroWelcomeStep.kt
    profile/
      ProfileSectionContent.kt
      steps/
        NameStep.kt
        GenderStep.kt
        DateOfBirthStep.kt
    bodygoal/
      BodyGoalSectionContent.kt
      steps/
        PrimaryGoalStep.kt
        HeightStep.kt
        CurrentWeightStep.kt
        TargetWeightStep.kt
        ActivityLevelStep.kt
    mobile/
      MobileSectionContent.kt
      steps/
        MobileNumberStep.kt
    workout/
      WorkoutSectionContent.kt
      steps/
        ExperienceStep.kt
        GymAccessStep.kt
        LocationStep.kt
        FocusAreasStep.kt
        EquipmentStep.kt
        TrainingDaysStep.kt
        DurationStep.kt
        WorkoutSplitStep.kt
        WorkoutHealthConcernsStep.kt
        WorkoutSpecialEventStep.kt
    review/
      ReviewSectionContent.kt
      steps/
        SummaryStep.kt
  common/
    OnboardingStepScaffold.kt
    OnboardingChoiceCard.kt
    OnboardingStepHeading.kt
    OnboardingValidationMessage.kt

domain/
  flow/
  model/
  repository/
  usecase/
    ValidateOnboardingStepUseCase.kt
    AdvanceOnboardingUseCase.kt
    FinalizeOnboardingUseCase.kt
    BuildProfileFromDraftUseCase.kt
```

## Rules For The Cleanup

1. Keep current stable `sectionId` and `stepId` values unchanged.
2. Keep current screen count effectively small; step files may stay visually
   basic.
3. The shell must not own feature-specific `when(sectionId)` business layout.
4. A section file may coordinate its own steps, but step UIs must move out of
   large section-level `when(stepId)` blocks.
5. Validation, next/back advancement, and profile finalization must not remain
   mixed directly inside one large ViewModel forever.
6. Review display mapping should come from a reusable mapper/use case rather
   than manual duplicated label logic inside the screen.

## Execution Phases

### Phase 1: Shell Extraction

- Replace the current `OnboardingScreen.kt` monolith with a pure shell.
- The shell should own:
  - header
  - section/step counters
  - progress bar
  - loading/error state
  - footer CTA area
  - scroll container slot
- The shell must not know Profile, Body Goal, Workout, or Review specifics.

### Phase 2: Section Coordinators

- Create one section coordinator file per current section.
- Each section coordinator receives:
  - current `stepId`
  - current answer
  - validation state
  - answer callback
- Each section coordinator decides which step composable to show.
- This keeps future sections additive instead of bloating one root screen.

### Phase 3: Dedicated Step Files

- Move each current step into its own file under `steps/`.
- Keep UI basic for now.
- The point is ownership clarity, not visual richness.
- After this split, future UI upgrades should happen mostly inside step files.

### Phase 4: ViewModel Load Reduction

- Keep `OnboardingViewModel` as the orchestration owner for now, but move:
  - step validation
  - advancement rules
  - final profile build/finalization
  into explicit domain use cases.
- The ViewModel should assemble state and call use cases, not contain every
  rule inline.

### Phase 5: Review Mapper Cleanup

- Extract answer-to-display mapping from `ReviewStepContent`.
- Build one reusable mapper/use case for summary rows.
- This prevents label drift when more sections are added later.

### Phase 6: Future Additive Sections

- Only after the cleanup is done should future sections be added, such as:
  - `mobile`
  - `targets`
  - `source`
  - `nutrition`
- Those sections should plug into the same shell + section + step structure.

## Reference Gap Roadmap

The current Tio-hub onboarding should stay visually basic for now, but missing
reference breadth should be added in this order so later upgrades remain
structural rather than ad-hoc. The goal is not raw parity with Tnyx-hub UI;
the goal is a cleaner, more maintainable Tio onboarding that can exceed the
older setup without another rewrite.

1. `intro`
   - Keep the current single welcome step for now.
   - Do not add the remaining intro screens yet.
   - Later expand inside the same `intro` section to a richer multi-screen
     product/setup flow when the copy and motion direction are ready.
2. `profile` / `body_goal`
   - Keep current local basics.
   - Decide whether Tio should continue with `date_of_birth` or add a derived
     age-focused presentation layer without changing the stored truth.
3. `mobile`
   - Keep the current local mobile capture step.
   - Later split this into:
     - mobile capture
     - OTP verification / verified state
   - Keep verification as a future backend/auth slice rather than forcing it
     into the current local-only onboarding flow.
4. `workout`
   - Keep explicit `gym_access` before the location/equipment shape.
   - Keep `equipment` conditional when the selected setup makes it unnecessary.
   - Keep workout breadth inside dedicated steps:
     - `focus_areas`
     - `workout_split`
     - `workout_health_concerns`
     - `special_event_goal`
   - Keep `workout_intro` as the gate that can still remove the whole workout
     branch from the effective progress path.
5. `targets`
   - Keep auto-seeded defaults for steps, sleep, and water.
   - Keep the new targets bridge/summary step so the user sees how the seeded
     defaults were chosen before goal pace.
   - Keep richer nutrition framing inside the same section boundary.
6. `source`
   - Keep the current discovery channel + primary reason baseline.
   - Keep the optional referral-detail step for invite, coach, or discovery
     context instead of overloading `source.reason`.
7. `completion`
   - Keep the dedicated setup/progress screen after review submission.
   - Keep the dedicated congrats/ready screen after setup completes.
   - Keep these as separate post-review states rather than stuffing them into
     review copy.
8. `polish widgets`
   - Plan backlog items inspired by the older flow, but implemented only if
     they improve Tio rather than copying complexity:
     - stronger resume banner
     - validation summary
     - Health Connect setup prompt
     - Watch connection prompt
     - plan preview after finalization
     - target adjustment preview
     - referral status widget
     - failed finalization recovery
9. `future orchestration parity with Tnyx`
   - `StateMachine`, `ResumeManager`, auth-context resume, remote resume, and
     local-draft migration should stay as a separate future roadmap item.
   - These are not mandatory for the current Tio onboarding expansion because
     Tio currently uses a lighter checkpoint + use-case architecture.
   - Only add this layer if the current lightweight flow stops being clear or
     maintainable.
7. `progress shell`
   - Keep the current single top-row progress bar.
   - The rendered progress denominator must follow the effective onboarding
     path, so declining `workout_intro` removes `workout` steps from visible
     progress instead of keeping a static full-flow count.

## What Should Not Change During This Cleanup

- No backend wiring.
- No Supabase synchronization changes.
- No guest-to-auth migration behavior changes in this cleanup slice.
- No section ID renames.
- No step ID renames.
- No rewrite of the local checkpoint contracts.

## Recommended First Implementation Slice

If work starts from this plan, the best first code slice is:

1. Create `presentation/shell/OnboardingShellScreen.kt`
2. Create `presentation/sections/*SectionContent.kt`
3. Move only one section first:
   - `profile`
4. Leave the other current sections temporarily bridged until the pattern is
   proven.

This keeps the cleanup vertical and low-risk.

This first slice is now implemented.

## Next

1. Continue trimming `OnboardingViewModel` orchestration only if initialization
   completion or generic operation-launch/error shell paths still feel heavy
   enough to justify another extraction.
2. Keep workout-intro parity and dynamic progress behavior stable alongside the
   new completion states.
3. Revisit whether completion states should later move behind a dedicated
   orchestration/use-case boundary once backend handoff exists.

## Success Criteria

- The root shell no longer contains feature-specific placeholder/fallback copy.
- Section-specific rendering no longer lives in one large root `when`.
- At least one section proves the final shell + section + step structure.
- Future onboarding breadth can be added by creating new section and step files
  without redesigning the root architecture again.

## Validation Expectation

When implementation begins, keep the existing focused Android gate:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:testDebugUnitTest :app:compileDebugKotlin --no-configuration-cache`

Latest run for the first structure slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the body goal structure slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the workout structure slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the review/use-case cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the source section slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the targets section slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the targets expansion slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the source expansion slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the initialization cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the retry cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the continue cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the checkpoint UI-state cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the checkpoint-application cleanup slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the targets/source bridge and Google auth foundation slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :features:auth:testDebugUnitTest :app:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the completion states slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache`
- Result: BUILD SUCCESSFUL

Latest run for the profile-seeded bootstrap slice:

- `./gradlew.bat :features:onboarding:testDebugUnitTest
  :app:compileDebugKotlin --no-configuration-cache --no-daemon
  -Dkotlin.compiler.execution.strategy=in-process`
- Result: BUILD SUCCESSFUL

## Truth Boundary

- This file is a structure plan only.
- It does not mean the cleanup is implemented yet.
- Runtime source remains the behavior truth until code changes land.
