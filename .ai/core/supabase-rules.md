# Supabase Rules

Use `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md` as the canonical source for Supabase setup.

## Incremental Setup

Do not create the full database upfront.

Create tables only when a feature slice needs them.

Recommended flow:

1. Build the UI slice.
2. Identify the real data shape.
3. Create the minimum Supabase table or RPC.
4. Add RLS.
5. Add demo seed data.
6. Connect repository.
7. Test the feature end to end.

## Security

- Never expose service role keys to mobile, web, or admin clients.
- Every client-accessible table needs RLS.
- Atomic writes should use hardened RPCs when needed.
- Backend/admin-only operations must stay server-side.

## Hardcoded Data

Hardcoded data is allowed only as temporary UI scaffolding.

When a feature becomes testable, move source-of-truth data behind:

- repository
- Supabase table or RPC
- demo seed data
- clear test path

## Future TypeScript Workspace

The future Turborepo / TypeScript workspace must not change Android feature ownership.

Backend, web, admin, shared contracts, and database migrations should be introduced with clear boundaries when that workspace starts.

## Canonical References

- [Supabase Incremental Setup Plan](../../../apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md)
- Current migrations: `supabase/migrations/`
