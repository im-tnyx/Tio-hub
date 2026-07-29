-- Remote migration version: 20260729151026
create policy auth_identities_deny_direct_access
on public.auth_identities
for all
to authenticated
using (false)
with check (false);
