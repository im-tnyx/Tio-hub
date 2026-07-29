-- Remote migration version: 20260729150842
create schema if not exists private;
revoke all on schema private from public, anon, authenticated;

create table public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    username text,
    display_name text,
    avatar_url text,
    dob date,
    gender text,
    timezone text not null default 'Asia/Kolkata',
    is_onboarded boolean not null default false,
    plan_label text not null default 'Free',
    status_label text,
    current_streak integer not null default 0,
    best_streak integer not null default 0,
    referral_code text,
    referred_by_id uuid references public.profiles (id) on delete set null,
    is_active boolean not null default true,
    deleted_at timestamptz,
    last_active_at timestamptz not null default now(),
    mobile_verified_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint profiles_username_format_check check (
        username is null
        or username ~ '^[a-z0-9_]{3,30}$'
    ),
    constraint profiles_gender_check check (
        gender is null
        or lower(gender) in (
            'male',
            'female',
            'non_binary',
            'other',
            'prefer_not_to_say'
        )
    ),
    constraint profiles_streak_check check (
        current_streak >= 0
        and best_streak >= current_streak
    ),
    constraint profiles_referral_code_check check (
        referral_code is null
        or referral_code ~ '^[A-Z0-9]{6,20}$'
    )
);

create unique index profiles_username_lower_unique_idx
    on public.profiles (lower(username))
    where username is not null;

create unique index profiles_referral_code_unique_idx
    on public.profiles (referral_code)
    where referral_code is not null;

create index profiles_referred_by_id_idx
    on public.profiles (referred_by_id)
    where referred_by_id is not null;

create table public.user_nutrition_profiles (
    user_id uuid primary key references public.profiles (id) on delete cascade,
    height_cm numeric(6, 2),
    current_weight_kg numeric(6, 2),
    target_weight_kg numeric(6, 2),
    weekly_weight_change_kg numeric(4, 2),
    body_fat_percentage numeric(5, 2),
    bed_time time without time zone,
    wake_up_time time without time zone,
    activity_level text,
    steps_target integer,
    water_target_ml integer,
    preferred_diet text,
    allergies text[] not null default '{}',
    disliked_foods text[] not null default '{}',
    medical_conditions text[] not null default '{}',
    macro_targets jsonb not null default
        '{"fat": 0, "carbs": 0, "protein": 0, "calories": 0}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint user_nutrition_height_check check (
        height_cm is null or height_cm between 50 and 300
    ),
    constraint user_nutrition_current_weight_check check (
        current_weight_kg is null or current_weight_kg between 20 and 500
    ),
    constraint user_nutrition_target_weight_check check (
        target_weight_kg is null or target_weight_kg between 20 and 500
    ),
    constraint user_nutrition_weekly_change_check check (
        weekly_weight_change_kg is null
        or weekly_weight_change_kg between -5 and 5
    ),
    constraint user_nutrition_body_fat_check check (
        body_fat_percentage is null
        or body_fat_percentage between 0 and 75
    ),
    constraint user_nutrition_activity_level_check check (
        activity_level is null
        or lower(activity_level) in (
            'sedentary',
            'light',
            'moderate',
            'active',
            'very_active',
            'dynamic'
        )
    ),
    constraint user_nutrition_steps_target_check check (
        steps_target is null or steps_target between 0 and 200000
    ),
    constraint user_nutrition_water_target_check check (
        water_target_ml is null or water_target_ml between 0 and 20000
    ),
    constraint user_nutrition_macro_targets_check check (
        jsonb_typeof(macro_targets) = 'object'
    )
);

create table public.user_workout_profiles (
    user_id uuid primary key references public.profiles (id) on delete cascade,
    experience_level text,
    special_event_goal text,
    workout_location text,
    available_equipment text[] not null default '{}',
    workout_duration_mins integer,
    training_days text[] not null default '{}',
    split_program text,
    focus_areas text[] not null default '{}',
    health_concerns text[] not null default '{}',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint user_workout_experience_level_check check (
        experience_level is null
        or lower(experience_level) in (
            'fresh',
            'beginner',
            'intermediate',
            'advanced'
        )
    ),
    constraint user_workout_location_check check (
        workout_location is null
        or lower(workout_location) in ('home', 'gym', 'both')
    ),
    constraint user_workout_duration_check check (
        workout_duration_mins is null
        or workout_duration_mins between 5 and 300
    ),
    constraint user_workout_split_program_check check (
        split_program is null
        or lower(split_program) in (
            'auto',
            'full_body',
            'upper_lower',
            'ppl',
            'body_part',
            'custom'
        )
    )
);

create table public.auth_identities (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles (id) on delete cascade,
    provider text not null,
    provider_uid text not null,
    provider_metadata jsonb not null default '{}'::jsonb,
    last_login_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint auth_identities_provider_check check (
        provider ~ '^[a-z0-9_]{2,32}$'
    ),
    constraint auth_identities_provider_uid_check check (
        length(btrim(provider_uid)) between 1 and 255
    ),
    constraint auth_identities_provider_uid_unique unique (
        provider,
        provider_uid
    ),
    constraint auth_identities_user_provider_unique unique (
        user_id,
        provider
    )
);

create index auth_identities_user_id_idx
    on public.auth_identities (user_id);

create view public.profile_overview
with (security_invoker = true)
as
select
    profile.id,
    profile.username,
    profile.display_name,
    profile.dob,
    profile.gender,
    profile.plan_label,
    profile.avatar_url,
    profile.status_label,
    profile.current_streak as streak,
    nutrition.body_fat_percentage::double precision as body_fat,
    nutrition.current_weight_kg::double precision as weight,
    round(nutrition.height_cm)::integer as height,
    case
        when nutrition.current_weight_kg is null
            or nutrition.height_cm is null
            or nutrition.height_cm = 0
        then null
        else round(
            nutrition.current_weight_kg
            / power(nutrition.height_cm / 100, 2),
            2
        )::double precision
    end as bmi,
    case
        when nutrition.current_weight_kg is null
            or nutrition.height_cm is null
            or profile.dob is null
            or lower(profile.gender) not in ('male', 'female')
        then null
        else round(
            (10 * nutrition.current_weight_kg)
            + (6.25 * nutrition.height_cm)
            - (
                5 * extract(
                    year from age(current_date, profile.dob)
                )
            )
            + case
                when lower(profile.gender) = 'male' then 5
                else -161
            end
        )::integer
    end as bmr,
    nutrition.target_weight_kg::double precision as journey_target_weight
from public.profiles as profile
left join public.user_nutrition_profiles as nutrition
    on nutrition.user_id = profile.id;

create or replace function private.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function private.set_updated_at();

create trigger user_nutrition_profiles_set_updated_at
before update on public.user_nutrition_profiles
for each row execute function private.set_updated_at();

create trigger user_workout_profiles_set_updated_at
before update on public.user_workout_profiles
for each row execute function private.set_updated_at();

create trigger auth_identities_set_updated_at
before update on public.auth_identities
for each row execute function private.set_updated_at();

create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (
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

revoke all on function private.set_updated_at()
    from public, anon, authenticated;
revoke all on function private.handle_new_user()
    from public, anon, authenticated;

create trigger on_auth_user_created
after insert on auth.users
for each row execute function private.handle_new_user();

insert into public.profiles (
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

alter table public.profiles enable row level security;
alter table public.user_nutrition_profiles enable row level security;
alter table public.user_workout_profiles enable row level security;
alter table public.auth_identities enable row level security;

revoke all on table public.profiles from anon, authenticated;
revoke all on table public.user_nutrition_profiles from anon, authenticated;
revoke all on table public.user_workout_profiles from anon, authenticated;
revoke all on table public.auth_identities from anon, authenticated;
revoke all on table public.profile_overview from anon, authenticated;

grant select, insert, update on table public.profiles to authenticated;
grant select, insert, update
    on table public.user_nutrition_profiles to authenticated;
grant select, insert, update
    on table public.user_workout_profiles to authenticated;
grant select on table public.profile_overview to authenticated;

grant all on table public.profiles to service_role;
grant all on table public.user_nutrition_profiles to service_role;
grant all on table public.user_workout_profiles to service_role;
grant all on table public.auth_identities to service_role;
grant select on table public.profile_overview to service_role;

create policy profiles_select_own
on public.profiles
for select
to authenticated
using ((select auth.uid()) = id);

create policy profiles_insert_own
on public.profiles
for insert
to authenticated
with check ((select auth.uid()) = id);

create policy profiles_update_own
on public.profiles
for update
to authenticated
using ((select auth.uid()) = id)
with check ((select auth.uid()) = id);

create policy user_nutrition_profiles_select_own
on public.user_nutrition_profiles
for select
to authenticated
using ((select auth.uid()) = user_id);

create policy user_nutrition_profiles_insert_own
on public.user_nutrition_profiles
for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy user_nutrition_profiles_update_own
on public.user_nutrition_profiles
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy user_workout_profiles_select_own
on public.user_workout_profiles
for select
to authenticated
using ((select auth.uid()) = user_id);

create policy user_workout_profiles_insert_own
on public.user_workout_profiles
for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy user_workout_profiles_update_own
on public.user_workout_profiles
for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

insert into storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
values (
    'tio-profile',
    'tio-profile',
    true,
    5242880,
    array['image/jpeg']
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

create policy tio_profile_select_own
on storage.objects
for select
to authenticated
using (
    bucket_id = 'tio-profile'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

create policy tio_profile_insert_own
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'tio-profile'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

create policy tio_profile_update_own
on storage.objects
for update
to authenticated
using (
    bucket_id = 'tio-profile'
    and (storage.foldername(name))[1] = (select auth.uid())::text
)
with check (
    bucket_id = 'tio-profile'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

create policy tio_profile_delete_own
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'tio-profile'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);
