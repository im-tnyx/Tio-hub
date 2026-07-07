-- Create profiles table
create table if not exists public.profiles (
    id text primary key,
    display_name text,
    dob date,
    gender text,
    plan_label text default 'Premium',
    weight double precision,
    height integer,
    bmi double precision,
    bmr integer,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Insert dummy/seed user profile
insert into public.profiles (id, display_name, dob, gender, plan_label, weight, height, bmi, bmr)
values (
    'demo-user',
    'Santosh Jangid',
    '1991-07-07',
    'male',
    'Premium',
    78.4,
    182,
    23.6,
    1840
)
on conflict (id) do update set
    display_name = excluded.display_name,
    dob = excluded.dob,
    gender = excluded.gender,
    plan_label = excluded.plan_label,
    weight = excluded.weight,
    height = excluded.height,
    bmi = excluded.bmi,
    bmr = excluded.bmr;
