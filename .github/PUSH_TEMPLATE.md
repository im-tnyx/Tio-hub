# Push And PR Template For AI Agents

Use this template before pushing any branch or opening/updating a pull request.

Audience: Codex, Gemini, Claude, and other AI coding agents working on Tio-hub.

Primary goal: push only intentional, reviewable, validated changes.

---

## Required Context

Fill this before pushing:

```text
Repo:
Base branch:
Working branch:
Task:
Commit message:
PR title:
PR summary:
Validation:
```

Example:

```text
Repo: im-tnyx/Tio-hub
Base branch: main
Working branch: codex/clean-workout-placeholder-nav
Task: Remove Workout secondary nav and keep Workout as simple placeholder.
Commit message: refactor(workout): remove secondary workout nav
PR title: refactor(workout): remove secondary workout nav
PR summary:
- Removed Workout tab secondary navigation from the app shell.
- Removed shell state/action coupling for Workout sub-tabs.
- Kept Workout as a simple placeholder for future redesign.
- Updated docs to match the cleaned placeholder state.
Validation:
- cd apps
- .\gradlew.bat :app:compileDebugKotlin
```

---

## Pre-Push Checklist

Run:

```bash
git status -sb
git diff --stat
git diff --check
```

Confirm:

- [ ] Branch name is correct.
- [ ] Base branch is correct.
- [ ] Changed files match the task scope.
- [ ] No unrelated feature areas are touched.
- [ ] No secrets, `.env`, keystores, APK/AAB, build outputs, or cache files are included.
- [ ] No generated files are included unless explicitly required.
- [ ] No destructive git operation was used.
- [ ] Docs updated if runtime behavior, architecture, navigation, or ownership changed.

---

## Architecture Checklist

For Tio-hub code changes:

- [ ] Clean Architecture boundaries respected.
- [ ] `apps/core` stays feature-agnostic.
- [ ] `apps/shared` stays pure Kotlin.
- [ ] Feature logic stays in owning feature module.
- [ ] Screens remain dumb UI.
- [ ] ViewModel/domain/repository boundaries are respected.
- [ ] Navigation remains type-safe.
- [ ] App shell owns chrome only, not feature business logic.
- [ ] Supabase/backend assumptions do not leak into screens.

If any item does not apply, mention why in the PR notes.

---

## Validation Checklist

Use the smallest meaningful validation.

Docs-only:

```text
Validation: docs-only, no build required.
```

Android compile:

```bash
cd apps
./gradlew :app:compileDebugKotlin
```

Windows:

```powershell
cd apps
.\gradlew.bat :app:compileDebugKotlin
```

Broader local checks:

```bash
cd apps
./gradlew :app:compileDebugKotlin :wear:compileDebugKotlin :shared:test :features:auth:testDebugUnitTest
```

If validation cannot run, document the exact reason.

---

## Commit Template

Use concise Conventional Commit style:

```bash
git add <scoped-files>
git commit -m "<type>(<scope>): <summary>"
```

Examples:

```text
docs(repo): add post-merge sync guide
refactor(workout): remove secondary workout nav
fix(navigation): preserve profile graph route
test(auth): cover fake auth repository
```

Avoid:

```text
update
fix
final
misc changes
```

---

## Push Command

For a new branch:

```bash
git push -u origin <branch-name>
```

For an existing branch:

```bash
git push origin <branch-name>
```

Never force-push unless:

- the user explicitly asks, or
- maintainers requested history cleanup, and
- you clearly explain the risk.

If force push is approved:

```bash
git push --force-with-lease origin <branch-name>
```

Prefer `--force-with-lease` over `--force`.

---

## PR Body Template

```markdown
## Summary

- 

## Why

- 

## Scope

- 

## Validation

- [ ] `cd apps`
- [ ] `./gradlew :app:compileDebugKotlin`

## Notes

- 
```

For documentation-only work:

```markdown
## Validation

- Docs-only change; no Android build required.
```

---

## GitHub CLI Commands

Create PR:

```bash
gh pr create \
  --base main \
  --head <branch-name> \
  --title "<PR title>" \
  --body "<PR body>"
```

View PR mergeability:

```bash
gh pr view <number> --json mergeable,mergeStateStatus,statusCheckRollup
```

If `gh` is not installed:

- push the branch
- provide the compare URL
- include PR title and summary for the user

Compare URL format:

```text
https://github.com/im-tnyx/Tio-hub/compare/main...<branch-name>?expand=1
```

---

## Post-Push Report

After pushing, tell the user:

```text
Branch:
Commit:
Pushed:
PR:
Validation:
Working tree:
```

Example:

```text
Branch: codex/clean-workout-placeholder-nav
Commit: aa228e2 refactor(workout): remove secondary workout nav
Pushed: yes
PR: https://github.com/im-tnyx/Tio-hub/pull/6
Validation: .\gradlew.bat :app:compileDebugKotlin -> BUILD SUCCESSFUL
Working tree: clean
```

---

## Agent Safety Rules

Agents must not:

- push unrelated changes
- rewrite history without explicit approval
- commit secrets
- hide failing validation
- claim PR creation if the tool was unavailable
- delete branches unless requested
- merge PRs unless explicitly told to proceed

Agents should:

- keep scope tight
- read nearby code before editing
- prefer existing patterns
- update docs when behavior changes
- report exact commands and outcomes

Ship clean, small, reviewable work. Future maintainers will thank you.
