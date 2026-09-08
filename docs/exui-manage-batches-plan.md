# ExUI “Create batch” primary navigation

Architect proposal: add a role-gated **Create batch** item to Manage Cases primary navigation
by changing ExUI `menuConfigs`, with a local proxy path for the TEC prototype and a later
platform release for shared environments.

Batches themselves are modelled as CCD case type `TEC_BATCH` and browsed from Case list / Find
case (case type filter). Create batch is the clerk-visible `uploadBatch` CCD event, linked from
primary nav via an ExUI case-create deep link.

## Problem

TEC clerks need a **global** Create batch entry point from Manage Cases (not case-scoped).
CCD tabs and case events cannot own this nav item. Primary navigation is owned by **ExUI**
(`rpx-xui-webapp`).

## Recommended solution

Add a **Create batch** item to ExUI’s baked-in menu configuration
(`api/configuration/menuConfigs/`), gated to TEC IdAM roles, pointing at the create-batch URL.

```mermaid
flowchart LR
  user[TEC_clerk] --> xui[Manage_Cases]
  xui -->|"GET /external/config/ui/"| menu[menuConfigs]
  menu --> nav[Primary_nav]
  nav -->|"Create batch"| create[Create_batch_journey]
  xui -->|"Case list case type TEC Batch"| batches[TEC_BATCH_cases]
```

### How ExUI nav actually works

- Browser loads `headerConfig` from `/external/config/ui/`.
- Server builds that from `setupMenuConfig(environment)` →
  [base-config.ts](https://github.com/hmcts/rpx-xui-webapp/blob/master/api/configuration/menuConfigs/base-config.ts)
  (+ AAT diffs), **not** from a live `HEADER_CONFIG` env var for the UI payload.
- `HeaderConfigService` picks the first role-regex key that matches the user (else `.+`).
- Individual items can further gate on `roles` / LaunchDarkly `flags`.

The durable change is a **code change (and release) of ExUI menuConfigs**.

Menu shape and JSON fragment for discussion:
[exui-navigation.md](./exui-navigation.md) and
[exui-header-config.example.json](./exui-header-config.example.json).

### Proposed menu shape for TEC

Add a dedicated role key (recommended), rather than appending into `.+` for all services:

| Item | `href` |
| --- | --- |
| Create case | `/cases/case-filter` |
| **Create batch** | Absolute URL of the create-batch journey (per env) |
| Find case | `/cases/case-search` (right-aligned, existing pattern) |

Omit **Case list** from the TEC menu; clerks still reach `/cases` via the **Manage cases** title.

- **Role key:** `caseworker-tec` (or `^caseworker-tec` / `^caseworker-tec$` depending on whether
  `caseworker-tec-system` should see it). Local IdAM roles today: `caseworker-tec`,
  `caseworker-tec-system` (`UserRole` in this repo).
- **Label:** `Create batch`
- **Target:** `/cases/case-create/TEC/TEC_BATCH/uploadBatch` (Create batch CCD wizard — see
  [ccd-architecture.md](./ccd-architecture.md#create-batch-journey-uploadbatch)).

### What to change in ExUI

1. `api/configuration/menuConfigs/base-config.ts` — add TEC role-key menu including Create batch.
2. Confirm whether AAT needs anything in `aat-diffs.ts` (usually not for a new key).
3. Do not copy Case list into the TEC key unless product wants it; title/home already opens cases.
4. Release via normal ExUI pipeline; environments pick up the new webapp image.

### Ownership and dependencies

| Concern | Owner |
| --- | --- |
| Nav item + role gating | ExUI (`rpx-xui-webapp`) |
| Create batch journey + URL | TEC |
| Batch list/details (`TEC_BATCH`) | TEC / this POC |
| IdAM roles for TEC clerks | IdAM / TEC access model |
| This POC repo (`tec-poc`) | Cannot ship primary nav; only documents and local wiring |

### Non-goals

- No CCD / TEC API change for the nav link itself.
- Not a first-class ExUI feature module (unlike Work Allocation “My work”) unless product later
  wants create-batch inside Manage Cases chrome.

## Prototype without AAT/prod platform change

This repo simulates the menuConfigs change locally with a reverse proxy (not a custom XUI image):

- `bootWithCCD` sets `XUI_PORT=3002` (real Manage Cases container).
- `bin/start-xui-manage-batches-proxy.sh` publishes **http://localhost:3000** and rewrites
  `/external/config/ui/` to inject a `caseworker-tec` menu that includes **Create batch**.
- Create batch nav href is the ExUI deep link for `uploadBatch`; `/tec-create-batch` redirects there.

Details: [exui-navigation.md](./exui-navigation.md).

- `RSE_LIB_XUI_ENV_HEADER_CONFIG` will **not** work — UI ignores it for `headerConfig`.
- Restarting the stock stack alone without the proxy (or with XUI still bound to :3000) will
  **not** show Create batch.

## Risks / decisions

1. **External URL vs same-origin proxy** — absolute URL is simplest; same-origin only if IdAM
   cookies / CSP block cross-origin.
2. **Who sees the link** — clerks only vs clerks + system role.
3. **Create case visibility** — TEC create is system-driven today; keeping Create case in the TEC
   menu matches ExUI norms but may be unused for clerks.
4. **Cross-jurisdiction pollution** — dedicated `caseworker-tec` key avoids showing Create batch
   to all `.+` users.
5. **Release coupling** — nav appears only after an ExUI release; create-batch URL must exist (or be
   behind a flag/placeholder) per environment.
6. **Case list default case type** — ExUI defaults via localStorage / first case type; see
   [exui-navigation.md](./exui-navigation.md).

## Acceptance criteria

- TEC clerk signing into Manage Cases sees **Create batch** in the primary nav (not Manage batches).
- Non-TEC users do not see it.
- Link opens the `uploadBatch` CCD wizard at `/cases/case-create/TEC/TEC_BATCH/uploadBatch`.
- Create case / Find case remain; Case list can stay hidden with cases reachable via Manage cases.
- Local POC can demonstrate the same nav via the nav proxy without AAT/prod deploy.
- Batches are browsable as case type **TEC Batch** on Case list / Find case.

Journey behaviour (TEC / this POC): see [ccd-architecture.md](./ccd-architecture.md#create-batch-journey-uploadbatch).

## Follow-ups

- [ ] ExUI PR: add `caseworker-tec` menu key with Create batch + standard items in `base-config.ts`
- [x] Create batch journey: clerk-visible `uploadBatch` CCD multi-page event
- [x] Local simulation: nav-injection proxy on :3000 (`bin/start-xui-manage-batches-proxy.sh`)
- [x] CCD `TEC_BATCH` case type for batch list/details
