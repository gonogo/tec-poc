# Define the TEC Case File View configuration foundation

This ExecPlan is a living document. Keep Progress, Surprises & Discoveries, Decision Log, and Outcomes & Retrospective current. No `PLANS.md` was found in this repository. This is a configuration-focused PoC plan, not a specification for implementing a working document-management service. Application changes and runtime validation have not been performed.

## Purpose / Big Picture


Produce a usable, importable CCD definition for a Case file tab on `TEC_BATCH_DATAFILE`, providing a foundation for actual development. CCD is the platform that defines case fields, events, states, and permissions; ExUI renders its caseworker interface from that definition. Success means reasonable confidence in that configuration, not a fully functional document lifecycle.

The intended business model remains exactly one datafile uploaded during case creation and zero or more system-generated documents created during PROCESSING. The future backend generates and links the required documents after `startProcessing` succeeds and before submitting `recordProcessingSuccess`. Case File View should organize these into Submitted datafile and Generated documents, with viewing available during and after processing. This PoC declares that model and its read-access rules. It does not have to generate, publish, or persist the system documents, and it adds no CCD event for doing so.

Use configuration-output tests, representative case-data fixtures, and a small definition-import/UI smoke check. Reuse the existing initial upload to check the native tab where practical. Do not build missing application features just to make a complete demonstration possible.

## Progress


- [x] (2026-09-17) Inspect the existing TEC batch configuration and pinned CCD/SDK sources.
- [x] (2026-09-17) Confirm the business scope: one creation-time datafile and system outputs created during PROCESSING.
- [x] (2026-09-17) Review Sptribs, PCS, and the CCD SDK for idiomatic configuration patterns.
- [x] (2026-09-17) Re-scope this plan to a configuration foundation, removing functional document-service implementation requirements.
- [x] (2026-09-17) Preserve the existing event model and place document generation/linking between processing start and completion, without a dedicated attachment event.
- [x] (2026-09-17) Include field-level category defaults and the pinned runtime's category-precedence behavior in the plan.
- [x] (2026-09-17) Declare the launcher, two categories, field-level category defaults, generated-document collection, and read-only permissions.
- [x] (2026-09-17) Verify fresh generated configuration, the unchanged event model, and representative document data with focused tests; `./gradlew test` and both Checkstyle suites pass.
- [ ] (2026-09-17) Import/UI validation is incomplete: `./gradlew bootWithCCD` stops before import because the preserved local `tec` database has a Flyway V1 checksum mismatch. The failure and unverified runtime behaviors are recorded; no database repair or reset was performed.

## Surprises & Discoveries


The initial upload already exists. `BatchDatafileCaseConfiguration.configureSubmission` makes `batchFile` mandatory for all three creation events. `BatchDatafileCaseMapper` reconstructs the document from its stored URL and filename, but currently supplies no category. Adding a fixed category in that mapper is sufficient scaffolding to exercise the new tab with existing data; no database migration is needed.

Case File View discovers CCD Document fields from case data, including collections. The launcher is not itself a document collection. Consequently, declaring a launcher does not grant access to the document fields, and repeating one document at multiple case-data paths can display duplicates. PCS demonstrates a plain `List<ListValue<Document>>` collection, so a custom metadata wrapper is unnecessary for this PoC.

The SDK emits `@CCD(categoryID = "...")` as the field definition's `CategoryID`, including for Collection(Document) fields. This default applies when a document has no runtime `category_id`. In the pinned Data Store's `CategoriesAndDocumentsService.resolveDocumentCategory`, a valid runtime category takes precedence; an invalid runtime category goes directly to Uncategorised rather than falling back to the field default. With neither runtime category nor field default, the document is also Uncategorised.

Case File View does not require a dedicated attachment event. TEC uses decentralised persistence: the backend owns business data and supplies the case projection to CCD. The existing processing transitions record lifecycle milestones; they do not describe every backend operation between those milestones. Their current handlers only return the target state and do not generate or link documents.

The SDK merges field access from events, tabs, and annotations. An R-only annotation does not remove an update grant derived from an event. Keep the launcher and displayed generated collection out of writable event inputs, and inspect the emitted permissions rather than relying only on Java annotations. Category definitions organize the view; they do not authorize access.

The SDK's generators can merge into existing output with `JsonUtils.AddMissing`, preserving stale rows. Configuration tests must generate into an empty temporary directory. A fresh generated definition is necessary evidence; merely compiling the configuration is insufficient.

The SDK's `generateCCDConfig` task starts the complete Spring application, even though definition generation itself does not use business data. In this repository that exposed duplicate Feign bean definitions and eager persistence/runtime beans. The task now allows the equivalent Feign definitions, enables lazy initialization, disables database auto-configuration, and disables readiness-group membership validation for generation only. Repository dependencies used by runtime callbacks are lazy, so the generator can resolve the CCD configuration without a database. Evidence: database-independent fresh generation completed with `BUILD SUCCESSFUL` and wrote `build/ccd-definition/TEC_BATCH_DATAFILE`.

The existing local CFT database is not compatible with the current application migration. `bootWithCCD` reported Flyway V1 checksum `858261369` applied versus `385591234` resolved before the host application could start. Because this plan requires preserving local cases and binaries, the database was not repaired or reset, and import/UI evidence could not be collected.

Displaying a document reference and establishing permission to download its bytes are different concerns. CDAM (the platform document-access service) must associate real uploads with a case. Earlier source review found relevant attachment paths in the pinned platform, but this plan does not implement or prove generated-document attachment. A synthetic fixture proves data shape, not binary access or production functionality.

## Decision Log


Decision (2026-09-17, user clarification): The deliverable is a usable CCD configuration foundation. Features may be non-functional. Database changes, document publishing, generation, transaction design, retries, and comprehensive lifecycle tests from earlier revisions are superseded, not remaining milestones.

Decision (2026-09-17, Codex): Retain exactly two categories, the existing singular `batchFile`, and a separate `generatedDocuments` collection. Use the SDK's plain `List<ListValue<Document>>`, following PCS. Remove the custom wrapper, generation identifier, and linked-at metadata because they were driven by a publication/idempotency implementation that is now out of scope. Future business metadata can be added when actual development requires it.

Decision (2026-09-17, user-approved refinement): Declare field-level defaults of `submittedDatafile` on `batchFile` and `generatedDocuments` on the generated-document collection. Retain the matching runtime category assignment in the initial-file projection. The defaults make the generated configuration self-contained when runtime category values are absent; they do not repair invalid runtime values. Assert both defaults in the emitted definition without introducing new backend functionality.

Decision (2026-09-17, user clarification): Preserve the state/event model in `docs/initial-ccd-config.md`. The earlier decision to add `attachGeneratedDocument` is superseded. Do not add that event, its singular request-only field, a stub handler, or associated event permissions. Keep the displayed `generatedDocuments` collection. Document generation and linking are future backend responsibilities while the case is PROCESSING, completed before `recordProcessingSuccess` is submitted, not performed as part of the completion event.

Decision (2026-09-17, Codex): Keep the native Case File View read-only, with fixed categories. No custom ExUI component, additional human upload journey, document movement, deletion, or replacement is introduced. Tab visibility is not restricted to PROCESSING. The timing of backend generation/linking is a processing responsibility, not a new CCD transition or a rule enforced by this PoC's tab configuration.

Decision (2026-09-17, Codex): Configuration tests and a successful import are the primary checks. A limited UI smoke check gives additional confidence. Generated-document fixtures are acceptable; functional generation, persistence, binary retrieval, and restart durability are not acceptance requirements. Record any unverified runtime behavior without silently claiming it works.

Decision (2026-09-17, Codex): Make `generateCCDConfig` database-independent by excluding JPA repository, Flyway, data-source, and Hibernate auto-configuration in that task and lazily injecting runtime repositories. Definition generation should not require or mutate business data. Runtime and CFT boot retain their normal persistence, migration, and validation behavior.

Decision (2026-09-17, Codex): Do not repair, clean, or recreate the incompatible local `tec` database merely to obtain UI evidence. The plan's recovery rules prioritize preserving existing local data, so record the exact import blocker and leave the runtime smoke check outstanding.

## Outcomes & Retrospective


The Case File View configuration foundation is implemented. The emitted definition contains the native launcher, Submitted datafile and Generated documents categories, field-level category defaults, a `Collection(Document)` generated-document field, and read-only grants for all four data-reading roles. The mapper supplies the runtime category for the initial upload. Fresh-output tests also prove the exact seven-event model and that processing events have no document input fields. A representative fixture demonstrates two uniquely identified generated documents with explicit categories and no hashes.

`./gradlew test`, both Checkstyle suites, and `./gradlew generateCCDConfig` pass. Generated JSON inspection confirms the configured field types, category IDs, and `R` grants. Documentation now places future generation/linking between processing start and success and explains category precedence. No application dependency, event, handler, database table, or document service was added.

Runtime import and UI rendering remain unverified. `bootWithCCD` failed before the host application started because the preserved local database has a Flyway checksum mismatch. This is an environment confidence gap, not a passing import. Document generation, persistence, download authorization, and populated-folder rendering remain intentionally out of scope.

## Context and Orientation


Work from `/Users/chris.bellingham/workspace/hmcts/tec/tec-poc`, using Java 21 and the existing Gradle wrapper. `build.gradle` pins CCD SDK 6.32.0 and CFTLib 0.19.2277. CFTLib runs the local CCD/ExUI stack and imports the generated definition. No platform upgrade is required.

The relevant source directory is `src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/batchdatafile`. `BatchDatafileCase.java` declares case fields. `BatchDatafileCaseConfiguration.java` declares the three creation events, processing transitions, and Batch details and History tabs. `BatchDatafileCaseMapper.java` reconstructs the initial document; `BatchDatafileCaseViewProvider.java` supplies that reconstructed case data to CCD. This reconstruction is called a case projection. The existing entity, repository, and database schema need no changes for this plan.

`docs/initial-ccd-config.md` defines the lifecycle to preserve. A future processing worker submits `startProcessing` to move AWAITING_PROCESSING to PROCESSING, then processes the datafile, generates/uploads documents, and establishes their case links while still in PROCESSING. Linking includes both saved TEC case references and the document-access association through CDAM. Once the required work succeeds, the worker submits `recordProcessingSuccess` to move to COMPLETE; failure follows the existing `recordProcessingFailure` and retry lifecycle. Starting processing does not automatically implement or schedule that work today. Worker scheduling and the linking mechanism are deferred to actual development, not added to this PoC. Once linked references are exposed in the case projection, Case File View can display them without waiting for COMPLETE.

The SDK's `Document` contains document URLs, filename, and optional category and timestamp. `ListValue<Document>` represents one collection item with an ID and a document value. `ComponentLauncher` instructs ExUI to render a built-in component. `HasAccessControl` declares field permissions, where R means read and CRU means create, read, and update.

Tests are in `src/test/java/uk/gov/hmcts/reform/tecpoc/ccd/batchdatafile`. `BatchDatafileCaseConfigurationTest` already builds a resolved SDK configuration and checks events, roles, and tabs. Extend that approach and add assertions against emitted definition files. `src/cftlib/java/uk/gov/hmcts/reform/tecpoc/cftlib/TecCftLibConfiguration.java` configures the existing local environment. Some older README instructions and `bin/create-tec-case.sh` target PCN cases, not this batch case; do not use them as acceptance drivers.

## Plan of Work


### Milestone 1: Declare the Case file tab and document model


In `BatchDatafileCase.java`, add `ComponentLauncher caseFileView` and `List<ListValue<Document>> generatedDocuments`. Leave `Document batchFile` and the existing creation journeys unchanged. Add a small `access/CaseFileViewAccess.java` class implementing `HasAccessControl`, granting only R to LA_USER, CLERK, TEC_MANAGER, and SYSTEM. Apply the same read-access class to the generated collection if its grants are identical; no separate access abstraction is needed merely for naming.

In `BatchDatafileCaseConfiguration.java`, insert a Case file tab between Batch details and History. Use `.forRoles(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER)` and `.field(BatchDatafileCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)")`. Keep both the launcher and generated collection outside writable event fields, and do not add a PROCESSING-only display condition to the tab. SYSTEM needs field access but not a user-facing tab.

Add `CaseFileCategory.java` in the same package: a small enum containing `SUBMITTED_DATAFILE("submittedDatafile", "Submitted datafile", 1)` and `GENERATED_DOCUMENTS("generatedDocuments", "Generated documents", 2)`. Follow PCS's enum-driven builder pattern: `builder.categories(UserRole.SYSTEM).categoryID(category.getId()).categoryLabel(category.getLabel()).displayOrder(category.getDisplayOrder()).build()`. The SDK's role parameter does not create per-category permissions.

In `BatchDatafileCase.java`, add `categoryID = "submittedDatafile"` to the existing `batchFile` field's `@CCD` annotation, preserving its label and creation permissions. On the new `generatedDocuments` collection, declare `@CCD(label = "Generated documents", categoryID = "generatedDocuments", access = CaseFileViewAccess.class)`. These must emit `CategoryID` on the Document and Collection(Document) CaseField rows respectively. Keep these values identical to the category enum IDs. Do not assign a category to the launcher itself.

In `BatchDatafileCaseMapper.toCase`, assign the reconstructed initial document category `submittedDatafile`. This is the only required change to the existing projection logic. Leave generated documents empty or absent in ordinary PoC cases. Do not add storage, fabricate historical timestamps, or copy the initial file into the generated collection. Test fixture documents use `generatedDocuments` as their category explicitly.

This milestone is demonstrated by tests showing the new tab, exactly two category definitions, both field-level defaults, a collection of Documents, and the initial file's runtime category. The existing creation tests must continue to pass.

### Milestone 2: Validate and hand off the definition


Update `BatchDatafileCaseConfigurationTest` and `BatchDatafileCaseMapperTest`. Add `BatchCaseFileDefinitionTest` in the same unit-test package, generating into a fresh JUnit `@TempDir`. Assert the emitted CaseField types, CaseTypeTab launcher parameter and ordering, exactly two Categories rows, and effective role permissions. In particular, the launcher and generated collection must have read-only grants. Preserve existing case-state read grants and creation-event behavior. Do not require custom complex-type permission rows for the plain SDK Document collection.

In the emitted CaseField rows, assert that `batchFile` has `FieldType = Document` and `CategoryID = submittedDatafile`, and `generatedDocuments` has `FieldType = Collection`, `FieldTypeParameter = Document`, and `CategoryID = generatedDocuments`. Assert that both category IDs exist in Categories and that the launcher has no CategoryID. These checks must inspect generated output, not just annotation values.

Retain the configuration test's exact existing event set: `submitRegistrationDatafile`, `submitWarrantDatafile`, `submitWarrantReissueDatafile`, `startProcessing`, `recordProcessingSuccess`, `recordProcessingFailure`, and `retryProcessing`. Verify their state transitions and role grants remain unchanged in the generated definition. No new attachment event or request-only Document field is introduced, and the existing processing events do not gain document-input fields.

Use a small fixture built in test code with one initial Document and two distinct `ListValue<Document>` entries. Assert the serialized paths, explicit category IDs, unique fixture item IDs, and absence of document hashes. Fixture URLs are placeholders and must not be described as accessible files. This verifies the proposed case-data contract without a generated-document repository or live publisher.

Import the generated definition using the existing CFTLib setup and check the Case file tab in ExUI. Reuse a case created through the existing datafile upload flow. Check that the tab loads, the initial file is categorized correctly, and there is no human attachment action. If document download already works, record that evidence, but do not expand this task into document-store integration to make it work. Empty-folder rendering is not an acceptance condition: the generated category is verified in the definition even if the UI hides empty categories.

If a populated generated folder cannot be checked with existing local fixture facilities, rely on the fixture/configuration tests and record that the populated runtime view remains unverified. Do not build a new seed service or application persistence just for this check. Import errors or incorrect launcher/permissions must be corrected; unrelated platform or document-access failures should be recorded as limitations with their observed symptoms.

Update `docs/initial-ccd-config.md`, preserving the user's existing edits and state/event diagram, to describe the tab, document fields, two categories, and their field-level defaults. Explain the runtime-category precedence and that defaults apply only when runtime categories are absent. Explain that future backend generation and linking occur after entering PROCESSING and before submitting the success event; do not add another event or move this work into the completion event. Include a short account of which checks passed and which runtime behaviors remain unverified. Keep this handoff in that existing document and this plan; no additional implementation guide is required.

## Concrete Steps


Run commands from the repository root. Begin with `git status --short` and preserve unrelated work. After editing the configuration and focused tests, run:

    ./gradlew test

Expect BUILD SUCCESSFUL, including the new definition-output and fixture assertions. These tests should not require adding database integration tests or a new stack-backed test suite.

Produce fresh build output when no active local stack depends on it. Preserve any diagnostic logs needed from `build` before running the standard clean task:

    ./gradlew clean generateCCDConfig
    rg -n 'CaseFileView|ComponentLauncher|submittedDatafile|generatedDocuments' build/ccd-definition/TEC_BATCH_DATAFILE

Expect a generated TEC_BATCH_DATAFILE definition containing the new fields, tab, and categories, with the existing event model unchanged. Use the output tests, not this text search alone, to establish correct grants and field types. Do not clean a running stack's artifacts; coordinate regeneration/import if one is already in use.

For the limited import/UI check, use the existing Docker and local registry setup:

    ./gradlew bootWithCCD

Confirm a successful definition import. Open `http://localhost:3000`, sign in with an existing local human test account, and open or create a TEC Batch Datafile Case through its current creation flow. Observe Batch details, Case file, and History in that order. Record the initial document's category and any relevant browser/network error. Do not add commands for generating or publishing documents.

Record the commands actually run and their results in this plan. If the environment prevents import or UI validation, distinguish that missing evidence from passing unit tests and hand off the limitation; do not represent the definition as runtime-verified.

Implementation results on 17 September 2026 were:

    ./gradlew clean generateCCDConfig
    BUILD SUCCESSFUL in 3s

    rg -n 'CaseFileView|ComponentLauncher|submittedDatafile|generatedDocuments' \
      build/ccd-definition/TEC_BATCH_DATAFILE
    CaseField.json contained Document/Collection defaults and ComponentLauncher
    Categories.json contained submittedDatafile and generatedDocuments
    CaseTypeTab contained three CaseFileView launcher rows

    ./gradlew test checkstyleMain checkstyleTest
    BUILD SUCCESSFUL in 4s

    ./gradlew bootWithCCD
    BUILD FAILED: Flyway V1 checksum mismatch in preserved local tec database
    Applied checksum 858261369; resolved checksum 385591234

The failed CFT run stopped before the host application was available, so there was no successful import or ExUI page to inspect. The browser smoke check was therefore not performed.

## Validation and Acceptance


The configuration foundation is ready when the focused tests pass, fresh definition output has the specified model and access rules, and the definition imports successfully. The limited UI check should show the native Case file tab for an authorized human reader, ideally with the existing initial upload under Submitted datafile. If UI verification is blocked, report it explicitly as an outstanding confidence gap.

Tests establish one singular initial Document field, a zero-or-more generated Document collection, two fixed categories, their matching field-level CategoryID defaults, and read-only Case File View access. The generated configuration therefore specifies the folder for each document field even when runtime category values are absent. The tab has no state restriction that would hide it during or after processing. The seven existing events retain their state transitions, input fields, and permissions; no attachment event or handler is added.

The fixture represents the intended multi-document case-data shape, but does not establish live generation or access to bytes. The handoff explicitly places future generation and linking before the success event, while leaving their implementation out of scope. Successful generated-document publication, automatic processing, persistence, restart durability, retries, concurrent appends, and full CDAM authorization testing are not required to complete this PoC configuration task.

## Idempotence and Recovery


Definition tests use fresh temporary output so reruns cannot pass because of stale rows. Regenerate from the Java configuration after changes; do not hand-edit generated JSON as the lasting fix. If import fails, inspect the validation error, correct the configuration, regenerate, and retry.

`generateCCDConfig` excludes persistence auto-configuration only for its short-lived generator process, so it can inspect configuration without connecting to or migrating application data. Lazy repository injection ensures the unused runtime callbacks and case-view provider do not force a repository bean during generation. The runtime application and `bootWithCCD` do not inherit those generator-only settings.

There are no new migrations, document uploads, background jobs, or data-cleanup operations in this plan. Preserve existing local cases and document binaries. Reuse an existing test case for smoke checks where possible. Do not reset the database or alter unrelated stack settings to obtain a cleaner demonstration.

## Artifacts and Notes


The essential launcher wiring is:

    @CCD(label = "Case file", access = CaseFileViewAccess.class)
    private ComponentLauncher caseFileView;

    builder.tab("caseFile", "Case file")
        .forRoles(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER)
        .field(BatchDatafileCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)");

The proposed read model contains these distinct document paths:

    batchFile                              -> Submitted datafile (exactly one)
    generatedDocuments[item-id].value      -> Generated documents (zero or more)

The intended future sequence preserves the existing CCD event model:

    AWAITING_PROCESSING
      -> startProcessing succeeds -> PROCESSING
      -> backend processes, generates/uploads, and links documents (still PROCESSING)
      -> recordProcessingSuccess -> COMPLETE

The middle backend work is not a CCD event or a synchronous implementation inside the start-event handler. Required document links must be established before the success event is submitted, so documents can be available while the case is still PROCESSING. This sequence describes the future processing responsibility, not functionality implemented by this configuration PoC.

Fresh output inspection showed the following effective grants for both `caseFileView` and `generatedDocuments`:

    caseworker-tec-la-user  R
    caseworker-tec          R
    caseworker-tec-manager  R
    caseworker-tec-system   R

The earlier review inspected `../../sptribs-case-api` at HEAD `048a3584` (SDK 6.8.1), `../../pcs-api` at HEAD `e7bbf1442` (SDK 6.33.0), and `../../dtsse-ccd-config-generator` at HEAD `93e6d13e`. TEC remains on SDK 6.32.0 and CFTLib 0.19.2277. No reference application's newer dependency version implies an upgrade here.

Relevant Sptribs evidence is in `src/main/java/uk/gov/hmcts/sptribs/ciccase/tab/CaseTypeTab.java` and `ciccase/model/access/CaseFileViewAccess.java`: native launcher wiring and explicit read access. Relevant PCS evidence is under `src/main/java/uk/gov/hmcts/reform/pcs/ccd`: `CaseType.java`, `domain/CaseFileCategory.java`, `domain/PCSCase.java`, and `view/DocumentsView.java` demonstrate enum-driven categories and an SDK Document collection. Its document de-duplication service reinforces that the same reference should not be exposed twice.

In the SDK checkout, under `sdk/ccd-config-generator/src/main/java/uk/gov/hmcts/ccd/sdk`, `generator/AuthorisationCaseFieldGenerator.java` merges access grants and `generator/CategoriesGenerator.java` writes category rows. `sdk/ccd-config-generator/src/test/java/uk/gov/hmcts/ccd/sdk/E2EConfigGenerationTests.java` is the reference for testing emitted configuration. These patterns are sufficient for the configuration scope; the reference applications' persistence and rendering services are not implementation instructions for this PoC.

## Interfaces and Dependencies


Use existing CCD SDK `Document`, `ListValue`, `ComponentLauncher`, and `HasAccessControl` types. Reuse the current configuration builder, mapper, and test infrastructure. New production-source declarations are limited to the launcher and generated-document collection fields, category enum, and read-access class, with corresponding tab/category configuration and initial-file category mapping. No new event, handler, library dependency, HTTP client, database table, renderer, publisher service, or ExUI component is needed.

Revision note (2026-09-17): Earlier revisions covered a complete document-publication feature and a three-codebase alignment review. Following the user's clarification, this revision supersedes those implementation milestones with a CCD-configuration foundation. It removes storage, publication clients, PDF generation, generation IDs, retry/transaction design, demo commands, and broad end-to-end acceptance requirements. It retains idiomatic configuration patterns, minimal scaffolding, focused definition tests, and a bounded import/UI check.

Revision note (2026-09-17, simple event model): Removed the dedicated attachment event, request-only field, stub handler, and related tests and acceptance criteria. The plan now preserves `docs/initial-ccd-config.md`'s event model and describes generation/linking as future backend work after `startProcessing` succeeds and before `recordProcessingSuccess` is submitted. Updated the decisions, milestones, progress, validation, and dependency scope consistently; actual processing and document linking remain outside the PoC.

Revision note (2026-09-17, field-level categories): Added explicit CategoryID defaults for the initial Document field and generated Document collection, plus emitted-definition assertions. Recorded that valid runtime categories override defaults and invalid runtime categories go to Uncategorised without fallback. This makes the configuration more self-contained without expanding the implementation scope or changing any events.

Revision note (2026-09-17, implementation): Implemented the Case File View model, access rules, categories, mapper category, fresh-output and fixture tests, generator task isolation, and documentation. Recorded passing tests/generation and the preserved local database checksum mismatch that prevented CFT import and UI verification.
