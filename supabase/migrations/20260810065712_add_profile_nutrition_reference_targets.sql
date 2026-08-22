-- Resolve versioned micronutrient reference targets from the signed-in user's
-- current age and an explicit male/female nutrition reference sex.

alter table public.user_nutrition_profiles
    add column nutrition_reference_sex text,
    add column nutrition_reference_sex_source text not null default 'profile',
    add column micronutrient_target_overrides jsonb not null default '{}'::jsonb;

alter table public.user_nutrition_profiles
    add constraint user_nutrition_reference_sex_check
        check (nutrition_reference_sex is null or nutrition_reference_sex in ('male', 'female')),
    add constraint user_nutrition_reference_sex_source_check
        check (nutrition_reference_sex_source in ('profile', 'custom')),
    add constraint user_nutrition_target_overrides_check
        check (
            jsonb_typeof(micronutrient_target_overrides) = 'object'
            and micronutrient_target_overrides - array[
                'vitaminAMcgRae', 'vitaminCMg', 'vitaminDMcg', 'vitaminEMg',
                'vitaminKMcg', 'thiaminMg', 'riboflavinMg', 'niacinMg',
                'vitaminB6Mg', 'vitaminB12Mcg', 'folateMcg', 'calciumMg',
                'ironMg', 'magnesiumMg', 'potassiumMg', 'zincMg',
                'seleniumMcg', 'phosphorusMg', 'copperMg', 'manganeseMg',
                'iodineMcg'
            ]::text[] = '{}'::jsonb
            and not jsonb_path_exists(
                micronutrient_target_overrides,
                '$.* ? (@.type() != "number" || @ <= 0)'
            )
        );

create table private.nutrition_reference_targets (
    version integer not null,
    reference_sex text not null,
    age_min integer not null,
    age_max integer,
    targets jsonb not null,
    sodium_limit_mg double precision not null,
    source_url text not null,
    primary key (version, reference_sex, age_min),
    constraint nutrition_reference_sex_check
        check (reference_sex in ('male', 'female')),
    constraint nutrition_reference_age_check
        check (age_min >= 9 and (age_max is null or age_max >= age_min)),
    constraint nutrition_reference_sodium_check
        check (sodium_limit_mg > 0),
    constraint nutrition_reference_targets_check
        check (
            jsonb_typeof(targets) = 'object'
            and targets ?& array[
                'vitaminAMcgRae', 'vitaminCMg', 'vitaminDMcg', 'vitaminEMg',
                'vitaminKMcg', 'thiaminMg', 'riboflavinMg', 'niacinMg',
                'vitaminB6Mg', 'vitaminB12Mcg', 'folateMcg', 'calciumMg',
                'ironMg', 'magnesiumMg', 'potassiumMg', 'zincMg',
                'seleniumMcg', 'phosphorusMg', 'copperMg', 'manganeseMg',
                'iodineMcg'
            ]::text[]
            and targets - array[
                'vitaminAMcgRae', 'vitaminCMg', 'vitaminDMcg', 'vitaminEMg',
                'vitaminKMcg', 'thiaminMg', 'riboflavinMg', 'niacinMg',
                'vitaminB6Mg', 'vitaminB12Mcg', 'folateMcg', 'calciumMg',
                'ironMg', 'magnesiumMg', 'potassiumMg', 'zincMg',
                'seleniumMcg', 'phosphorusMg', 'copperMg', 'manganeseMg',
                'iodineMcg'
            ]::text[] = '{}'::jsonb
            and not jsonb_path_exists(targets, '$.* ? (@.type() != "number" || @ <= 0)')
        )
);

revoke all on table private.nutrition_reference_targets from public, anon, authenticated;
grant select on table private.nutrition_reference_targets to service_role;

insert into private.nutrition_reference_targets (
    version, reference_sex, age_min, age_max, targets, sodium_limit_mg, source_url
)
values
    (1, 'male', 9, 13, '{"vitaminAMcgRae":600,"vitaminCMg":45,"vitaminDMcg":15,"vitaminEMg":11,"vitaminKMcg":60,"thiaminMg":0.9,"riboflavinMg":0.9,"niacinMg":12,"vitaminB6Mg":1,"vitaminB12Mcg":1.8,"folateMcg":300,"calciumMg":1300,"ironMg":8,"magnesiumMg":240,"potassiumMg":2500,"zincMg":8,"seleniumMcg":40,"phosphorusMg":1250,"copperMg":0.7,"manganeseMg":1.9,"iodineMcg":120}', 1800, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'male', 14, 18, '{"vitaminAMcgRae":900,"vitaminCMg":75,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":75,"thiaminMg":1.2,"riboflavinMg":1.3,"niacinMg":16,"vitaminB6Mg":1.3,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1300,"ironMg":11,"magnesiumMg":410,"potassiumMg":3000,"zincMg":11,"seleniumMcg":55,"phosphorusMg":1250,"copperMg":0.89,"manganeseMg":2.2,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'male', 19, 30, '{"vitaminAMcgRae":900,"vitaminCMg":90,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":120,"thiaminMg":1.2,"riboflavinMg":1.3,"niacinMg":16,"vitaminB6Mg":1.3,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1000,"ironMg":8,"magnesiumMg":400,"potassiumMg":3400,"zincMg":11,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":2.3,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'male', 31, 50, '{"vitaminAMcgRae":900,"vitaminCMg":90,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":120,"thiaminMg":1.2,"riboflavinMg":1.3,"niacinMg":16,"vitaminB6Mg":1.3,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1000,"ironMg":8,"magnesiumMg":420,"potassiumMg":3400,"zincMg":11,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":2.3,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'male', 51, 70, '{"vitaminAMcgRae":900,"vitaminCMg":90,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":120,"thiaminMg":1.2,"riboflavinMg":1.3,"niacinMg":16,"vitaminB6Mg":1.7,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1000,"ironMg":8,"magnesiumMg":420,"potassiumMg":3400,"zincMg":11,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":2.3,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'male', 71, null, '{"vitaminAMcgRae":900,"vitaminCMg":90,"vitaminDMcg":20,"vitaminEMg":15,"vitaminKMcg":120,"thiaminMg":1.2,"riboflavinMg":1.3,"niacinMg":16,"vitaminB6Mg":1.7,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1200,"ironMg":8,"magnesiumMg":420,"potassiumMg":3400,"zincMg":11,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":2.3,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 9, 13, '{"vitaminAMcgRae":600,"vitaminCMg":45,"vitaminDMcg":15,"vitaminEMg":11,"vitaminKMcg":60,"thiaminMg":0.9,"riboflavinMg":0.9,"niacinMg":12,"vitaminB6Mg":1,"vitaminB12Mcg":1.8,"folateMcg":300,"calciumMg":1300,"ironMg":8,"magnesiumMg":240,"potassiumMg":2300,"zincMg":8,"seleniumMcg":40,"phosphorusMg":1250,"copperMg":0.7,"manganeseMg":1.6,"iodineMcg":120}', 1800, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 14, 18, '{"vitaminAMcgRae":700,"vitaminCMg":65,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":75,"thiaminMg":1,"riboflavinMg":1,"niacinMg":14,"vitaminB6Mg":1.2,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1300,"ironMg":15,"magnesiumMg":360,"potassiumMg":2300,"zincMg":9,"seleniumMcg":55,"phosphorusMg":1250,"copperMg":0.89,"manganeseMg":1.6,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 19, 30, '{"vitaminAMcgRae":700,"vitaminCMg":75,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":90,"thiaminMg":1.1,"riboflavinMg":1.1,"niacinMg":14,"vitaminB6Mg":1.3,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1000,"ironMg":18,"magnesiumMg":310,"potassiumMg":2600,"zincMg":8,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":1.8,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 31, 50, '{"vitaminAMcgRae":700,"vitaminCMg":75,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":90,"thiaminMg":1.1,"riboflavinMg":1.1,"niacinMg":14,"vitaminB6Mg":1.3,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1000,"ironMg":18,"magnesiumMg":320,"potassiumMg":2600,"zincMg":8,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":1.8,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 51, 70, '{"vitaminAMcgRae":700,"vitaminCMg":75,"vitaminDMcg":15,"vitaminEMg":15,"vitaminKMcg":90,"thiaminMg":1.1,"riboflavinMg":1.1,"niacinMg":14,"vitaminB6Mg":1.5,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1200,"ironMg":8,"magnesiumMg":320,"potassiumMg":2600,"zincMg":8,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":1.8,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28'),
    (1, 'female', 71, null, '{"vitaminAMcgRae":700,"vitaminCMg":75,"vitaminDMcg":20,"vitaminEMg":15,"vitaminKMcg":90,"thiaminMg":1.1,"riboflavinMg":1.1,"niacinMg":14,"vitaminB6Mg":1.5,"vitaminB12Mcg":2.4,"folateMcg":400,"calciumMg":1200,"ironMg":8,"magnesiumMg":320,"potassiumMg":2600,"zincMg":8,"seleniumMcg":55,"phosphorusMg":700,"copperMg":0.9,"manganeseMg":1.8,"iodineMcg":150}', 2300, 'https://www.nationalacademies.org/read/25353/chapter/28');

create or replace function public.get_my_nutrition_reference_targets()
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_dob date;
    v_profile_gender text;
    v_custom_sex text;
    v_sex_source text;
    v_overrides jsonb := '{}'::jsonb;
    v_reference_sex text;
    v_age integer;
    v_catalog private.nutrition_reference_targets%rowtype;
begin
    if v_user_id is null then
        raise exception 'Authentication required' using errcode = '42501';
    end if;

    select u.dob, lower(u.gender), p.nutrition_reference_sex,
           p.nutrition_reference_sex_source, p.micronutrient_target_overrides
    into v_dob, v_profile_gender, v_custom_sex, v_sex_source, v_overrides
    from public.users as u
    left join public.user_nutrition_profiles as p on p.user_id = u.id
    where u.id = v_user_id;

    if not found or v_dob is null then
        return jsonb_build_object('status', 'profile_required');
    end if;

    v_reference_sex := case
        when v_sex_source = 'custom' then v_custom_sex
        when v_profile_gender in ('male', 'female') then v_profile_gender
        else null
    end;
    if v_reference_sex is null then
        return jsonb_build_object('status', 'profile_required');
    end if;

    v_age := extract(year from age(current_date, v_dob))::integer;
    select catalog.*
    into v_catalog
    from private.nutrition_reference_targets as catalog
    where catalog.version = 1
      and catalog.reference_sex = v_reference_sex
      and v_age >= catalog.age_min
      and (catalog.age_max is null or v_age <= catalog.age_max)
    order by catalog.age_min desc
    limit 1;

    if not found then
        return jsonb_build_object(
            'status', 'unsupported_age',
            'age', v_age,
            'referenceSex', v_reference_sex
        );
    end if;

    return jsonb_build_object(
        'status', 'resolved',
        'age', v_age,
        'referenceSex', v_reference_sex,
        'referenceVersion', v_catalog.version,
        'sourceUrl', v_catalog.source_url,
        'sodiumLimitMg', v_catalog.sodium_limit_mg,
        'targets', v_catalog.targets || coalesce(v_overrides, '{}'::jsonb)
    );
end;
$$;

revoke all on function public.get_my_nutrition_reference_targets() from public, anon;
grant execute on function public.get_my_nutrition_reference_targets() to authenticated, service_role;

comment on function public.get_my_nutrition_reference_targets() is
    'Returns versioned age/sex reference targets plus owner overrides for auth.uid().';
