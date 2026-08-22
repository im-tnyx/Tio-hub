-- Extend each meal item with queryable limits plus provider/serving snapshots.
-- Existing owner-scoped RLS and table-level authenticated grants remain in force.

alter table public.meal_log_items
    add column sodium double precision,
    add column cholesterol double precision,
    add column micronutrients jsonb not null default '{}'::jsonb,
    add column serving_snapshot jsonb,
    add column raw_input text,
    add column input_source text not null default 'manual',
    add column image_url text,
    add column confidence_score double precision,
    add column nutrition_snapshot jsonb;

alter table public.meal_log_items
    add constraint meal_log_items_extended_nutrients_nonnegative
        check (
            (sodium is null or (sodium >= 0 and sodium < 'Infinity'::double precision))
            and (
                cholesterol is null
                or (cholesterol >= 0 and cholesterol < 'Infinity'::double precision)
            )
        ),
    add constraint meal_log_items_confidence_score_range
        check (
            confidence_score is null
            or (confidence_score >= 0 and confidence_score <= 1)
        ),
    add constraint meal_log_items_input_source_not_blank
        check (btrim(input_source) <> ''),
    add constraint meal_log_items_micronutrients_object
        check (
            jsonb_typeof(micronutrients) = 'object'
            and micronutrients - array[
                'vitaminAMcgRae',
                'vitaminCMg',
                'vitaminDMcg',
                'vitaminEMg',
                'vitaminKMcg',
                'thiaminMg',
                'riboflavinMg',
                'niacinMg',
                'vitaminB6Mg',
                'vitaminB12Mcg',
                'folateMcg',
                'calciumMg',
                'ironMg',
                'magnesiumMg',
                'potassiumMg',
                'zincMg',
                'seleniumMcg',
                'phosphorusMg',
                'copperMg',
                'manganeseMg',
                'iodineMcg'
            ]::text[] = '{}'::jsonb
            and not jsonb_path_exists(
                micronutrients,
                '$.* ? ((@.type() != "number" && @.type() != "null") || (@.type() == "number" && @ < 0))'
            )
        ),
    add constraint meal_log_items_serving_snapshot_object
        check (
            serving_snapshot is null
            or jsonb_typeof(serving_snapshot) = 'object'
        ),
    add constraint meal_log_items_nutrition_snapshot_object
        check (
            nutrition_snapshot is null
            or jsonb_typeof(nutrition_snapshot) = 'object'
        );

comment on column public.meal_log_items.micronutrients is
    'Canonical optional vitamin/mineral amounts; units are encoded in each JSON key.';
comment on column public.meal_log_items.serving_snapshot is
    'Serving label, amount, unit, and optional gram equivalent captured when logged.';
comment on column public.meal_log_items.nutrition_snapshot is
    'Versioned provider provenance without credentials or raw provider payloads.';
