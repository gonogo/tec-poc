# TEC API POC

This codebase is a local sandbox for experimentation around CCD config and its effect upon Manage Cases.

Functionality is underpinned by runtime supplied by [rse-cft-lib](https://github.com/hmcts/rse-cft-lib). See
(AI-generated) doc
[TEC decentralised CCD architecture](tech_docs/source/ccd-architecture.html.md.erb) for the build-time and local runtime architecture, and
[CFTLib Shared Database](tech_docs/source/cftlib-shared-database.html.md.erb) for a description of the decentralised CCD datamodel.
Rendered versions are on the tech docs site at http://localhost:4568 (for example
[/ccd-architecture.html](http://localhost:4568/ccd-architecture.html)).

All CCD config including states, events, roles and case types are for illustration only.

## Prerequisites

- Java 21
- Docker
- An authenticated HMCTS Azure Container Registry session (`az acr login --name hmctsprod`)
- `jq` for the command-line example below
- Ruby 3.3 and Bundler (optional; needed for the design/tech docs previews started with `bootWithCCD`)

Gradle is provided by the checked-in `./gradlew` wrapper.

## Run TEC with a local CCD stack

Start the application together with CCD Data Store, Definition Store, User Profile, local IDAM/S2S simulators, and
their supporting infrastructure:

```bash
./gradlew bootWithCCD
```

CFTLib runs the Java services in isolated classloaders in one JVM and uses Docker for
supporting infrastructure. It is therefore a clear stand-in for the CFT platform, but avoids the cost of
running every CCD Java service as a separate container.

The local services are:

- TEC API and decentralised callback runtime: http://localhost:4013
- Manage Case (XUI): http://localhost:3000 (nav-injection proxy; real container on :3002)
- Design docs (GOV.UK Tech Docs / Middleman): http://localhost:4567
- Tech docs (GOV.UK Tech Docs / Middleman): http://localhost:4568
- CCD Data Store: http://localhost:4452
- IDAM simulator: http://localhost:5062
- S2S simulator: http://localhost:8489
- Shared PostgreSQL: `localhost:6432` (the TEC database is `tec`)

`bootWithCCD` starts both docs previews via `bin/start-design-docs.sh` (:4567) and
`bin/start-tech-docs.sh` (:4568) (first run may run `bundle install` under each site). If
Ruby/Bundler are missing, the stack still starts and the docs servers are skipped; install
Ruby 3.3 and re-run, or start either script alone.

Design docs publish to GitHub Pages from `.github/workflows/deploy-pages.yml` when `design_docs/`
changes on `main`. Enable **Settings → Pages → Source: GitHub Actions**, and keep `host` /
`github_repo` in `design_docs/config/tech-docs.yml` aligned with the Pages URL.

Stop the Java stack with `Ctrl-C`. The Docker containers will continue to run; to tear everything down
(Java processes, local stubs/proxies, design/tech docs servers, and CFTLib containers) and free the required ports:

```bash
./bin/stop-boot-with-ccd.sh
```

To stop and start again in one step:

```bash
./bin/restart-boot-with-ccd.sh
```

### Use Manage Case

CFTLib starts the Manage Case web application in Docker on port **3002**. A local proxy on
**http://localhost:3000** injects a TEC **Upload batch file** primary-nav item (see
[tech_docs/source/exui-navigation.html.md.erb](tech_docs/source/exui-navigation.html.md.erb)
or http://localhost:4568/exui-navigation.html).

Open http://localhost:3000 and sign in with a configured local account:

```text
Username: tec-demo@test.com
Password: password
```

Local authority demo user (GM-scoped TEC / batch access; no Create case / exceptions / Tasks):

```text
Username: tec-la-demo@test.com
Password: password
```

After sign-in you should see **Upload batch file** in the primary navigation (local simulation of the
ExUI `menuConfigs` change). Clerks also see **Create case**; LA users do not. That link opens the
`uploadBatch` CCD wizard (`/cases/case-create/TEC/TEC_BATCH/uploadBatch`): select batch type, upload
a file, review placeholder validation, confirm the statement of truth, Check your answers, then
Submit. The confirmation screen shows the new case number (no Manage cases link in the body). Open
Case list via **Manage cases** and use the case type filter to switch between PCN cases
(`TEC` / **TEC PCN**), batches (`TEC_BATCH` / **TEC Batch**), and exception cases
(`TEC_EXCEPTION` / **TEC Exception**) — LA users do not see Exception.

Seed scripts leave `localAuthority` as-is; `CaseAccessCategory` is derived server-side. For LA-visible
cases, override when seeding, e.g. `LOCAL_AUTHORITY=manchesterCityCouncil ./bin/create-tec-case.sh`.
Re-seed or recreate cases after this change so categories are populated.

Journey detail: [tech_docs/source/ccd-architecture.html.md.erb](tech_docs/source/ccd-architecture.html.md.erb#upload-batch-file-journey-uploadbatch)
(or http://localhost:4568/ccd-architecture.html#upload-batch-file-journey-uploadbatch).

## Create a PCN case

With `bootWithCCD` running, create a valid TEC case using the local system user:

```bash
./bin/create-tec-case.sh
```

The script generates unique valid identifiers and submits an amount of `12345` pence. Set `AMOUNT_DUE`,
`FILE_IDENTIFIER`, `BATCH_IDENTIFIER`, or `PENALTY_CHARGE_NUMBER` to override those defaults.

To make the request manually, obtain a token for the local TEC system user (password `password`):

```bash
TOKEN=$(curl --silent --request POST http://localhost:5062/o/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=tec' \
  --data-urlencode 'client_secret=123456' \
  --data-urlencode 'username=tec-system@test.com' \
  --data-urlencode 'password=password' \
  --data-urlencode 'scope=openid profile roles' | jq --raw-output '.access_token')
```

Then call the small TEC-facing API:

```bash
curl --request POST http://localhost:4013/pcn-cases \
  --header "Authorization: Bearer ${TOKEN}" \
  --header 'Content-Type: application/json' \
  --data '{
    "fileIdentifier": "RTE12345",
    "batchIdentifier": "RTE123456",
    "penaltyChargeNumber": "TE1234567A8",
    "localAuthority": "westminster",
    "respondentDetails1": "ALEX EXAMPLE",
    "respondentDetails2": "1 EXAMPLE STREET",
    "respondentDetails3": "LONDON",
    "respondentDetails4": "SW1A 1AA",
    "vehicleRegistrationNumber": "AB12CDE",
    "natureOfOffence": "01",
    "dateChargeCertificateServed": "260824",
    "amountDue": 12345
  }'
```

`amountDue` is expressed in pence; for example, `12345` represents £123.45.
`localAuthority` is a FixedList code from the 2023 England councils list (for example `westminster`,
`manchesterCityCouncil`).

The response contains the CCD-generated reference and initial state:

```json
{
  "caseReference": 1755000000000000,
  "state": "PENDING_CASE_ISSUED"
}
```

## Create a batch

Batches are a second CCD case type (`TEC_BATCH`, display name **TEC Batch**).

**In Manage Case:** use primary nav **Upload batch file** to run the clerk `uploadBatch` wizard (see
above). That creates a real batch case through CCD.

**Via API / scripts** (hidden `createBatch` event — for seeding demos):

```bash
./bin/create-tec-batch.sh
./bin/create-tec-batches.sh 6
```

Optional overrides: `BATCH_IDENTIFIER`, `PCN_COUNT`, `OPERATION`, `RECEIVED_VIA`, `LOCAL_AUTHORITY`,
`TARGET_STATE` (`QUEUED_FOR_PROCESSING` | `PROCESSING_STARTED` | `PROCESSING_COMPLETE`).
Completed batches get sample Inputs/Outputs documents attached for Batch details demos.

In Manage Case, open Case list → set case type to **TEC Batch** → open a row for History, Tasks,
and Batch details (Inputs/Outputs links at the bottom of Batch details). Registration batches also
show **Fees due** while queued and **Fees paid** when processing is complete (`PCN count × £11`).
See [tech_docs/source/ccd-architecture.html.md.erb](tech_docs/source/ccd-architecture.html.md.erb#batch-details-presentation)
(or http://localhost:4568/ccd-architecture.html#batch-details-presentation).

## Create an exception case

Exception cases are a third CCD case type (`TEC_EXCEPTION`, display name **TEC Exception**). Case
details show Form validation result and Associated TEC case as `—`, plus a PCN. Clerk Next steps
are **Reject item** (radio reason + optional History comment; case stays open) and **Edit PCN**.

With `bootWithCCD` running:

```bash
./bin/create-tec-exception-case.sh
```

Optional override: `PENALTY_CHARGE_NUMBER`.

Or via the API (same token pattern as PCN create):

```bash
curl --request POST http://localhost:4013/exception-cases \
  --header "Authorization: Bearer ${TOKEN}" \
  --header 'Content-Type: application/json' \
  --data '{
    "penaltyChargeNumber": "AB1234567A0"
  }'
```

In Manage Case, open Case list → set case type to **TEC Exception** → open a row for History,
Tasks, Roles and access, Case details, and Case File View.

### Prototype Tasks tab (local)

The **Tasks** tab is a CCD collection tab backed by prototype data in `TecCaseView`, not Work Allocation.
No extra docker services or Azure registry access are required.

Create a case and move it to `CASE_ISSUED` to see sample tasks:

```bash
./bin/create-tec-case.sh
./bin/transition-to-case-issued.sh <case-reference-from-output>
```

Open the case in Manage Case as `tec-demo@test.com` to see the **Tasks** tab.

### Attach a document to Case File View (local)

Case File View folders are empty until documents are attached. CFTLib's Case Document AM API
proxies uploads to dm-store on port `4506`. `bootWithCCD` starts the local dm-store stub
automatically; if Case File View opens blank, ensure the stub is still running
(the document viewer loads binaries through CDAM → dm-store):

```bash
./bin/start-local-dm-store.sh
```

With `bootWithCCD` running (restart it after pulling these changes so the attach event, migration and
document URL pattern are loaded), create a case and attach a file:

```bash
./bin/create-tec-case.sh
./bin/attach-case-file-document.sh <case-reference> "Hearing documents" ./path/to/file.pdf
```

If the filename has spaces, quote it:

```bash
./bin/attach-case-file-document.sh <case-reference> "Applications" "Witness statement - Out of time.pdf"
```

`<folder>` may be a category id or label: `hearingDocuments`, `ordersAndNoticesOfHearings`,
`applications`, `correspondence`, `uncategorisedDocuments` (or the matching display labels).

Refresh the case in Manage Case to see the file under the chosen Case File View folder.

### Generate a sample TE9/PE3 application (local)

With `bootWithCCD` running, generate application data for an existing case, submit the
`recordApplication` event, fill the TE9/PE3 PDF template, and attach it under **Applications**:

```bash
./bin/generate-application.sh <case-reference> "out of time" TE9
./bin/generate-application.sh <case-reference> "in time" PE3
```

Hyphens in the case reference are ignored. Type may be `in time` / `out of time` (or
`in-time` / `out-of-time`, `inTime` / `outOfTime`). Form must be `TE9` or `PE3`.
Attached PDFs are named like `Witness statement - Out of time.pdf` or
`Statutory declaration - In time.pdf`.

The script copies PCN, VRN, name and address from the case where possible and randomly
fills the remaining application fields. Set `SEED=<n>` for reproducible random values.
On first run the script creates `bin/.venv-generate-application` and installs `pypdf` /
`reportlab` there for PDF filling.

### Generate a sample TE7/PE2 time-extension request (local)

With `bootWithCCD` running, generate time-extension data for an existing case, submit the
`recordTimeExtension` event, fill the TE7/PE2 PDF template, and attach it under **Applications**:

```bash
./bin/generate-time-extension.sh <case-reference> TE7
./bin/generate-time-extension.sh <case-reference> PE2
```

Hyphens in the case reference are ignored. Form must be `TE7` or `PE2`. Set `SEED=<n>` for
reproducible random values. The script reuses the same Python venv as
`generate-application.sh`. Attached PDFs are named from the section heading (for TE7, based on
permission sought: `Application to file out of time.pdf` or
`Application for extension of time.pdf`).

Documents are stored by the local dm-store stub under `bin/.local-dm-store-data/`. If that stub
was restarted before persistence was added, older folder entries can still appear while the viewer
stays blank — re-attach the file once so the binary is available again.

Manage Case loads the viewer via its `/documentsv2` proxy to Case Document AM (`:4455`).
`bootWithCCD` sets that through `XUI_DOCUMENTS_API(_V2)` in `build.gradle` (compose interpolates
these into the XUI container). If the viewer is empty and XUI logs show proxying to `:5062`
instead of `:4455`, recreate Manage Case with those env vars set, or restart `bootWithCCD`
after pulling the Gradle fix so Manage Case picks up the CDAM URLs.
