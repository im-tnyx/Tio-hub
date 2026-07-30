---
task_id:
title:
status: planned
priority:
owner:
branch:
github_issue:
github_pr:
platforms: []
created:
updated:
---

# Task: <title>

Allowed status values: `planned`, `ready`, `in-progress`, `blocked`, `review`, `completed`, `cancelled`.

## Goal

One observable outcome.

## Acceptance Criteria

- [ ] Verifiable completion condition

## Scope

- Included work

## Out Of Scope

- Explicit exclusions

## Current State

Only facts needed to resume. Include repository identity and unrelated dirty work when relevant.

## Workstreams

### Primary Owner

- [ ] Work item

### Additional Owner

Include only when the same objective genuinely spans another owner.

## Decisions

- Decision
- Reason
- Canonical reference or runtime evidence

## Blockers And Open Questions

- Blocker
- Required evidence or decision

## Files Touched

- Repository-relative path

## Validation

- Command:
- Result:
- Remaining validation:

## Next Action

Exactly one highest-value next action.

## Required Context

List only the `.ai/core/` files and canonical docs this task actually needs.
Agent loads only these — not the full `.ai/` layer.

- `.ai/core/architecture-summary.md`
- `.ai/core/coding-rules.md`
- `.ai/core/ui-rules.md`
- (remove lines not needed for this task)

## Canonical References

- `AGENTS.md`
- `.ai/CURRENT.md`
- Applicable task playbook under `.ai/task-playbooks/`
- Relevant canonical docs under `apps/docs/`
- Relevant runtime source/config
