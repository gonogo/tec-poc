# Modernised TEC Configuration



# Purpose

This document specifies the Access Management and Work Allocation configuration requirements for the TEC (Traffic Enforcement Centre) modernisation service. It is intended for the Access Management component team and focuses on the configuration required to support TEC (staff) users, Judicial users and Local Authority (professional) users.

The following elements are currently out of scope and will be addressed in a future version of this document:

- Tasks
- Work Types

# Business Design

## HLD Requirements

| # | Requirement | Significance |
| --- | --- | --- |
|  | Two types of TEC actors require access to TEC cases, "Manager" and "Clerk" |  |
|  | TEC actors are considered staff users. |  |
|  | Two types of Local Authority actors require access to TEC cases, "LA Super User" and "LA User". |  |
|  | Local Authority actors are considered professional users. | Access is via MyHMCTS. |
|  | There are no immediate use-cases for judicial access (DDJs). However, minimal case access shall be configured for future proofing. | Minimal judicial configuration required. |
|  | No tasks shall be presented to judicial users. |  |
|  | TEC actors can access all TEC cases. |  |
|  | Local Authority users can only access TEC cases belonging to the same Local Authority.  |  |

# Application and Data Design

## Common Reference Data

### Jurisdiction

The service belongs to the TBC\_JURISDICTION jurisdiction.

### Case Type

The service uses the following case types.

| Case Type Name | CCD Identifier | Description |
| --- | --- | --- |
| ? | `TBD_CCD_ID` | All TEC cases. |

### Region / Location

Single base location, no region based access control.

| ePimms ID | Description |
| --- | --- |
| 420219 | Civil National Business Centre |

### Mapping to HMCTS Service Code

| CCD Identifier | Level 4 Description | Level 5 Description | Service Code (ID) |
| --- | --- | --- | --- |
| `TBD_CCD_ID` | TBD | TBD | `TBD_HMCTS_SERVICE_CODE` |

Mapping between service codes and authorisation/lower-level reference data:

|  | ***From Judicial Office Authorisation file*** | ***From Location Reference Data*** |  |  |
| --- | --- | --- | --- | --- |
| Ticket Code | Jurisdiction | Lower Level Authorisation | Service Code | Service Description |
| 294 | Civil | Civil Authorisation | `TBD_HMCTS_SERVICE_CODE` |  |

## Judge Appointments and Roles

|  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- |
| **S.No** | **Appointment ** | **Appointment Type** | **Attribute Type** | **JOH Type** | **JO Codes** | **Mapping to Generic roles** |
|  | Chairman |  |  |  |  |  |
|  | Chancellor of the High Court |  |  |  |  |  |
|  | Chief Insolvency and Companies Court Judge | Salaried / SPTW | Appointment  | Judge  | 117 | TEC Leadership Judge Salaried |
|  | Chief Master | Salaried / SPTW | Appointment  | Judge  | 16 | TEC Leadership Judge Salaried |
|  | Circuit Judge | Salaried / SPTW | Appointment  | Judge  | 19 | TEC Salaried |
|  | Circuit Judge (sitting in retirement) | Fee-paid | Appointment  | Judge  | 124 | TEC Fee Paid |
|  | Circuit Judge, Central Criminal Court | Salaried | Appointment  | Judge  | 102 | TEC Salaried |
|  | Costs Judge | Salaried / SPTW | Appointment  | Judge  | 23 | TEC Salaried |
|  | Court of Appeal Judge | Salaried / SPTW | Appointment  | Judge  | 20 | TEC Fee Paid |
|  | Deputy Chamber President | Salaried / SPTW | Appointment  | Judge  | 32 | TEC Leadership Judge Salaried |
|  | Deputy Circuit Judge | Fee-paid | Appointment  | Judge  | 30 | TEC Fee Paid |
|  | Deputy Costs Judge | Fee-paid | Appointment  | Judge  | 31 | TEC Fee Paid |
|  | Deputy District Judge - PRFD | Fee-paid | Appointment  | Judge  | 99 | TEC Fee Paid |
|  | Deputy District Judge- Sitting in Retirement | Fee-paid | Appointment  | Judge  | 25 | TEC Fee Paid |
|  | Deputy District Judge | Fee-paid | Appointment  | Judge  | 201 | TEC Fee Paid |
|  | Deputy High Court Judge |  |  |  |  |  |
|  | Deputy Insolvency and Companies Court Judge | Fee-paid | Appointment  | Judge  | 108 | TEC Fee Paid |
|  | Deputy Insolvency and Companies Court Judge (sitting in retirement) |  |  |  |  |  |
|  | Deputy Master | Fee-paid | Appointment  | Judge  | 95 | TEC Fee Paid |
|  | District Judge | Fee-paid | Appointment  | Judge  | 45 | TEC Fee Paid |
|  | District Judge (MC) | Salaried / SPTW | Appointment  | Judge  | 46 | TEC Salaried |
|  | District Judge (sitting in retirement) | Fee-paid | Appointment  | Judge  | 125 | TEC Fee Paid |
|  | Employment Judge | Fee-paid | Appointment  | Judge  | 48 | TEC Fee Paid |
|  | High Court Judge | Salaried / SPTW | Appointment  | Judge  | 51 | TEC Salaried |
|  | High Court Judge (sitting in retirement) | Fee-paid | Appointment  | Judge  | 186 | TEC Fee Paid |
|  | Insolvency and Companies Court Judge | Salaried / SPTW | Appointment  | Judge  | 193 | TEC Salaried |
|  | Insolvency and Companies Court Judge (sitting in retirement) | Fee-paid | Appointment  | Judge  | 192 | TEC Fee Paid |
|  | Judge of the Upper Tribunal (sitting in retirement) |  |  |  |  |  |
|  | Lady Chief Justice |  |  |  |  |  |
|  | Lord Justice of Appeal (sitting in retirement) |  |  |  |  |  |
|  | Master | Salaried / SPTW | Appointment  | Judge  | 57 | TEC Salaried |
|  | Master (sitting in retirement) |  |  |  |  |  |
|  | Master of the Rolls | Salaried / SPTW | Appointment  | Judge  | 59 | TEC Leadership Judge Salaried |
|  | Recorder | Fee-paid | Appointment  | Judge  | 67 | TEC Fee Paid |
|  | Regional Employment Judge | Salaried / SPTW | Appointment  | Judge  | 71 | TEC Leadership Judge Salaried |
|  | Registrar of Criminal Appeals | Salaried / SPTW | Appointment  | Judge  | 145 | TEC Salaried |
|  | Senior Circuit Judge | Salaried / SPTW | Appointment  | Judge  | 75 | TEC Salaried |
|  | Senior Costs Judge | Salaried / SPTW | Appointment  | Judge  | 188 | TEC Salaried |
|  | Senior Master | Salaried / SPTW | Appointment  | Judge  | 80 | TEC Leadership Judge Salaried |
|  | Specialist Circuit Judge | Salaried / SPTW | Appointment  | Judge  | 82 | TEC Salaried |
|  | Tribunal Judge | Fee-paid | Appointment  | Judge  | 84 | TEC Fee Paid |

## Staff Reference Data Areas of Work

| Area of Work | CRD Label | CRD Identifier | Description |
| --- | --- | --- | --- |
| Traffic Enforcement Centre | TBD | TBD | Single area of work covering all TEC cases. |

# Staff Role Configuration

## Staff Reference Data

Within this table, "Staff Job Titles" are coarse-grained jobs which involve a range of different responsibilities. These are distinct from the more granular "roles" which will be assigned later by Role Assignment Service. "TEC Actor" is a label only relevant to the TEC team and has no further standing in this document.

  

| **TEC Actor** | **Staff Job Title (ID)** | **Status** | **Role Category** |
| --- | --- | --- | --- |
| TEC Manager | National Business Centre Team Leader (6) | Existing | ? |
| TEC Clerk | National Business Centre Administrator (11) | Existing | ? |

## Standard Staff Roles

Roles in this section correspond to granular Role Assignment Service roles catalogued [here](https://github.com/hmcts/am-role-assignment-service/tree/master/src/main/resources/roleconfig) and [here](https://hmcts.atlassian.net/wiki/pages/viewpage.action?pageId=278763072&spaceKey=AM&title=Work%2BAllocation%2BCommon%2BORG%2Band%2BCASE%2BRoles#WorkAllocationCommonORGandCASERoles-Standardroles).

The TEC access model does not require region/location based access, therefore no challenged access is considered.

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Role Name** | **Role Label ** | **Description** | **Configuration Required** | **Substantive Role** |
| hmcts-admin | HMCTS Admin | Standard Admin role to perform global search | ? | ? |
| case-allocator | Case Allocator | Has permission to allocate cases to others | ? | ? |
| task-supervisor | Task Supervisor | Has permission to allocate tasks to others | ? | ? |
| nbc-team-leader | NBC Team Leader |  | ? | ? |
| national-business-centre | NBC Admin |  | ? | ? |

## TEC Roles and Work Type Mapping

The work types attribute on a role assignment defines the work types that will be available to the user to select in the UI because of that role assignment.

A user will have multiple role assignments, and the work types available to the user will be the combination of all the work types specified in any of their role assignments.

To be addressed in a future revision of this document.

  

| Role Name | Work Types |
| --- | --- |
|  |  |

  

| ID | Work Type Filter | Description |
| --- | --- | --- |
|  |  |  |

  

| # | Role Name | **Role Label** | **Organisational Role?** | **New/Existing Role?** | **Case Role?** | **Role Category** | **work\_type** | **Substantive Role** | **Description (For Public Mapping purposes only)** | **Notes** |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  |  |  |  |  |  |

## Staff Organisational Role Mappings

| **For a staff member identified as a...** | **...create a role assignment with...** |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **RoleLabel** | **Service Code** | **Additive Role Flag** | **ActorID** | **RoleName** | **RoleCategory** | **Classification** | **GrantType** | **ReadOnly** | **Begin** | **End** | **@jurisdiction** | **@primaryLocation** | **@skillCode** | **@region** | **@worktypes** |
| National Business Centre Team Leader | `TBD_HMCTS_SERVICE_CODE` |  | #Idam\_Id | hmcts-admin | ADMIN | PRIVATE | BASIC | Y | - | - | TBC\_JURISDICTION | - | TBD | - | TBD |
|  | #Idam\_Id | national-business-centre | ADMIN | PUBLIC | STANDARD | N | - | - |   | \<primary location\> | TBD | - | TBD |  |  |
|  | #Idam\_Id | nbc-team-leader | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |
| Case Allocator = Y | #Idam\_Id | case-allocator | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |
| Task Supervisor = Y | #Idam\_Id | task-supervisor | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |
| National Business Centre Administrator | `TBD_HMCTS_SERVICE_CODE`   |  | #Idam\_Id | hmcts-admin | ADMIN | PRIVATE | BASIC | Y |  - | -  | TBC\_JURISDICTION | - | TBD | - | TBD |
|  | #Idam\_Id | national-business-centre | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |
| Case Allocator = Y | #Idam\_Id | case-allocator | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |
| Task Supervisor = Y | #Idam\_Id | task-supervisor | ADMIN | PUBLIC | STANDARD | N | - | - | TBC\_JURISDICTION | \<primary location\> | TBD | - | TBD |  |  |

## Staff Case Role Assignment Validation Rules 

This section is only relevant if we need specific case access, i.e. where a user outside TEC requires access to a TEC case. This business requirement is to be decided.

| # | **Case Type** | **The case role...** | **Case Role Label** | **...with attributes...** | **...can be assigned or removed if assignee has a role assignment with...** | **...and the requestor has a role assignment with...** | **...and...** |
| --- | --- | --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  |  |  |

# Judicial Role Configuration

## Identifying Judicial Office Holders

|  |   |   |  |  |  | and their office attributes are determined as follows |  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Rule # | A JOH who is ... | ... is identified in reference data by ... | Appointment Type | Appointment End Date | Authorisation Service Code | Jurisdiction CCD | Authorisation | Start date | End date | Region ID | Base Location | Primary Location | Business  outcome |
| 1 | Generic Fee Paid | judicial\_user\_profile.appointment\_type = "Fee Paid" | Fee-paid | Empty or \>= today's date | `TBD_HMCTS_SERVICE_CODE` | TBC\_JURISDICTION | Authorisation Civil | #Start\_date | #Last\_date + 1 | CFT Region ID |  | \<primary location\> | TEC Fee Paid |
| 2 | Generic Salaried | judicial\_user\_profile.appointment\_type = "Salaried" OR "SPTW" | Salaried / SPTW | Empty or \>= today's date | `TBD_HMCTS_SERVICE_CODE` | TBC\_JURISDICTION | Authorisation Civil | #Start\_date | #Last\_date + 1 | CFT Region ID |  | \<primary location\> | TEC Salaried |
| 5 | Generic Leadership Judge | judicial\_user\_profile.appointment = ANY OF: - Chief Insolvency and Companies Court Judge - Chief Master - Deputy Chamber President  - Master of the Rolls  - Regional Employment Judge - Senior Master  OR judicial\_user\_profile.role= "Designated Civil Judge"ORjudicial\_user\_profile.role = "Acting Designated Civil Judge"And judicial\_user\_profile.appointment\_type = "Salaried" OR "SPTW"  | Salaried / SPTW | Empty or \>= today's date | `TBD_HMCTS_SERVICE_CODE` | TBC\_JURISDICTION | Authorisation Civil | #Start\_date | #Last\_date + 1 | CFT Region ID |  | \<primary location\> | TEC Leadership Judge Salaried |

## Standard Judicial Roles

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Role Name** | **Role Label ** | **Description** | **Configuration Required** | **Substantive Role** |
| hmcts-judiciary |  |  |  |  |
| ~~fee-paid-judge~~ |  |  |  |  |
| ~~judge~~ |  |  |  |  |
| ~~leadership-judge~~ |  |  |  |  |
| ~~task-supervisor~~ |  |  |  |  |
| ~~case-allocator~~ |  |  |  |  |

## TEC Judicial Roles

TODO

## Judicial Organisation Role Mappings 

Rules for mapping judicial appointments to judicial (access management) roles.

| For a Judicial Office Holder who is... | ... and has a booking? |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|  |  | ActorIDType | ActorID | RoleType | RoleName | RoleCategory | Classification | GrantType | ReadOnly | Begin | End | Notes | @jurisdictionCCD | @region | @location | @PrimaryLocation | Authorisations | Appointment type | @bookable | @workTypes |
| TEC Fee Paid | N/A | IDAM | #Idam\_Id | ORGANISATION | hmcts-judiciary  | JUDICIAL | PRIVATE | BASIC | Y | #Start\_date | #Last\_date + 1 |  |  |  |  |  |  | FEE-PAID |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | fee-paid-judge |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| Y | IDAM | #Idam\_Id | ORGANISATION | judge |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| TEC Salaried | N/A | IDAM | #Idam\_Id | ORGANISATION | hmcts-judiciary  | JUDICIAL | PRIVATE | BASIC | Y | #Start\_date | #Last\_date + 1 |  |  |  |  |  |  | SALARIED |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | judge |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| TEC Leadership Judge Salaried | N/A | IDAM | #Idam\_Id | ORGANISATION | hmcts-judiciary  | JUDICIAL | PRIVATE | BASIC | Y | #Start\_date | #Last\_date + 1 |  |  |  |  |  |  | SALARIED |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | judge |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | leadership-judge |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | task-supervisor |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |
| N/A | IDAM | #Idam\_Id | ORGANISATION | case-allocator |  |  |  |  |   |   |  |  |  |  |  |  |  |  |  |  |

# Professional Role Configuration

This section has been assembled based on the following reference materials:

- <https://hmcts.atlassian.net/wiki/spaces/RCCD/pages/277949690/Professional+Group+Access+Onboarding>
- <https://hmcts.atlassian.net/wiki/spaces/AM/pages/278762978/HLD+-+Professional+Access+Management+v1.2>

## Organisation Type and Profiles

| Organisation Type | Organisation Profile(s) |
| --- | --- |
| LOCAL\_AUTHORITY\_ORG | LOCAL\_AUTHORITY\_PROFILE |

## Access Types

| CaseTypeId | AccessTypeID | OrganisationProfileID | AccessMandatory | AccessDefault | Display | Description | Hint | DisplayOrder | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `TBD_CCD_ID` | tec-work | LOCAL\_AUTHORITY\_PROFILE | Y | Y | N | - | - | - | Base role for all TEC users. |
| `TBD_CCD_ID` | tec-all-cases | LOCAL\_AUTHORITY\_PROFILE | N | Y | Y | Can manage all cases of this type in your organisation. |  | 1 |  |
| `TBD_CCD_ID` | tec-org-admin | LOCAL\_AUTHORITY\_PROFILE | N | N | Y | Can manage their Local Authority. | Grants capability to manage email whitelist and request new prefixes. | 2 | Management of Org's users and profile is granted by existing admin role in Manage Org. |

## Organisational Roles and Group Access

| CaseTypeID | AccessTypeID | OrganisationProfileID | OrganisationalRoleName | GroupRoleName | CaseAssignedRoleField | GroupAccessEnabled | CaseAccessGroupIDTemplate | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `TBD_CCD_ID` | tec-work | LOCAL\_AUTHORITY\_PROFILE | tec-work-user | N/A | N/A | N/A | N/A | Organisational role for regular operational access, including datafile submission and other non-group access needed by EXUI. |
| `TBD_CCD_ID` | tec-all-cases | LOCAL\_AUTHORITY\_PROFILE | N/A | local-authority-all-cases | TBC\_CASE\_ASSIGNED\_FIELD | Y | TBC\_JURISDICTION:TBD\_CCD\_ID:tec-la-user:TBC\_CASE\_ASSIGNED\_FIELD:$ORGID$ | Group access role for visibility of TEC cases. |
| `TBD_CCD_ID` | tec-org-admin | LOCAL\_AUTHORITY\_PROFILE | tec-org-admin | N/A | N/A | N/A | N/A | Organisational role for admin capabilities, sans case access. |

## Case Creation Permissions

There are no case creation use cases for Local Authority actors. Case creation will be driven by a TEC service account.
