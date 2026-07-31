# Android Onboarding Post-Completion Parity Roadmap

Status: Planned
Updated: 2026-07-30
Execution: On Hold until explicit user confirmation after overall app completion

## Objective

Define the future implementation plan for the onboarding gaps that are
intentionally still lighter than the older Tnyx reference:

- backend / Firebase / remote-config parity,
- guest-to-auth full handoff,
- production analytics sink.

Comment:
This roadmap is intentionally future-only. It must not be started during the
current buildout phase unless the user explicitly re-activates it.

## Why This Stays Deferred

- Current Tio onboarding architecture is now structurally aligned enough for
  active feature work.
- App-wide core flows, feature coverage, and runtime truth should stabilize
  before adding auth migration, remote orchestration, or production analytics.
- Starting this too early would couple onboarding to backend assumptions that
  may still change while the app is incomplete.

Comment:
Priority abhi feature completion aur stable runtime truth hai, parity
orchestration nahi.

## Current Truth Snapshot

Already implemented in Tio:

- `OnboardingContainer` shell,
- `OnboardingTopBar` / `OnboardingBottomBar`,
- `SectionRenderer`,
- `OnboardingStateMachine`,
- `BuildFlowUseCase`,
- `RestoreFlowUseCase`,
- explicit `ResumeManager` contract + app implementation,
- `StepValidator`,
- local checkpoint persistence,
- local profile finalization,
- route-context aware flow shaping,
- analytics event contract + placeholder logger.

Still intentionally incomplete:

- no backend-owned onboarding finalization path,
- no Firebase-auth-based guest-to-account migration,
- no remote resume / remote draft merge,
- no production analytics destination,
- no remote config driven onboarding variation.

## Activation Gate

This roadmap should begin only when all of the following are true:

1. Main Android app feature scope is considered functionally complete enough by
   the user.
2. Current local onboarding flow is stable and no longer changing weekly.
3. Profile, workout, and nutrition ownership boundaries are final enough to
   map to backend truth.
4. Auth direction is confirmed:
   - `Firebase auth + backend`
   - `Supabase auth + backend`
   - `backend-only auth abstraction`
5. The user explicitly says to start this roadmap.

Comment:
Is gate ke bina is plan ko execute nahi karna hai.

## Future Scope

### Track A: Backend / Firebase / Remote Config Parity

Goal:
Move onboarding from local-only orchestration to a real app lifecycle where
remote truth can participate without breaking the current feature-owned
architecture.

Expected outcomes:

- onboarding initialization can read remote bootstrap context,
- per-section or final completion can sync to backend-owned truth,
- remote config can shape optional onboarding branches,
- local checkpoint remains a resilience layer instead of the only truth.

Not in scope for this track:

- changing section IDs,
- changing step IDs,
- rewriting the presentation shell,
- turning onboarding into a mega-repository owner.

### Track B: Guest-to-Auth Full Handoff

Goal:
Allow a guest user to start onboarding locally and safely continue or merge
that work after account creation/sign-in.

Expected outcomes:

- guest checkpoint survives until auth success,
- auth success triggers controlled merge logic,
- local guest answers migrate into the authenticated account path,
- local guest snapshots are cleaned after successful migration.

### Track C: Production Analytics Sink

Goal:
Keep the current analytics event contract but replace the placeholder logger
with a real sink.

Expected outcomes:

- event schema remains stable,
- tracker stays feature-owned,
- logger implementation becomes platform-owned,
- analytics can be switched without touching screens.

## Recommended Delivery Order

1. Auth direction confirmation
2. Guest-to-auth handoff
3. Backend-owned profile/workout/nutrition sync path
4. Production analytics sink
5. Remote config path

Comment:
Remote config sabse last me isliye, kyuki bina stable flow ke remote-config
 sirf extra complexity degi.

## Detailed Plan

## Phase 0: Preconditions And Contracts

Before implementation:

- confirm auth provider direction,
- confirm whether the Android app will talk to backend directly or through a
  shared API client abstraction,
- confirm the authoritative remote data owners for:
  - profile basics,
  - workout profile,
  - nutrition profile,
  - onboarding completion state,
  - referral/source context.

Files likely involved:

- `apps/shared/...` only if cross-platform contracts become truly shared,
- `apps/app/src/main/java/com/tnyx/di/...`,
- `apps/features/onboarding/domain/...`,
- backend/API contract documentation when available.

Exit criteria:

- no ambiguity remains about auth and remote truth ownership.

## Phase 1: Guest Draft Handoff Contract

Implement:

- explicit guest-session vs authenticated-session onboarding path model,
- merge policy for checkpoint + draft answers,
- post-auth continuation trigger,
- cleanup policy after successful migration.

Suggested artifacts:

- `OnboardingAccountHandoffUseCase.kt`
- `OnboardingDraftMergePolicy.kt`
- `OnboardingSessionState.kt`

Behavior rules to define:

- if remote profile is empty and local draft is richer, local wins,
- if remote profile already has verified fields, verified remote wins,
- conflicting mutable fields should use explicit precedence rules,
- merge should be idempotent.

Comment:
Ye sabse risky phase hai. Isme silent overwrite allowed nahi hona chahiye.

Exit criteria:

- guest onboarding can continue safely after sign-in without losing data.

## Phase 2: Remote Bootstrap And Finalization

Implement:

- remote bootstrap read during onboarding init,
- remote-aware checkpoint preparation,
- remote finalization path for onboarding completion,
- section-level or final sync rules.

Suggested new pieces:

- `RemoteOnboardingRepository`
- `OnboardingBootstrapDataSource`
- `FinalizeRemoteOnboardingUseCase`
- remote DTO mappers for profile/workout/nutrition payloads

Important rule:

- presentation layer must remain unchanged in responsibility,
- onboarding must orchestrate save/finalize, not become permanent owner of
  profile/workout/nutrition truth.

Exit criteria:

- completed onboarding can persist to remote truth in a controlled path.

## Phase 3: Resume Beyond Device-Local Only

Implement:

- evaluate whether remote resume is needed,
- if needed, add remote draft snapshot format,
- restore precedence rules:
  - verified remote state
  - remote draft
  - local checkpoint
  - fresh flow

Comment:
Remote resume tabhi add karna chahiye jab real user interruption cases
important ho. Har cheez cloud me daalna default goal nahi hai.

Exit criteria:

- resume strategy is deterministic and conflict-safe.

## Phase 4: Production Analytics Sink

Implement:

- keep current `OnboardingAnalyticsEvent`,
- keep current `OnboardingAnalyticsTracker`,
- replace placeholder logger with injected production logger,
- add environment-aware no-op / debug logger if needed.

Possible sink directions:

- Firebase Analytics,
- backend event collector,
- PostHog / Segment style provider.

Suggested platform files:

- `apps/app/src/main/java/com/tnyx/analytics/...`
- updated `OnboardingAnalyticsModule.kt`

Events that should remain stable:

- `screen_view`
- `next_clicked`
- `back_clicked`
- `onboarding_completed`
- future auth handoff / merge recovery events if added

Exit criteria:

- analytics events leave the app through a real sink with no screen changes.

## Phase 5: Remote Config

Implement only after remote truth and analytics are stable:

- `RemoteConfigManager` contract,
- app-owned implementation,
- config schema for optional branches only,
- safe defaults when config fetch fails.

Allowed remote-config use:

- show/hide optional sections,
- tune intro breadth,
- tune referral/source prompts,
- enable staged experiments.

Not allowed:

- renaming IDs,
- breaking persisted position truth,
- silently reshaping required steps without migration strategy.

Exit criteria:

- remote config can tune additive behavior without destabilizing checkpoint
  compatibility.

## Data Ownership Rules

When this roadmap starts, keep these ownership boundaries:

- onboarding owns orchestration and temporary draft/checkpoint state,
- profile owns identity/body baseline fields,
- workout owns workout preference truth,
- nutrition owns nutrition preference truth,
- auth owns account/session identity,
- analytics owns event delivery,
- remote config owns optional flow tuning only.

Comment:
Onboarding ko permanent data owner banana future tech debt hoga. Isko avoid
karna hai.

## Risks To Control

1. Guest draft overwrite after sign-in
2. Remote vs local conflict ambiguity
3. Duplicate completion writes
4. Broken resume after flow version changes
5. Analytics coupling directly into ViewModel logic
6. Remote config changing flow without migration-safe rules

## Validation Strategy For The Future

When this roadmap is activated, validation should expand in stages:

1. unit tests for merge policy and handoff rules
2. unit tests for remote bootstrap/finalization use cases
3. repository tests for local + remote resume behavior
4. onboarding integration tests for guest -> auth continuation
5. compile/build validation

Expected command baseline:

- `./gradlew.bat :features:onboarding:testDebugUnitTest`
- `./gradlew.bat :app:testDebugUnitTest`
- `./gradlew.bat :app:compileDebugKotlin`

Future additions only when test coverage exists:

- auth integration checks
- remote repository tests
- analytics logger tests

## Explicit Do-Not-Start Note

Do not begin this roadmap simply because the structure now exists.

Only start when:

- the user explicitly confirms,
- the broader app is considered complete enough,
- backend/auth direction is confirmed.

Comment:
Ye file execution order batati hai, approval nahi deti.

## Success Criteria

This roadmap will be considered complete only when:

- guest-to-auth onboarding continuation works safely,
- remote onboarding finalization is real and stable,
- production analytics sink is wired,
- optional remote config exists with safe defaults,
- current local checkpoint architecture remains understandable and reversible.

## Truth Boundary

- This file is a future roadmap only.
- It does not authorize immediate implementation.
- Runtime source remains current behavior truth.
- `.ai/CURRENT.md` should not point to this roadmap until the user explicitly
  activates it.
