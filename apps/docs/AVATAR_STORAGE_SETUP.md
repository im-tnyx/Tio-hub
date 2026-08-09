# Avatar Storage Setup

Last updated: 2026-07-30

## Current Runtime

- Android now binds `ProfileRepository` to `SupabaseProfileRepository`.
- Selecting an avatar uploads `avatar.jpg` to the live `tio-profile` bucket
  under the authenticated user's object path.
- The repository updates `public.users.avatar_url` after a successful upload.
- Avatar state is no longer local-only profile truth.

## Live Storage Foundation

The connected Tio-hub Supabase project contains the public `tio-profile`
bucket:

- object pattern: `<user-id>/avatar.jpg`
- maximum object size: 5 MB
- allowed MIME type: `image/jpeg`
- owner-scoped authenticated object policies

`public.users.avatar_url` stores the image location in the current schema.
The verified object and security inventory is maintained in
[SUPABASE_SCHEMA_STATUS.md](SUPABASE_SCHEMA_STATUS.md). Executable storage
setup belongs only in timestamped files under `supabase/migrations/`; do not
copy ad-hoc SQL from this document into a project.

## Future Backend Contract

Production avatar operations will use backend APIs. The backend must:

1. Verify the client token and map it to the internal user ID.
2. Reject attempts to write another user's object path.
3. Accept only supported image types and enforce byte and dimension limits.
4. Normalize the final image to JPEG and use a deterministic object path.
5. Upload with server-controlled Supabase credentials.
6. Update `users.avatar_url` in the same owned-user operation.
7. Return a stable avatar URL or media DTO to the client.
8. Remove both the stored object and profile reference when requested.

The Android repository implementation may expose the existing
`updateAvatar(jpegBytes)` and `removeAvatar()` contract while replacing only
its data implementation with backend API calls.

## Validation Gate

Before enabling remote avatar persistence:

1. Verify signed-out requests are rejected.
2. Verify user A cannot read, replace, or delete user B's private operation
   targets.
3. Verify invalid MIME types, empty files, and oversized files are rejected.
4. Verify replacement refreshes Home, You/Profile, and Personal Information.
5. Verify removal returns all avatar surfaces to initials/person fallback.
6. Verify backend logs do not contain image bytes, credentials, or tokens.
7. Verify no Supabase service-role credential is packaged in Android.

## Deferred Camera Support

The current UI uses the system gallery picker. Full-resolution camera capture
remains a separate slice using `FileProvider`, a temporary content URI,
rotation handling, and cleanup. `TakePicturePreview` should not be used for a
production avatar because it returns only a thumbnail.
