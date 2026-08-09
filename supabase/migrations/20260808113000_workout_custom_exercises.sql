-- Migration: Workout Custom Exercises
-- Slice: custom_exercises
-- Date: 2026-08-08

create table public.custom_exercises (
    id                      uuid        default gen_random_uuid() primary key,
    user_id                 uuid        references auth.users(id) on delete cascade not null,
    name                    text        not null,
    schema_version          integer     not null default 2,
    body_part               text,
    aliases                 text[]      not null default '{}',
    primary_muscle_groups   text[]      not null default '{}',
    secondary_muscle_groups text[]      not null default '{}',
    equipment               text[]      not null default '{}',
    instructions            text[]      not null default '{}',
    media_assets            jsonb       not null default '[]'::jsonb,
    tracking_type           text        not null default 'WEIGHT_REPS',
    created_at              timestamptz default now() not null,
    updated_at              timestamptz default now() not null,
    constraint custom_exercises_tracking_type_check
        check (
            tracking_type in (
                'WEIGHT_REPS',
                'BODYWEIGHT_REPS',
                'ASSISTED_BODYWEIGHT_REPS',
                'DURATION',
                'DISTANCE_DURATION',
                'STEPS_DURATION'
            )
        )
);

create index custom_exercises_user_name_idx
    on public.custom_exercises (user_id, name);

grant select, insert, update, delete on public.custom_exercises to authenticated;

alter table public.custom_exercises enable row level security;

create policy "custom_exercises_owner_all"
    on public.custom_exercises
    for all
    to authenticated
    using (user_id = auth.uid())
    with check (user_id = auth.uid());
