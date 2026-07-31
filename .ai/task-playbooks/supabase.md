# Supabase Task Playbook

Use this guide when adding a new Supabase table, RPC, or storage bucket to Tio-hub.

## Pre-Work

1. Confirm the feature slice exists in Android code first.
2. Identify the real data shape from the UI and ViewModel.
3. Check existing migrations in `supabase/migrations/` to avoid duplication.
4. Review `apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md` for the planned schema.

## Migration Naming

```
supabase/migrations/
  YYYYMMDDHHMMSS_<scope>_<action>.sql
```

Examples:
- `20260730193000_add_profiles_mobile_column.sql`
- `20260801120000_create_meal_logs_table.sql`

## Table Creation Checklist

- [ ] Table created with correct column types
- [ ] Primary key defined
- [ ] `user_id` references `auth.users(id)` when user-owned
- [ ] RLS enabled on the table
- [ ] RLS policy: `SELECT` — authenticated user owns the row
- [ ] RLS policy: `INSERT` — authenticated user sets own `user_id`
- [ ] RLS policy: `UPDATE` — authenticated user owns the row
- [ ] RLS policy: `DELETE` — authenticated user owns the row (when needed)
- [ ] Migration applied to connected Tio-hub project

## Security Rules

- Never expose service-role key in Android, web, or admin client code.
- Every client-accessible table must have RLS enabled.
- Backend/admin-only writes must stay server-side.
- Atomic multi-table writes should use a hardened RPC.

## Repository Wiring

After migration is applied:

1. Add interface method to `<Feature>Repository.kt`.
2. Implement in `Supabase<Feature>Repository.kt` under `apps/app/`.
3. Register the binding in the relevant DI module.
4. Test the feature end to end with a real Supabase session.

## Validation

```bash
# Compile check after wiring
cd apps
.\gradlew.bat :app:compileDebugKotlin

# Feature test
.\gradlew.bat :<feature>:testDebugUnitTest --no-configuration-cache
```

## Storage Buckets

Bucket names follow the pattern: `tio-<scope>` (e.g., `tio-profile`).

- Set bucket to private unless public read is explicitly required.
- Use signed URLs for private asset access.
- Never store a service-role key in client code to access private buckets.

## Canonical References

- [Supabase Incremental Setup Plan](../../../apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md)
- `supabase/migrations/`
- `.ai/core/supabase-rules.md`
