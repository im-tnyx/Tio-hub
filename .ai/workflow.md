# Workflow

Use docs to freeze ownership before building large feature areas.

## Session Continuity

At session start:

1. Read root `AGENTS.md`.
2. Read `.ai/CURRENT.md`.
3. Read only its referenced task file when one is active.
4. For `apps/` work, read `apps/AGENTS.md`.
5. Inspect relevant canonical docs and runtime source.

Keep one primary task. Route tasks using `.ai/tasks/README.md`.

Update the active task only when:

- a meaningful milestone completes
- a durable decision changes
- a blocker appears or is resolved
- validation changes
- the next action changes
- repository, branch, or platform scope changes

Before ending incomplete work, make the task's `Current State`, `Validation`, and `Next Action` sufficient for another session to resume without loading the full conversation.

## Current Development Flow

1. Check current source and canonical docs.
2. Confirm feature ownership.
3. Add or update the smallest useful module/screen slice.
4. Keep UI scaffolding minimal.
5. Move data behind repositories when persistence is needed.
6. Add Supabase tables only when the real data shape is known.
7. Run validation.
8. Update progress docs when behavior or architecture changes.
9. Update ADRs when durable architecture decisions change.
10. Update the architecture changelog when module boundaries, data flow, navigation policy, or engineering practice changes.

## Closing Or Switching Tasks

1. Record the latest validation and remaining risk.
2. Mark the task `completed`, `cancelled`, or leave it accurately in progress.
3. Set `.ai/CURRENT.md` to `none` or point it to the new primary task.
4. Archive only after merge or explicit closure.
5. Never put secrets, environment values, or private user data in task files.

## Source of Truth Order

When code and docs conflict:

1. Runtime source/config wins for actual behavior.
2. Product docs win for ownership and product status.
3. Platform-local docs win for implementation details.
4. This `.ai` directory is only a concise orientation layer.

## Do Not Start Without Explicit Need

Do not create large future areas before a slice needs them:

- Full onboarding rebuild
- Health integrations
- Recovery
- Billing / Entitlement
- Community
- Challenges
- AI Coach runtime
- Full Supabase schema

Plan them in docs first, then implement vertical slices.
