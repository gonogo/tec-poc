# Goal

Add an **Observations** tab to the TEC Manage Cases detail view showing the existing penalty charge number (PCN) and the respondent's first address line.

# Approach

Extend the existing decentralised CCD case-view configuration in `TecCaseConfiguration` with a third tab named and labelled `Observations`. Map the tab to `TecCase::getPenaltyChargeNumber` and `TecCase::getRespondentDetails2`; the repository and `TecCaseView` already persist and project those values, so no schema or API contract change is needed. Update the CCD architecture documentation to describe the new tab and verify the generated definition plus the local Manage Cases rendering.

# File Changes

- **Modify** [TecCaseConfiguration.java](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/TecCaseConfiguration.java?type=file&root=%252F): add an `observations` tab labelled `Observations`, containing the PCN field and `respondentDetails2` with a user-facing label identifying it as the first address line.
- **Modify** [ccd-architecture.md](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/docs/ccd-architecture.md?type=file&root=%252F): update the case-presentation description from two application tabs to three and document the fields shown by Observations.
- **No change** [TecCase.java](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/TecCase.java?type=file&root=%252F): both required values already exist as CCD fields.
- **No change** [TecCaseRepository.java](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/TecCaseRepository.java?type=file&root=%252F): both values are already inserted and selected.
- **No change** [TecCaseView.java](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/TecCaseView.java?type=file&root=%252F): it already returns the complete persisted case projection.
- **No change** [V1__create_tec_case.sql](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/resources/db/migration/V1__create_tec_case.sql?type=file&root=%252F): the requested data is already stored in `respondent_details_2` and `penalty_charge_number`.

# Implementation Steps

## Task 1: Add the CCD tab

1. In [TecCaseConfiguration.java](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/src/main/java/uk/gov/hmcts/reform/tecpoc/ccd/TecCaseConfiguration.java?type=file&root=%252F), add a new `builder.tab("observations", "Observations")` block alongside the existing registration tabs.
2. Add `TecCase::getPenaltyChargeNumber` to the block, preserving the model's existing “Penalty charge number” label.
3. Add `TecCase::getRespondentDetails2` with the explicit label “First line of respondent address” so Manage Cases does not expose the generic internal field name.
4. Keep the existing Registration request and Registration workflow tab contents unchanged; do not duplicate the requested fields into those tabs unless required by existing behaviour.

## Task 2: Keep documentation aligned

1. In [ccd-architecture.md](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/docs/ccd-architecture.md?type=file&root=%252F), change the tab count to three.
2. Add an Observations bullet specifying that it displays the PCN and first line of the respondent address, and identify that the address value is the existing respondent-details line 2.
3. Leave the source-of-truth and read-flow descriptions intact because the data projection path is unchanged.

## Task 3: Verify generated CCD metadata and runtime behaviour

1. Run `./gradlew generateCCDConfig` from the repository root.
2. Inspect the generated TEC definition under `build/ccd-definition/TEC` to confirm it contains a case view tab with identifier `observations`, label `Observations`, and exactly the PCN plus first-address-line fields.
3. Run the narrow unit/integration verification available for the repository, then run `./gradlew check` if Docker and the local test dependencies are available.
4. For end-to-end confirmation, start the local stack with `./gradlew bootWithCCD`, create a sample case using [create-tec-case.sh](air-file://aaq75qrq8776lgt6dee6/Users/chris.bellingham/Library/Caches/JetBrains/Air/agents/air/i-want-to-add-a-new-tab-to-the-manage-cases-detail-case-view-it--4a5ede30-5/tec-poc/bin/create-tec-case.sh?type=file&root=%252F), open Manage Cases, and confirm the Observations tab shows the submitted PCN and `respondentDetails2` value.
5. Confirm existing tabs, case creation, case read projection, search, and work-basket behaviour remain unchanged.

# Acceptance Criteria

- The generated TEC CCD definition contains an application tab with ID `observations` and display label `Observations`.
- The Observations tab displays the submitted `penaltyChargeNumber` value under the PCN field label.
- The Observations tab displays the submitted `respondentDetails2` value under “First line of respondent address”.
- The first address line is not sourced from or concatenated with respondent detail lines 1, 3, 4, 5, or 6.
- No Flyway migration is added, because the requested values are already stored in existing columns.
- Existing Registration request and Registration workflow tabs retain their current field membership and labels.
- The project compiles with warnings treated as errors, and the relevant Gradle verification tasks pass; any Docker/authentication-only limitation is explicitly reported.
- The architecture documentation states that three application tabs are generated and describes the Observations tab accurately.

# Verification Steps

- Run `./gradlew generateCCDConfig`.
- Inspect the generated case-view JSON for the tab ID, label, and two field IDs.
- Run `./gradlew test`.
- Run `./gradlew check` when Docker-backed integration prerequisites are available.
- Manually exercise the local stack with a case whose PCN and `respondentDetails2` are distinctive, then verify the Manage Cases detail view.
- Check that optional respondent lines 4–6 being absent does not affect the Observations tab.

# Risks & Mitigations

- **Ambiguous address mapping:** the current repository names address components generically. The existing README request maps `respondentDetails1` to the respondent name and `respondentDetails2` to `1 EXAMPLE STREET`; use `respondentDetails2) as the first address line and document that mapping.
- **Generated-definition drift:** CCD metadata is generated at build time and imported by CFTLib. Regenerate and inspect the TEC definition rather than relying only on Java compilation.
- **Label compatibility:** changing the model annotation could affect other views and generated field metadata. Apply the requested display label at the tab field configuration level, leaving the model annotation and existing views unchanged.
- **Local verification dependencies:** Manage Cases verification requires Docker and the configured local CCD/CFTLib stack. Run all non-Docker checks available and report environment-only failures separately.