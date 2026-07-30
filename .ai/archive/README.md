# Archive

Completed task records for `Tio-hub`.

## Policy

- Move a task here only after its associated branch is merged or the task is explicitly closed by the user.
- Never point `.ai/CURRENT.md` at an archived task.
- Do not infer product status, architecture truth, or feature availability from archived tasks.
- Use runtime source and canonical docs (`apps/docs/`) for those purposes.

## Folder Structure

Tasks are grouped by year:

```
archive/
  2026/
    local-YYYYMMDD-<slug>.md
```

## Lifecycle

```
.ai/tasks/<scope>/local-YYYYMMDD-<slug>.md   ← active or paused
        ↓  (after merge or explicit closure)
.ai/archive/<year>/local-YYYYMMDD-<slug>.md  ← archived, read-only history
```

## Naming Convention

Task files use the same name as when active:

```
local-YYYYMMDD-short-slug.md
```

`local-` prefix indicates a locally initiated task (not externally tracked).

## What Is Preserved

Archived tasks record:

- The original objective and scope
- Key decisions and their rationale
- Validation result at closure
- Files touched
- Any known follow-up items

They do not contain chat transcripts, speculative plans, secrets, or copied canonical docs.
