# TEC_BATCH_DATAFILE: Local Authority access specification

Status: business rules agreed in discussion; proposed CCD configuration contract for implementation. The project has been upgraded to SDK 7.3.0, but this access policy has not been implemented or imported. Prepared 18 September 2026.

## Scope and agreed rules

This specification covers LA users of `TEC_BATCH_DATAFILE` in jurisdiction `TEC`. The [Modernised TEC Configuration draft](Modernised%20TEC%20Configuration.md) does not currently cover this case type. Its roles and access types are possible reuse candidates, not requirements for this design.

The following rules are agreed:

- Each LA user belongs to exactly one LA.
- Creation and visibility are always granted together as one business entitlement.
- An LA user can create a batch case through `submitRegistrationDatafile`, `submitWarrantDatafile` or `submitWarrantReissueDatafile`.
- Every case belongs to exactly one LA, independently of who creates it. Cases created programmatically also identify their owning LA.
- An LA user can read all attributes of every batch case belonging to their LA, including cases created by colleagues or the service account, in every processing state.
- LA users cannot trigger any subsequent event, change an existing case or delete it. Creation necessarily permits supplying the initial event inputs.

Staff, judicial and service-account permissions are outside this specification, except for the ownership contract that service-account creation must satisfy. No case-transfer operation is introduced.

The design follows [Map incoming roles to access profiles](patterns/case-roles-and-access-profiles.md) and [Add a Case File tab](patterns/case-file-tab.md). Identifiers and technical choices below are proposed defaults. Confirmation that these identifiers and assignment attributes are supported by the receiving AM/PRD services is an integration dependency, not an unresolved business permission rule.

## Proposed incoming-role contract

Use one entitlement, `tec-batch-access`, which provisions both of these assignments:

| Incoming RAS role | Required applicability | CCD target profile | Responsibility |
| --- | --- | --- | --- |
| `tec-batch-submitter` | Entitled LA user; jurisdiction `TEC`; case type `TEC_BATCH_DATAFILE`; applicable before a case exists | `tec-batch-create` | Start and submit the three creation events |
| `tec-batch-reader` | Same user and case type; matched to the access group for that user's LA | `tec-batch-read` | Read the LA's existing cases |

Both incoming mapping keys are unprefixed RAS names. Neither is an IDAM role or a bracketed case role. The reader assignment must carry the matching `caseAccessGroupId`; an unscoped assignment with the same name does not fulfil the contract. The submitter assignment must remain usable during case creation, before group data exists on a case.

The two assignments are provisioned and revoked together. They must not appear as independently selectable capabilities. Partial provisioning is a fault, not a supported user persona. Actual assignment creation, eligibility and validity dates belong to the professional access/AM integration. The precise RAS grant type, classification and attribute payload must be agreed and tested against the receiving environment; these are not fabricated here from the unrelated draft.

### Role reuse

The proposed names keep the scope explicit. Reuse `tec-work-user` only if its agreed contract includes batch submission for exactly the intended users. Reuse `local-authority-all-cases` only if its assignment scope and group identifiers support this batch case type and its intended readers. A similar label is insufficient evidence of equivalent scope. Reusing an incoming role would not require reusing another case type's permission profile.

The current `caseworker-tec-la-user` combines capabilities without the proposed organisation boundary. Do not keep it as an unrestricted parallel route when the new model is enabled. Local test users should exercise the intended assignments rather than receive a legacy bypass.

## Access-type and group metadata

The intended CCD output consists of one `AccessType` row and two `AccessTypeRole` rows for that access type. This is one business entitlement associated with two technical roles. The newer generator inspected alongside the pattern supports distinct role rows sharing an access-type/profile pair; acceptance by the target definition store and provisioning of both assignments still require integration verification.

`LOCAL_AUTHORITY_PROFILE` is a **candidate PRD organisation-profile identifier**, reused provisionally from the draft. Confirm the actual catalogue ID before import. It identifies a kind of organisation, not a specific LA. Each LA's actual organisation ID is substituted into the group template at runtime.

All rows use `CaseTypeID=TEC_BATCH_DATAFILE`. Use the definition's common `LiveFrom` metadata. Proposed `LiveTo` for access-type rows is `01/01/2099`, matching the documented PCS convention: use an explicit valid date and manage its eventual expiry, rather than assuming a blank value is accepted. It is catalogue validity, not an individual user's assignment expiry.

### `AccessType.json`

| Column | Proposed value |
| --- | --- |
| `AccessTypeID` | `tec-batch-access` |
| `OrganisationProfileID` | `LOCAL_AUTHORITY_PROFILE` — confirm with PRD |
| `AccessMandatory` | `Yes` |
| `AccessDefault` | `Yes` |
| `Display` | `No` |
| `Description` | `Submit batch datafiles and view your local authority's batch cases.` |
| `HintText` | Empty |
| `DisplayOrder` | `1` |
| `LiveTo` | `01/01/2099` |

Mandatory/default/hidden is the proposed presentation of the entitlement for the relevant LA users. It must not inadvertently enrol unrelated professional users if the organisation profile has a wider membership than the intended TEC population. Configure eligibility upstream as required. These flags alone do not prove both role assignments are provisioned.

### `AccessTypeRole.json`

| Column | Submission row | Reading row |
| --- | --- | --- |
| `AccessTypeID` | `tec-batch-access` | `tec-batch-access` |
| `OrganisationProfileID` | `LOCAL_AUTHORITY_PROFILE` | `LOCAL_AUTHORITY_PROFILE` |
| `OrganisationalRoleName` | `tec-batch-submitter` | Empty |
| `GroupRoleName` | Empty | `tec-batch-reader` |
| `GroupAccessEnabled` | `No` | `Yes` |
| `CaseAssignedRoleField` | Empty | `tec-batch-reader` |
| `CaseAccessGroupIDTemplate` | Empty | `TEC:TEC_BATCH_DATAFILE:tec-batch-access:tec-batch-reader:$ORGID$` |
| `LiveTo` | `01/01/2099` | `01/01/2099` |

Despite its name, `CaseAssignedRoleField` is a **role-name value**, not a Java field path. It must match the case's `OrganisationPolicy.OrgPolicyCaseAssignedRole` value and an incoming `RoleToAccessProfiles.RoleName`. Using the unbracketed `tec-batch-reader` follows the local pattern's organisation-group example; this design therefore introduces no bracketed role.

### `RoleToAccessProfiles.json`

| `RoleName` | `AccessProfiles` | `ReadOnly` | `Disabled` | `Authorisation` | `CaseAccessCategories` |
| --- | --- | --- | --- | --- | --- |
| `tec-batch-submitter` | `tec-batch-create` | `N` | `N` | Empty | Empty |
| `tec-batch-reader` | `tec-batch-read` | `Y` | `N` | Empty | Empty |

Empty filters are deliberate proposals: this design relies on the incoming assignments' case-type applicability and the reader's organisation-group scope, rather than judicial authorisations or case-access categories. Confirm that upstream assignment qualification supplies that scope. `ReadOnly=N` on the submitter mapping permits creation; it is not a grant to update existing cases. Permissions are supplied separately below.

`CaseRoles.json` remains `[]` for this LA design. Do not add `[tec-batch-reader]`, `[CREATOR]` or a role per LA. If target-platform validation establishes a need for a bracketed role, revise the contract and corresponding mapping together rather than silently changing the identifier.

## Permission profile specification

The following is the proposed minimum authorisation output. It must be verified through the target CCD and ExUI creation/read paths before deployment. `CR` means create/read; `R` means read. A dash means no grant for that profile, not an explicit deny that overrides other grants.

| Resource | `tec-batch-create` | `tec-batch-read` |
| --- | --- | --- |
| Case type `TEC_BATCH_DATAFILE` | `CR` | `R` |
| State `AWAITING_PROCESSING` | — | `R` |
| State `PROCESSING` | — | `R` |
| State `COMPLETE` | — | `R` |
| State `PROCESSING_FAILED` | — | `R` |
| Event `submitRegistrationDatafile` | `CR` | `R` for history only |
| Event `submitWarrantDatafile` | `CR` | `R` for history only |
| Event `submitWarrantReissueDatafile` | `CR` | `R` for history only |
| Event `startProcessing` | — | `R` for history only |
| Event `recordProcessingSuccess` | — | `R` for history only |
| Event `recordProcessingFailure` | — | `R` for history only |
| Event `retryProcessing` | — | `R` for history only |
| Field `batchFile` | `CR` for creation inputs | `R` |
| Creation-page label `validationSuccessful` | `CR` if required by SDK label generation | `R` |
| Every other case attribute, including nested/collection values | — | `R` |
| `caseHistory` viewer and `caseFileView` launcher | — | `R` |
| Ownership policy and `CaseAccessGroups` | — | `R` |

No LA profile receives `U` or `D`. Event `C` permits initiating an event; event `R` for history does not permit invoking it. Keeping event-history visibility is the proposed interpretation of a complete read-only case view. Implement it with the history-only configuration mechanism and verify the emitted rows.

Case-type `R` on the creation profile supports definition/creation response handling; it must not be accompanied by existing-state read grants. The reader profile supplies those grants only after its group-scoped assignment is applicable. Verify that the platform applies scope before combining profile permissions. If ExUI requires additional bootstrap permissions, add only evidenced requirements and rerun cross-LA denial checks; do not solve this by giving the submitter profile read access to all states.

The current readable business fields are `caseState`, `batchFile`, `fileIdentifier`, `batchIdentifier`, `localAuthority`, `submitterEmail`, `batchType`, `numberOfBatches`, `numberOfPcns`, `numberOfPcnsProcessed`, `feesDue`, `receivedVia`, `receivedAt`, `emailReceivedAt` and `generatedDocuments`. New attributes must also be reviewed against the agreed all-attributes read policy. `batchType` remains derived from the initiating event; ownership remains derived from trusted context. Neither becomes a freely writable user input because the user can create a case.

The SDK currently derives `CRU` for `batchFile` from submission grants and injects `CRU` for the history viewer for some roles. The proposed output deliberately narrows these. Author the SDK configuration to produce the matrix and inspect the generated result; copying today's `.grant(Permission.CRU, ...)` calls will not satisfy it. Field grants are not intrinsically conditional on whether a case is being created: the absence of update grants and subsequent executable events must enforce post-creation immutability.

## LA ownership and case-access data

Proposed authoritative business field: `owningLocalAuthorityOrganisationId`. Persist it when the case is created. The existing `localAuthority` display field is not a trusted ownership key and is currently not persisted by the mapper.

Expose a top-level `owningLocalAuthorityPolicy` of CCD type `OrganisationPolicy`, with:

```json
{
  "Organisation": {
    "OrganisationID": "LA_A",
    "OrganisationName": "Example Local Authority A"
  },
  "OrgPolicyCaseAssignedRole": "tec-batch-reader"
}
```

`LA_A` is illustrative; use the canonical organisation identifier supplied by the professional organisation service. Keep the field top-level to match the inspected data-store organisation-policy lookup.

The corresponding CCD case data must include the platform's exact `CaseAccessGroups` property, containing:

```json
[
  {
    "id": "a-stable-collection-item-id",
    "value": {
      "caseAccessGroupType": "CCD:all-cases-access",
      "caseAccessGroupId": "TEC:TEC_BATCH_DATAFILE:tec-batch-access:tec-batch-reader:LA_A"
    }
  }
]
```

The actual collection item ID must be valid for the platform and stable across reads. Group IDs must match the reader assignment byte-for-byte. There is one LA reader group on each batch case, regardless of the number of users in that LA. The group is not tied to the creator's user ID.

For this decentralised application, persist the owner and consistently derive the policy/group projection from it. Confirm when the CCD runtime derives groups from the policy versus when the decentralised view must supply them; do not assume the normal centralised persistence path populates them here. The agreed contract requires the correct group to be available whenever access is evaluated, including the first read and search/indexing paths.

Creation rules:

1. For an LA user, resolve the user's single LA through trusted membership data. Reject missing or ambiguous membership. Ignore or reject caller-supplied attempts to select another owning LA or inject groups.
2. For service-account creation, require a valid owning LA in the trusted processing context. Do not infer ownership from the service account's identity.
3. Validate and persist ownership as part of creation, and expose matching access metadata before making the case available. Do not leave a successfully created batch without its LA association.
4. Do not expose ownership/group data as editable creation inputs to LA users. Backend enrichment must occur through a trusted creation/callback path; its placement must be checked against CCD field-permission validation.
5. Preserve owner and group membership throughout processing. Ownership transfer is outside this design.

Ownership validation, persistence and projection are necessary application work alongside static CCD configuration. The current mapper saves only the case reference, batch type and document URL/name, so this contract is not yet implemented.

## Presentation and documents

Bind the Datafile Details, History and Case file views to the intended reader profile. Align launcher, document-field and nested-field read grants with the tab configuration; tab visibility alone does not enforce access. The submitted file and generated documents should be readable for the same LA audience, subject to matching document-store authorisation. No post-creation upload, replace, remove or other action is introduced.

Search and work-basket results must observe the same LA group boundary as direct case reads. Providing a Local authority search filter does not enforce that boundary. Do not grant access through a creator-only exception, a broad legacy role or an unrestricted submission profile to compensate for missing group data.

## Implementation pattern and version constraints

Follow the local pattern's separation of `AccessProfile`, incoming `UserRole`, source formatting (`RoleType`/`ExternalUserRole`) and `RoleToAccessProfiles`. Preserve `BatchDatafileCase` / `BatchDatafileCaseState`. SDK 7.3.0 accepts any `HasRole` as a mapping input, so the mapping component can retain `AccessProfile` as its configuration role type while passing separately defined incoming roles. Prefer that consistent generic type to avoid resolving the wrapper as the case's role enum.

Because incoming names and target profiles differ here, use explicit `AccessTypeRole` definitions and explicit role-to-profile mappings. The pattern's convenience attachment derives `GroupRoleName` from the attaching profile's name; attaching a group directly to `tec-batch-read` would silently choose a different group role from the proposed `tec-batch-reader`. Do not accept an additional automatically derived same-name mapping as a replacement.

The project now declares SDK **7.3.0**, verified as the latest release in the [HMCTS Maven plugin metadata](https://pkgs.dev.azure.com/hmcts/Artifacts/_packaging/hmcts-lib/maven/v1/hmcts/ccd/sdk/hmcts.ccd.sdk.gradle.plugin/maven-metadata.xml) on 18 September 2026. The published sources include `CCDAccessGroup`, `AccessTypeGenerator`, `AccessTypeRoleGenerator`, explicit access-type builders, `OrganisationPolicy` and `CaseAccessGroup`. The earlier SDK 6.32.0 generation limitation is resolved.

Use the SDK's native access-type and role-mapping APIs for this implementation; a separate JSON overlay is no longer needed solely to compensate for missing generator APIs. The existing CFTLib dependency remains at 0.19.2277. Definition-store import, assignment provisioning and decentralised group enforcement for the proposed policy still need integration verification.

The build now declares `decentralised-runtime` explicitly as an application dependency and `ccd-runtime-indexing` only as a CFTLib dependency, with versions supplied by the SDK BOM. This replaces deprecated `ccd.decentralised` / `ccd.runtimeIndexing` flags while preserving the runtime dependency scope.

Upgrade verification: application and CFTLib compilation, `bootJar`, the unit test and all three integration tests passed. Fresh definition generation produced the same 37 JSON files with identical parsed contents as the 6.32.0 baseline. Docker-backed tests required temporary test JVM settings `api.version=1.44` for Docker 29 and `spring.main.allow-bean-definition-overriding=true`, matching the existing local runtime's bean-override setting. Without the latter, the OpenAPI test's duplicate Feign bean failure was also reproduced on 6.32.0 in an isolated baseline copy. Repository test settings were not changed. This verifies the existing application, not the proposed LA group-access policy or a full CFTLib/ExUI session.

Additional 7.3.0 details to account for:

- Mapping targets default to the incoming role's name. `.accessProfiles(...)` replaces that list; supply both of this design's explicit target mappings rather than relying on defaults.
- The mapping `ReadOnly` flag is independent of CRUD and the assignment's read-only attribute.
- Role mapping and authorisation generators use `AddMissing` merging. Verify changed definitions in fresh generated output so obsolete legacy grants cannot survive unnoticed.
- Event configuration contributes field/state grants, and history defaults can add permissions. Test the final authorisation rows, not just enum values.
- The locally cached data-store group matcher is conditional on `enable-case-group-access-filtering=true`. Confirm group enforcement in the target environment, including search; a correct definition alone does not enable that feature.

## Completion criteria and integration dependencies

| Check | Expected result |
| --- | --- |
| Definition output | One batch entitlement, two associated role rows, two explicit LA role-to-profile mappings, no new bracketed roles; agreed ACL matrix emitted without leftover legacy grants |
| Provisioning | An eligible LA user receives both assignments for the same LA; removal revokes both; neither capability is independently selectable |
| Each initiating event | User can complete all three flows, including validation pages and initial upload; each creates a new case owned by the user's LA |
| Own-LA sharing | A second user from LA A can find and read the case, every field and its documents in all four states |
| System-created case | A case created for LA A by the service account has the same visibility as an LA-user-created case |
| Cross-LA access | LA B cannot discover/read LA A's case or documents through search, direct reference, API or legacy access paths |
| No subsequent mutation | All four later events are denied to the LA user; changes to existing attributes, documents and ownership are denied; deletion is denied |
| Creation input tampering | Supplying LA B ownership, arbitrary groups or invalid organisation data cannot create/share a case on LA B's behalf |
| Group derivation | Case policy, projected `CaseAccessGroups` and user assignment group ID agree, including immediately after creation and after processing transitions |
| Membership removal | Revoked users lose group visibility even when they originally created the case; cached/indexed paths do not retain access |

The business specification is sufficient. Remaining integration work is to confirm the PRD profile identifier, accept the proposed incoming role names and complete assignment attributes, verify one entitlement produces both assignments, verify the target definition-store/runtime group-access path, and verify the exact creation/bootstrap ACLs with ExUI. These are concrete technical dependencies; no further LA permission choices are required to start implementation.

## Evidence and verification limits

- [Local access-profile pattern](patterns/case-roles-and-access-profiles.md): role/profile separation, access-type/group metadata and organisation-policy matching.
- [Local Case File pattern](patterns/case-file-tab.md): separate tab, launcher and document permissions.
- [Current case configuration](../src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/batchdatafile/BatchDatafileCaseConfiguration.java), [case model](../src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/batchdatafile/BatchDatafileCase.java) and [mapper](../src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/batchdatafile/BatchDatafileCaseMapper.java): current events, fields and absent persisted ownership.
- Published `com.github.hmcts:ccd-config-generator:7.3.0` sources: `CaseRoleToAccessProfile`, `ConfigBuilder`, `CCDAccessGroup`, `AccessTypeGenerator`, `AccessTypeRoleGenerator` and `CaseAccessGroup`. The original permission-generation analysis also inspected the former 6.32.0 sources; fresh 7.3.0 generation produced the same 37 JSON files, with identical parsed contents, for the current application configuration.
- Cached `com.github.hmcts.rse-cft-lib:ccd-data-store-api:0.19.2277` sources: `AuthorisedCreateCaseOperation` checks create permission on case type/event/submitted fields and read permission when returning created data; `CaseAccessGroupUtils` derives groups from organisation policies; `CaseAccessGroupsMatcher` matches group IDs and requires collection item IDs.
- Newer SDK sources referenced by the pattern: `AccessTypeGenerator`, `AccessTypeRoleGenerator`, `CCDAccessGroup` and `ConfigBuilderImpl`; PCS `GroupAccessType` and `CaseAccessGroupsUtil` provide template, validity-date and projection examples.

This specification was checked against local source and existing generated output. The proposed metadata has not been imported, assignments have not been provisioned, and runtime access tests have not been performed. The cached definition-store source archive contains no implementation sources, so import/provisioning behaviour has not been verified from that artifact.
