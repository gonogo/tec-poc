# TEC API POC

This codebase is a local sandbox for experimentation around CCD config and its effect upon Manage Cases.

Functionality is underpinned by runtime supplied by [rse-cft-lib](https://github.com/hmcts/rse-cft-lib). See
(AI-generated) doc
[TEC decentralised CCD architecture](docs/ccd-architecture.md) for the build-time and local runtime architecture, and
[CFTLib Shared Database](docs/cftlib-shared-database.md) for a description of the decentralised CCD datamodel.

All CCD config including states, events, roles and case types are for illustration only.

## Prerequisites

- Java 21
- Docker
- An authenticated HMCTS Azure Container Registry session (`az acr login --name hmctsprod`)
- `jq` for the command-line example below

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
- CCD Data Store: http://localhost:4452
- IDAM simulator: http://localhost:5062
- S2S simulator: http://localhost:8489
- Shared PostgreSQL: `localhost:6432` (the TEC database is `tec`)

Stop the Java stack with `Ctrl-C`. The Docker containers will continue to run; to tear everything down
(Java processes, local stubs/proxies, and CFTLib containers) and free the required ports:

```bash
./bin/stop-boot-with-ccd.sh
```

To stop and start again in one step:

```bash
./bin/restart-boot-with-ccd.sh
```

### Use Manage Case

CFTLib starts the Manage Case web application in Docker on port **3002**. A local proxy on
**http://localhost:3000** injects a TEC **Create batch** primary-nav item (see
[docs/exui-navigation.md](docs/exui-navigation.md)).

Open http://localhost:3000 and sign in with the configured local clerk account:

```text
Username: tec-demo@test.com
Password: password
```

After sign-in you should see **Create batch** in the primary navigation (local simulation of the
ExUI `menuConfigs` change). That link opens the `uploadBatch` CCD wizard
(`/cases/case-create/TEC/TEC_BATCH/uploadBatch`): select batch type, upload a file, review
placeholder validation, confirm the statement of truth, Check your answers, then Submit. The
confirmation screen shows the new case number (no Manage cases link in the body). Open Case list
via **Manage cases** and use the case type filter to switch between PCN cases (`TEC`) and batches
(`TEC_BATCH` / **Batch**).

Journey detail: [docs/ccd-architecture.md](docs/ccd-architecture.md#create-batch-journey-uploadbatch).

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

Batches are a second CCD case type (`TEC_BATCH`, display name **Batch**).

**In Manage Case:** use primary nav **Create batch** to run the clerk `uploadBatch` wizard (see
above). That creates a real batch case through CCD.

**Via API / scripts** (hidden `createBatch` event — for seeding demos):

```bash
./bin/create-tec-batch.sh
./bin/create-tec-batches.sh 6
```

Optional overrides: `BATCH_IDENTIFIER`, `PCN_COUNT`, `OPERATION`, `RECEIVED_VIA`, `LOCAL_AUTHORITY`,
`TARGET_STATE` (`QUEUED_FOR_PROCESSING` | `PROCESSING_STARTED` | `PROCESSING_COMPLETE`).
Completed batches get sample Inputs/Outputs documents attached for Batch details demos.

In Manage Case, open Case list → set case type to **Batch** → open a row for History, Tasks,
and Batch details (Inputs/Outputs links at the bottom of Batch details).

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
