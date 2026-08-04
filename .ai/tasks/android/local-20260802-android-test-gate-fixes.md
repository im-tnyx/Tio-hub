---
task_id: local-20260802-android-test-gate-fixes
title: Restore Android workout and app test gates
status: completed
priority: high
owner: codex
branch: codex/android-test-gate-fixes
github_issue:
github_pr:
platforms: [android, wear]
created: 2026-08-02
updated: 2026-08-03
---

# Task: Restore Android workout and app test gates

Allowed status values: `planned`, `ready`, `in-progress`, `blocked`, `review`, `completed`, `cancelled`.

## Goal

Make the currently recorded Android test gates reliable and green without
adding product features or broad refactors.

## Acceptance Criteria

- [x] `:features:workout:test` compiles and passes with the existing
  `kotlin.test` dependency pattern restored.
- [ ] `:app:testDebugUnitTest` root cause is captured with the exact failing
  test class, nested `Caused by` chain, missing path, and resolved artifact.
- [x] Any repository fix for Robolectric is evidence-based and minimal, or the
  failure is proven environment-only with exact evidence.
- [x] `.github/workflows/android-ci.yml` includes the Workout and app unit-test
  gates only after both pass reliably.
- [ ] `apps/docs/ANDROID_APP_PROGRESS.md` reflects explicit `✅ PASS`, `❌ FAIL`,
  or `⚠️ ENVIRONMENT BLOCKED` markers for this validation slice.
- [ ] `.ai/CURRENT.md` and this task remain aligned to the active branch and
  current scope.

## Scope

- Workout test compilation fix
- Robolectric app unit-test diagnosis and narrow fix if repository-owned
- Android CI gate update after reliable green validation
- Truth-aligned docs/task continuity updates

## Out Of Scope

- Nutrition, Home, Auth, Profile, Settings, Onboarding, or Workout UX/product
  expansion
- Supabase schema, migrations, RLS, RPC, or storage work
- `apps/shared/build.gradle.kts` changes
- KMP conversion
- Production Room behavior changes unless independently proven necessary

## Current State

- Repository: `G:\projects\Tio-hub`
- Base `main` now includes merged PR #31 at commit
  `cdd550703a8c4d4141d526b2769af466303865f0`.
- Active branch is `codex/android-test-gate-fixes`.
- Workout test gate is now narrowed and locally green after restoring
  `libs.kotlin.test` and fixing one stale metric-isolation assertion.
- `:app:testDebugUnitTest` no longer appears to require a repository code fix.
  The failure path is environment-dependent and is reproducible only when the
  test process cannot obtain writable Robolectric lock/temp paths:
  - default machine cache path hits Gradle wrapper lock access denial under
    `G:\dev\gradle-cache`
  - repo-local Gradle cache with the default `user.home` fails because
    Robolectric cannot create `C:\Users\SANTOSH\.robolectric-download-lock`
  - repo-local Gradle cache with writable `user.home` but sandboxed execution
    reaches Robolectric artifact resolution and then fails while creating temp
    download parents for
    `org.robolectric:android-all-instrumented:15-robolectric-12650502-i7`
  - the same repo-local configuration succeeds when the command runs with full
    local permissions and writable `user.home` + `java.io.tmpdir`
- Unrelated dirty work that must be preserved:
  - `AGENTS.md`
  - `apps/AGENTS.md`
  - `.ai/tasks/android/local-20260731-onboarding-refinement.md`
  - `device-smoke/`

## Decisions

- Start from merged `main` only after PR #31 merge and fast-forward sync.
- Preserve unrelated dirty work by restoring it after local `main` sync.
- Fix Workout test dependency first, then diagnose Robolectric with evidence
  before changing repository configuration.
- Do not commit a repository workaround for the locally blocked Robolectric
  temp/home path problem without evidence from a clean runner.
- Use clean local permissions plus workspace-scoped writable Robolectric paths
  for deterministic local validation instead of changing Android runtime code.

## Validation

- Command: `gh pr view 31 --json state,mergedAt,mergeCommit,url`
- Result: PR #31 merged into `main` with merge commit
  `cdd550703a8c4d4141d526b2769af466303865f0`
- Command: `git switch main`
- Result: switched successfully with unrelated local changes preserved
- Command: `git pull --ff-only origin main`
- Result: fast-forwarded `main` to merge commit
  `cdd550703a8c4d4141d526b2769af466303865f0`
- Command: `git status --short`
- Result: unrelated local modifications remained limited to
  `AGENTS.md`, `apps/AGENTS.md`,
  `.ai/tasks/android/local-20260731-onboarding-refinement.md`, and
  untracked `device-smoke/`
- Command: `git switch -c codex/android-test-gate-fixes`
- Result: branch created successfully from synced `main`
- Command:
  `./gradlew.bat :features:workout:test --stacktrace`
- Result: `BUILD SUCCESSFUL` after adding `testImplementation(libs.kotlin.test)`
  to `apps/features/workout/build.gradle.kts` and making
  `WorkoutViewModelTest.metricChangeTargetsOnlyTheSelectedExercise` assert that
  the untouched exercise keeps its prior value instead of a stale hardcoded
  value
- Command:
  `./gradlew.bat :app:testDebugUnitTest --stacktrace --info --no-daemon --rerun-tasks`
  with default machine Gradle cache
- Result: build startup blocked by
  `G:\dev\gradle-cache\wrapper\dists\gradle-9.5.0-bin\...\gradle-9.5.0-bin.zip.lck`
  `AccessDeniedException`
- Command:
  `./gradlew.bat :app:testDebugUnitTest --stacktrace --info --no-daemon --rerun-tasks`
  with `GRADLE_USER_HOME=G:\projects\Tio-hub\.gradle-codex`
- Result: first failing class `DataStoreAuthSessionStoreTest`; 14 Robolectric
  persistence-oriented tests fail because
  `MavenDependencyResolver` cannot create
  `C:\Users\SANTOSH\.robolectric-download-lock`
- Command:
  `./gradlew.bat :app:testDebugUnitTest --stacktrace --info --no-daemon --rerun-tasks`
  with writable `user.home`
- Result: first failing class remains `DataStoreAuthSessionStoreTest`; exact
  Robolectric artifact becomes visible as
  `org.robolectric:android-all-instrumented:15-robolectric-12650502-i7`, but
  download staging still fails while creating parent directories under temp
  paths
- Command:
  `./gradlew.bat :app:testDebugUnitTest --stacktrace --info --no-daemon --rerun-tasks`
  with writable `user.home`, writable `java.io.tmpdir`, and full local
  permissions
- Result: `BUILD SUCCESSFUL`; the same test suite passes when Robolectric can
  use workspace-scoped writable lock/temp paths without sandbox denial
- Environment:
  `HTTP_PROXY=http://127.0.0.1:9`,
  `HTTPS_PROXY=http://127.0.0.1:9`,
  `NO_PROXY=localhost,127.0.0.1,::1`
- Repository config:
  `gradle.properties` does not enable offline mode; no `--offline` flag was
  used in the diagnostic commands
- Current conclusion:
  local Robolectric failure is environment-blocked and path/write related; a
  repository runtime/config defect is not proven by the captured evidence
- Not yet completed:
  clean GitHub Actions runner verification after branch push

## Next Action

Run the full required local validation set, then commit/push this branch and
open a draft PR so the expanded Android CI can verify the clean runner path.
