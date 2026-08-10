-- Cover parent ownership checks and composite cascade lookups.

create index meal_log_items_meal_log_owner_idx
    on public.meal_log_items (meal_log_id, user_id);
