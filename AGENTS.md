# Repository guidance

## Local ExUI and RAS role alignment

The ExUI version used by CFTLib performs a legacy client-side Create Case check after CCD has authorised the
available events. It compares each event ACL role with the user's IDAM session roles and cannot see access profiles
derived from Role Assignment Service assignments.

Keep the local compatibility roles in `TecCftLibConfiguration.LA_BATCH_SUBMITTER_EXUI_ROLES` aligned with all of
the following whenever LA RAS roles or access-profile mappings change:

- non-read-only submitter assignments in `src/cftlib/resources/cftlib-am-role-assignments.json`;
- mappings in `RoleToAccessProfiles`; and
- create-event ACL access profiles in the generated CCD definition.

Only local demo users with the corresponding RAS submitter assignment should receive the shadow IDAM role. Use the
mapped create-event access-profile name, because that is the exact value ExUI compares with the event ACL. Do not
treat the shadow role as backend authorisation: RAS remains authoritative for CCD access. Remove the workaround when
the local ExUI Create Case component becomes RAS/access-profile aware or stops repeating CCD's authorisation check.
