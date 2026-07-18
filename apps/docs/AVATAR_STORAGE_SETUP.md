# Avatar Storage Setup

The Android avatar flow uses the public Supabase Storage bucket `profile-avatars` and stores one JPEG object per authenticated user:

```text
<auth-user-id>/avatar.jpg
```

The app writes the resulting public URL to `public.profiles.avatar_url`. The URL includes a cache-busting query parameter after replacement so Coil refreshes every visible avatar surface.

## Required database contract

The target Supabase project must contain `public.profiles` with:

- `id uuid primary key` matching `auth.users.id`
- `avatar_url text null`
- Row Level Security enabled
- authenticated users allowed to select and update only their own row

The connected `Tnyx_flutter` project inspected on July 18, 2026 does not currently expose a `public.profiles` table, so no production schema change was applied from ChatGPT.

## Required bucket contract

Create a public bucket named `profile-avatars` with:

- maximum file size: 5 MB or lower
- allowed MIME type: `image/jpeg`
- public reads
- authenticated, owner-scoped insert/update/select/delete policies

The client converts selected images to a center-cropped JPEG with a maximum edge of 1024 px before upload.

## Policy template

Review and apply this SQL to the correct project through the repository's normal migration workflow. Do not run it against an unrelated project.

```sql
alter table public.profiles
  add column if not exists avatar_url text;

alter table public.profiles enable row level security;

create policy "profiles_select_own"
on public.profiles
for select
to authenticated
using ((select auth.uid()) = id);

create policy "profiles_update_own"
on public.profiles
for update
to authenticated
using ((select auth.uid()) = id)
with check ((select auth.uid()) = id);

insert into storage.buckets (
  id,
  name,
  public,
  file_size_limit,
  allowed_mime_types
)
values (
  'profile-avatars',
  'profile-avatars',
  true,
  5242880,
  array['image/jpeg']
)
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

create policy "avatar_insert_own_folder"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'profile-avatars'
  and (storage.foldername(name))[1] = (select auth.uid()::text)
);

create policy "avatar_select_own"
on storage.objects
for select
to authenticated
using (
  bucket_id = 'profile-avatars'
  and owner_id = (select auth.uid()::text)
);

create policy "avatar_update_own"
on storage.objects
for update
to authenticated
using (
  bucket_id = 'profile-avatars'
  and owner_id = (select auth.uid()::text)
)
with check (
  bucket_id = 'profile-avatars'
  and (storage.foldername(name))[1] = (select auth.uid()::text)
);

create policy "avatar_delete_own"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'profile-avatars'
  and owner_id = (select auth.uid()::text)
);
```

Before applying, check for policies with equivalent behavior and avoid duplicate names. Storage upsert requires INSERT, SELECT, and UPDATE permissions. Deleting requires SELECT and DELETE permissions.

## Runtime validation

1. Sign in as user A and upload an avatar.
2. Confirm the object path begins with user A's UUID.
3. Confirm `profiles.avatar_url` updates only for user A.
4. Confirm Home, You/Profile, and Personal Information refresh without restarting.
5. Replace the image and verify the new bytes display instead of the cached image.
6. Remove the image and verify all surfaces return to initials/person fallback.
7. Sign in as user B and verify user B cannot update or delete user A's object.
8. Verify non-JPEG source images are converted before upload and large images are reduced to 1024 px.

## Deferred camera support

This change uses the system gallery picker. Full-resolution camera capture should be added separately with `FileProvider`, a temporary content URI, cleanup, and rotation handling. `TakePicturePreview` should not be used for production avatars because it only returns a thumbnail.
