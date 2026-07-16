# Task Continuity Workflow

Truth Boundary: Task files are compact execution checkpoints. They do not replace runtime source, canonical Android/Wear docs, GitHub Issues, Pull Requests, migrations, or external evidence.

## Task Routing

Route by the primary objective:

- `android/`: native phone application work
- `watch/`: Wear OS-owned work
- `database/`: Supabase migrations, RLS, RPC, grants, and schema governance
- `docs/`: documentation-only work
- `cross-platform/`: one objective spanning Android, Wear, shared contracts, data, or an explicitly requested external comparison

Do not put unrelated objectives in one task. Keep one primary active task per session.

## Starting A Task

1. Confirm repository identity and requested scope.
2. Create a file from `.ai/templates/task-template.md`.
3. Prefer `<github-issue>-<slug>.md`; without an issue use `local-<YYYYMMDD>-<slug>.md`.
4. Point `.ai/CURRENT.md` to that file.
5. Read `apps/AGENTS.md` for app work.
6. Read only relevant canonical docs and runtime source.
7. Record unrelated dirty work that must be preserved.

## During Work

Update the task only after a meaningful change:

- milestone completed
- architecture or ownership decision made
- blocker found or resolved
- validation result changed
- scope or next action changed
- work moved to another branch/platform

Keep checkpoints concise. Link to truth instead of copying large documents or code.

## Completing A Task

1. Run appropriate validation.
2. Set status to `completed` or `cancelled`.
3. Record final commit/PR only when one exists.
4. Update canonical docs only if their truth changed.
5. Set `.ai/CURRENT.md` to `Active Task: none`.
6. Move the task to `.ai/archive/<year>/` only after merge or explicit closure.

## Safety

Never store secrets, tokens, `.env` values, credentials, private user data, raw private prompts, or generated build output in task files.
