-- Migration: Rename the app-owned profile table to users.
-- auth.users remains the Supabase authentication identity source.

alter table public.profiles rename to users;

alter table public.users
    rename constraint profiles_pkey to users_pkey;
alter table public.users
    rename constraint profiles_id_fkey to users_id_fkey;
alter table public.users
    rename constraint profiles_referred_by_id_fkey to users_referred_by_id_fkey;
alter table public.users
    rename constraint profiles_username_format_check to users_username_format_check;
alter table public.users
    rename constraint profiles_gender_check to users_gender_check;
alter table public.users
    rename constraint profiles_streak_check to users_streak_check;
alter table public.users
    rename constraint profiles_referral_code_check to users_referral_code_check;

alter index public.profiles_username_lower_unique_idx
    rename to users_username_lower_unique_idx;
alter index public.profiles_referral_code_unique_idx
    rename to users_referral_code_unique_idx;
alter index public.profiles_referred_by_id_idx
    rename to users_referred_by_id_idx;

alter policy profiles_select_own
    on public.users rename to users_select_own;
alter policy profiles_insert_own
    on public.users rename to users_insert_own;
alter policy profiles_update_own
    on public.users rename to users_update_own;

alter trigger profiles_set_updated_at
    on public.users rename to users_set_updated_at;

create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.users (
        id,
        display_name,
        timezone
    )
    values (
        new.id,
        coalesce(
            new.raw_user_meta_data ->> 'display_name',
            new.raw_user_meta_data ->> 'full_name',
            new.raw_user_meta_data ->> 'name',
            ''
        ),
        coalesce(
            new.raw_user_meta_data ->> 'timezone',
            'Asia/Kolkata'
        )
    )
    on conflict (id) do nothing;

    insert into public.auth_identities (
        user_id,
        provider,
        provider_uid,
        last_login_at
    )
    values (
        new.id,
        'supabase',
        new.id::text,
        now()
    )
    on conflict (provider, provider_uid) do nothing;

    return new;
end;
$$;

revoke all on function private.handle_new_user()
    from public, anon, authenticated;
