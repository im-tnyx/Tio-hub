-- Migration: Nutrition Meal Logs
-- Slice: meal_logs + meal_log_items
-- Applied to: Tio-hub Supabase project
-- Date: 2026-07-31

-- ============================================================
-- TABLE: meal_logs
-- One row per meal entry per day per user
-- ============================================================

create table public.meal_logs (
    id         uuid        default gen_random_uuid() primary key,
    user_id    uuid        references auth.users(id) on delete cascade not null,
    log_date   date        not null,
    meal_type  text        not null,  -- 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'
    name       text        not null,
    image_url  text,
    created_at timestamptz default now() not null,
    updated_at timestamptz default now() not null
);

alter table public.meal_logs enable row level security;

create index meal_logs_user_date_idx on public.meal_logs (user_id, log_date);

-- Owner-scoped RLS: users can only access their own rows
create policy "meal_logs_owner_all"
    on public.meal_logs
    for all
    to authenticated
    using  (user_id = auth.uid())
    with check (user_id = auth.uid());

-- ============================================================
-- TABLE: meal_log_items
-- Individual food items belonging to a meal_log
-- ============================================================

create table public.meal_log_items (
    id             uuid             default gen_random_uuid() primary key,
    meal_log_id    uuid             references public.meal_logs(id) on delete cascade not null,
    user_id        uuid             references auth.users(id) on delete cascade not null,
    name           text             not null,
    quantity       double precision not null default 1.0,
    unit           text             not null default 'serving',
    calories       integer          not null default 0,
    protein        double precision not null default 0.0,
    carbs          double precision not null default 0.0,
    fats           double precision not null default 0.0,
    fiber          double precision not null default 0.0,
    sugar          double precision not null default 0.0,
    trans_fat      double precision not null default 0.0,
    saturated_fat  double precision not null default 0.0,
    created_at     timestamptz      default now() not null,
    updated_at     timestamptz      default now() not null
);

alter table public.meal_log_items enable row level security;

create index meal_log_items_meal_id_idx on public.meal_log_items (meal_log_id);
create index meal_log_items_user_id_idx on public.meal_log_items (user_id);

-- Owner-scoped RLS: users can only access their own items
create policy "meal_log_items_owner_all"
    on public.meal_log_items
    for all
    to authenticated
    using  (user_id = auth.uid())
    with check (user_id = auth.uid());
