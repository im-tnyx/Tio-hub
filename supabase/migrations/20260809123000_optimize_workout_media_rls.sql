-- Migration: Optimize Workout Media RLS
-- Slice: custom_exercise_media
-- Date: 2026-08-09

alter policy custom_exercises_owner_all
on public.custom_exercises
using (
    user_id = (select auth.uid())
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
)
with check (
    user_id = (select auth.uid())
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

alter policy tio_exercise_media_select_own
on storage.objects
using (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

alter policy tio_exercise_media_insert_own
on storage.objects
with check (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and storage.extension(name) in ('jpg', 'png', 'gif', 'mp4', 'webm', 'mov', 'm4v', '3gp')
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);

alter policy tio_exercise_media_update_own
on storage.objects
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

alter policy tio_exercise_media_delete_own
on storage.objects
using (
    bucket_id = 'tio-exercise-media'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and ((select auth.jwt())->>'is_anonymous')::boolean is false
);
