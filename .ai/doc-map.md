# Tio-hub Documentation Map

Status: Orientation helper
Scope: repository root
Truth Boundary: This file routes agents to source truth. It does not replace runtime source or canonical docs.

## Execution And Recovery

- Repository rules: `AGENTS.md`
- Active task pointer: `.ai/CURRENT.md`
- Active execution detail: the single task referenced by `.ai/CURRENT.md`
- Temporary task records: `.ai/tasks/`
- Closed task records: `.ai/archive/<year>/`

Task files cannot override runtime source, canonical docs, GitHub history, or database evidence.

## Repository Orientation

- Concise project context: `.ai/project-context.md`
- Architecture summary: `.ai/architecture-summary.md`
- Ownership summary: `.ai/ownership-rules.md`
- Coding rules: `.ai/coding-rules.md`
- UI rules: `.ai/ui-rules.md`
- Data/security rules: `.ai/supabase-rules.md`
- Working workflow: `.ai/workflow.md`

## Android And Wear Truth

- App instructions: `apps/AGENTS.md`
- Current Android status: `apps/docs/ANDROID_APP_PROGRESS.md`
- Docs index: `apps/docs/README.md`
- Architecture: `apps/docs/ARCHITECTURE.md`
- Engineering rules: `apps/docs/ENGINEERING_GUIDELINES.md`
- Definition of Done: `apps/docs/DEFINITION_OF_DONE.md`
- Navigation: `apps/docs/NAVIGATION_GUIDE.md`
- Ownership: `apps/docs/PROFILE_SETTINGS_GUIDE.md`
- Wear plan: `apps/docs/WEAR_OS_PLAN.md`
- Wear status: `apps/docs/WEAR_OS_PROGRESS.md`

## Workout Truth

- Phone runtime: `apps/features/workout/`
- Shared Phone/Watch contracts: `apps/shared/src/main/java/com/tnyx/shared/workout/`
- Wear runtime: `apps/wear/src/main/java/com/tnyx/wear/`
- Product, UX, gender-aware media, and 90-day target: `apps/docs/WORKOUT_PRODUCT_BLUEPRINT.md`
- Workout persistence plan: `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`
- Current status: `apps/docs/ANDROID_APP_PROGRESS.md`

## Data And Supabase

- Current migrations: `supabase/migrations/`
- Incremental data plan: `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md`
- AI safety summary: `.ai/supabase-rules.md`

Do not infer a table, RLS policy, RPC, backend, or sync path from a planning document. Inspect checked-in migrations and runtime wiring.

## Design References

- Product/design notes: `design/*.md`
- Decompiled or captured references: `design/references/`

Reference material is not product-owned implementation source. Verify provenance before using code, datasets, artwork, media, strings, or remote URLs.

## External Repository Comparisons

`G:\projects\Tnyx-hub` may be inspected only when the task explicitly requests comparison or migration analysis. Its Flutter/backend/database state is not Tio-hub runtime truth.
