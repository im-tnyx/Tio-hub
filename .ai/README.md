# AI Operator And Continuity Layer

Status: Helper documentation for AI-assisted work
Scope: `Tio-hub` repository
Truth Boundary: `.ai/` improves orientation and continuity. It does not replace runtime source, canonical Android/Wear docs, migrations, GitHub history, or external evidence.

This directory is intentionally compact. It should point to source truth instead of duplicating it.

## Start Here

At the beginning of a work session:

1. Read [`AGENTS.md`](../AGENTS.md).
2. Read [`CURRENT.md`](CURRENT.md).
3. When `Active Task` is not `none`, read only the referenced task file.
4. Read only the `core/` files listed under that task's `Required Context` section.
5. For app work, read [`apps/AGENTS.md`](../apps/AGENTS.md).
6. Read only the relevant canonical docs and runtime source named by the task.

Do not preload every task, archive record, app document, or reference archive.
Do not load all `core/` files — load only those listed in the active task's `Required Context`.

## Orientation Files

**Root-level** (always available for orientation):

- [Project Context](project-context.md)
- [Workflow](workflow.md)
- [Documentation Map](doc-map.md)

**`core/`** (stable summaries — load only when named by the active task):

- [Architecture Summary](core/architecture-summary.md)
- [Ownership Rules](core/ownership-rules.md)
- [Coding Rules](core/coding-rules.md)
- [UI Rules](core/ui-rules.md)
- [Supabase Rules](core/supabase-rules.md)

**`task-playbooks/`** (execution guides by platform):

- [Android](task-playbooks/android.md)
- [Supabase](task-playbooks/supabase.md)
- [Wear OS](task-playbooks/wear.md)

## Continuity Files

- `CURRENT.md`: tiny pointer to one primary active task
- `tasks/`: compact tracked task checkpoints
- `templates/task-template.md`: standard task format
- `archive/<year>/`: completed tasks after merge or explicit closure

Task files record execution state only. They cannot override canonical docs or source.

## Repository Identity Guard

- `G:\projects\Tio-hub` is the native Android/Wear target repository.
- `G:\projects\Tnyx-hub` is separate and must be treated only as an explicit comparison when requested.
- Never import feature status, backend truth, database truth, or task state from another repository by assumption.

## Canonical References

- [Android App Progress](../apps/docs/ANDROID_APP_PROGRESS.md)
- [Android Docs Index](../apps/docs/README.md)
- [Engineering Guidelines](../apps/docs/ENGINEERING_GUIDELINES.md)
- [Definition Of Done](../apps/docs/DEFINITION_OF_DONE.md)
- [Architecture Changelog](../apps/docs/ARCHITECTURE_CHANGELOG.md)
- [Architecture Decision Records](../apps/docs/adr/README.md)
- [Profile / Settings Guide](../apps/docs/PROFILE_SETTINGS_GUIDE.md)
- [Architecture](../apps/docs/ARCHITECTURE.md)
- [Navigation Guide](../apps/docs/NAVIGATION_GUIDE.md)
- [Supabase Incremental Setup Plan](../apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md)
- [Onboarding Flow Detailed](../apps/docs/ONBOARDING_FLOW_DETAILED.md)
- [Apps README](../apps/README.md)

## Safety

- Never store secrets, `.env` values, tokens, credentials, private user data, or raw private prompts in `.ai/`.
- Do not copy large source excerpts into tasks; link to the relevant file and record the decision.
- Update checkpoints only after meaningful progress, a decision, a blocker, validation, or next-action change.
