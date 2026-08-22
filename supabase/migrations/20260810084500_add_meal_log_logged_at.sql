-- Persist the user-selected meal date and time while retaining log_date for diary queries.

alter table public.meal_logs
    add column logged_at timestamptz;

-- Legacy rows only stored a date. Preserve that date and use their creation time
-- as the best available approximation instead of inventing a new meal time.
update public.meal_logs
set logged_at = (
    log_date::timestamp + (created_at at time zone 'UTC')::time
) at time zone 'UTC'
where logged_at is null;

alter table public.meal_logs
    alter column logged_at set default now(),
    alter column logged_at set not null;

create index meal_logs_user_logged_at_idx
    on public.meal_logs (user_id, logged_at desc);

comment on column public.meal_logs.logged_at is
    'Exact meal date-time selected by the user, stored as an absolute instant.';
