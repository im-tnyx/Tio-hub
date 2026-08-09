-- Backfill app-owned users and identity mappings that predate the active
-- auth.users signup trigger. Both inserts are idempotent.

insert into public.users (
    id,
    display_name,
    timezone
)
select
    auth_user.id,
    coalesce(
        auth_user.raw_user_meta_data ->> 'display_name',
        auth_user.raw_user_meta_data ->> 'full_name',
        auth_user.raw_user_meta_data ->> 'name',
        ''
    ),
    coalesce(
        auth_user.raw_user_meta_data ->> 'timezone',
        'Asia/Kolkata'
    )
from auth.users as auth_user
on conflict (id) do nothing;

insert into public.auth_identities (
    user_id,
    provider,
    provider_uid,
    last_login_at
)
select
    auth_user.id,
    'supabase',
    auth_user.id::text,
    now()
from auth.users as auth_user
on conflict (provider, provider_uid) do nothing;
