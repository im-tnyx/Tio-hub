-- Harden authenticated nutrition logging without changing the Android data shape.

alter table public.meal_logs enable row level security;
alter table public.meal_log_items enable row level security;

revoke all on table public.meal_logs, public.meal_log_items
    from public, anon, authenticated, service_role;

grant select, insert, update, delete
    on table public.meal_logs, public.meal_log_items
    to authenticated, service_role;

drop policy if exists "meal_logs_owner_all" on public.meal_logs;
drop policy if exists "meal_logs_owner_select" on public.meal_logs;
drop policy if exists "meal_logs_owner_insert" on public.meal_logs;
drop policy if exists "meal_logs_owner_update" on public.meal_logs;
drop policy if exists "meal_logs_owner_delete" on public.meal_logs;

create policy "meal_logs_owner_select"
    on public.meal_logs
    for select
    to authenticated
    using ((select auth.uid()) is not null and (select auth.uid()) = user_id);

create policy "meal_logs_owner_insert"
    on public.meal_logs
    for insert
    to authenticated
    with check ((select auth.uid()) is not null and (select auth.uid()) = user_id);

create policy "meal_logs_owner_update"
    on public.meal_logs
    for update
    to authenticated
    using ((select auth.uid()) is not null and (select auth.uid()) = user_id)
    with check ((select auth.uid()) is not null and (select auth.uid()) = user_id);

create policy "meal_logs_owner_delete"
    on public.meal_logs
    for delete
    to authenticated
    using ((select auth.uid()) is not null and (select auth.uid()) = user_id);

drop policy if exists "meal_log_items_owner_all" on public.meal_log_items;
drop policy if exists "meal_log_items_owner_select" on public.meal_log_items;
drop policy if exists "meal_log_items_owner_insert" on public.meal_log_items;
drop policy if exists "meal_log_items_owner_update" on public.meal_log_items;
drop policy if exists "meal_log_items_owner_delete" on public.meal_log_items;

create policy "meal_log_items_owner_select"
    on public.meal_log_items
    for select
    to authenticated
    using (
        (select auth.uid()) is not null
        and (select auth.uid()) = user_id
        and exists (
            select 1
            from public.meal_logs
            where meal_logs.id = meal_log_items.meal_log_id
              and meal_logs.user_id = (select auth.uid())
        )
    );

create policy "meal_log_items_owner_insert"
    on public.meal_log_items
    for insert
    to authenticated
    with check (
        (select auth.uid()) is not null
        and (select auth.uid()) = user_id
        and exists (
            select 1
            from public.meal_logs
            where meal_logs.id = meal_log_items.meal_log_id
              and meal_logs.user_id = (select auth.uid())
        )
    );

create policy "meal_log_items_owner_update"
    on public.meal_log_items
    for update
    to authenticated
    using (
        (select auth.uid()) is not null
        and (select auth.uid()) = user_id
        and exists (
            select 1
            from public.meal_logs
            where meal_logs.id = meal_log_items.meal_log_id
              and meal_logs.user_id = (select auth.uid())
        )
    )
    with check (
        (select auth.uid()) is not null
        and (select auth.uid()) = user_id
        and exists (
            select 1
            from public.meal_logs
            where meal_logs.id = meal_log_items.meal_log_id
              and meal_logs.user_id = (select auth.uid())
        )
    );

create policy "meal_log_items_owner_delete"
    on public.meal_log_items
    for delete
    to authenticated
    using (
        (select auth.uid()) is not null
        and (select auth.uid()) = user_id
        and exists (
            select 1
            from public.meal_logs
            where meal_logs.id = meal_log_items.meal_log_id
              and meal_logs.user_id = (select auth.uid())
        )
    );

alter table public.meal_logs
    add constraint meal_logs_name_not_blank
        check (btrim(name) <> ''),
    add constraint meal_logs_meal_type_valid
        check (meal_type in ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS')),
    add constraint meal_logs_id_user_id_key
        unique (id, user_id);

alter table public.meal_log_items
    drop constraint meal_log_items_meal_log_id_fkey,
    add constraint meal_log_items_meal_log_owner_fkey
        foreign key (meal_log_id, user_id)
        references public.meal_logs(id, user_id)
        on delete cascade,
    add constraint meal_log_items_name_not_blank
        check (btrim(name) <> ''),
    add constraint meal_log_items_unit_not_blank
        check (btrim(unit) <> ''),
    add constraint meal_log_items_quantity_positive
        check (quantity > 0 and quantity < 'Infinity'::double precision),
    add constraint meal_log_items_nutrients_nonnegative
        check (
            calories >= 0
            and protein >= 0 and protein < 'Infinity'::double precision
            and carbs >= 0 and carbs < 'Infinity'::double precision
            and fats >= 0 and fats < 'Infinity'::double precision
            and fiber >= 0 and fiber < 'Infinity'::double precision
            and sugar >= 0 and sugar < 'Infinity'::double precision
            and trans_fat >= 0 and trans_fat < 'Infinity'::double precision
            and saturated_fat >= 0 and saturated_fat < 'Infinity'::double precision
        );

drop trigger if exists meal_logs_set_updated_at on public.meal_logs;
create trigger meal_logs_set_updated_at
before update on public.meal_logs
for each row execute function private.set_updated_at();

drop trigger if exists meal_log_items_set_updated_at on public.meal_log_items;
create trigger meal_log_items_set_updated_at
before update on public.meal_log_items
for each row execute function private.set_updated_at();
