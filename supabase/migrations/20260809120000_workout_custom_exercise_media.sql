-- Migration: Workout Custom Exercise Media
-- Slice: custom_exercise_media
-- Date: 2026-08-09

drop policy if exists custom_exercises_owner_all
on public.custom_exercises;

create policy custom_exercises_owner_all
on public.custom_exercises
for all
to authenticated
using (
    user_id = (select auth.uid())
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
)
with check (
    user_id = (select auth.uid())
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

insert into storage.buckets (
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
)
values (
    'tio-exercise-media',
    'tio-exercise-media',
    false,
    52428800,
    array[
        'image/jpeg',
        'image/png',
        'image/gif',
        'video/mp4',
        'video/webm',
        'video/quicktime',
        'video/x-m4v',
        'video/3gpp'
    ]
)
on conflict (id) do update
set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists tio_exercise_media_select_own
on storage.objects;

drop policy if exists tio_exercise_media_insert_own
on storage.objects;

drop policy if exists tio_exercise_media_update_own
on storage.objects;

drop policy if exists tio_exercise_media_delete_own
on storage.objects;

create policy tio_exercise_media_select_own
on storage.objects
for select
to authenticated
using (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

create policy tio_exercise_media_insert_own
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and storage.extension(name) in ('jpg', 'png', 'gif', 'mp4', 'webm', 'mov', 'm4v', '3gp')
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

create policy tio_exercise_media_update_own
on storage.objects
for update
to authenticated
using (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
)
with check (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and storage.extension(name) in ('jpg', 'png', 'gif', 'mp4', 'webm', 'mov', 'm4v', '3gp')
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

create policy tio_exercise_media_delete_own
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);
