# Post-Merge Sync Guide

Use this guide after a pull request is merged into `main`.

This is written for humans and AI coding agents such as Codex, Gemini, Claude, or any other assistant working in this repository. Goal simple hai: merge ke baad local repo, remote refs, and future work branches clean state mein aa jayein.

---

## When To Use This

Run this after:

- a PR is merged on GitHub
- a branch is fast-forwarded into `main`
- an AI agent pushed a merge result
- you are about to start new work and want latest `main`

Do not use this guide to overwrite uncommitted work. Pehle apna work save, commit, stash, ya explicitly discard karo.

---

## Safety First

Before syncing:

```bash
git status -sb
```

Expected clean output:

```text
## main...origin/main
```

or:

```text
## codex/some-branch...origin/codex/some-branch
```

If files are modified:

- Do not run destructive commands.
- Do not use `git reset --hard` unless the user explicitly asks.
- Commit or stash intentional work first.
- If changes are not yours, stop and ask.

---

## Standard Sync Flow

From any branch:

```bash
git fetch origin
git switch main
git pull --ff-only origin main
```

If `git switch main` is not available:

```bash
git checkout main
git pull --ff-only origin main
```

Why `--ff-only`:

- It updates local `main` only if Git can fast-forward.
- It avoids accidental merge commits.
- It keeps history clean and predictable.

---

## Verify The Merge Commit

Check the latest commits:

```bash
git log --oneline -5 --decorate
```

If you know the PR commit SHA:

```bash
git branch --contains <commit-sha>
```

Expected:

- `main` contains the merged commit.
- `origin/main` points to the same commit after fetch.

You can also verify exact refs:

```bash
git rev-parse main
git rev-parse origin/main
```

Both should match after a clean sync.

---

## After Fast-Forward Push Merge

If a PR was merged by pushing the PR branch directly into `main`:

```bash
git fetch origin main
git branch -f main origin/main
```

Only do this when:

- `origin/main` is already verified.
- local `main` has no unique commits.
- working tree is clean.

Do not use this to hide local work.

---

## Optional Branch Cleanup

After the PR is merged and no one needs the branch:

```bash
git branch -d <branch-name>
git push origin --delete <branch-name>
```

Use `git branch -D` only when you intentionally want to delete an unmerged local branch. AI agents should not use force-delete unless the user explicitly asks.

For Codex-style branches:

```bash
git branch -d codex/<task-name>
git push origin --delete codex/<task-name>
```

---

## Start New Work From Fresh Main

```bash
git switch main
git pull --ff-only origin main
git switch -c codex/<new-task-name>
```

Branch naming examples:

```text
codex/fix-auth-session-routing
codex/update-docs-for-supabase-plan
codex/clean-workout-placeholder-nav
```

Other AI agents may use their own prefix if the user wants:

```text
gemini/<task-name>
claude/<task-name>
ai/<task-name>
```

Default preference for Codex work remains:

```text
codex/<task-name>
```

---

## Validation After Sync

For Android app changes, use:

```bash
cd apps
./gradlew :app:compileDebugKotlin
```

Windows:

```powershell
cd apps
.\gradlew.bat :app:compileDebugKotlin
```

For broader CI-like local checks:

```bash
cd apps
./gradlew :app:compileDebugKotlin :wear:compileDebugKotlin :shared:test :features:auth:testDebugUnitTest
```

Run only the checks relevant to your change. Docs-only changes do not need Android compile unless they were bundled with code.

---

## AI Agent Rules

Agents must:

- Check `git status -sb` before sync.
- Never discard user changes without explicit instruction.
- Prefer `git pull --ff-only`.
- Avoid merge commits unless the user asks for one.
- Do not use `git reset --hard` as a shortcut.
- Verify the target branch and commit SHA before pushing.
- Tell the user what was synced, pushed, or skipped.

Agents should mention:

- current branch
- target branch
- latest commit SHA
- validation commands run
- whether the working tree is clean

---

## Quick Checklist

- [ ] Working tree checked.
- [ ] Remote refs fetched.
- [ ] `main` fast-forwarded.
- [ ] Merge commit verified.
- [ ] Old branch deleted only if safe/requested.
- [ ] New work starts from fresh `main`.
- [ ] Validation run or intentionally skipped.

Keep sync boring. Boring git is good git.
