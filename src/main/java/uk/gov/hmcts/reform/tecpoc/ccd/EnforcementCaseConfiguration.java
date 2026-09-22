package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Component
public class EnforcementCaseConfiguration
    implements CCDConfig<EnforcementCase, EnforcementCaseState, UserRole> {

    public static final String CASE_TYPE = "TEC_ENFORCEMENT";
    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private final EnforcementCaseRepository repository;

    public EnforcementCaseConfiguration(@Lazy EnforcementCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void configureDecentralised(
        DecentralisedConfigBuilder<EnforcementCase, EnforcementCaseState, UserRole> builder
    ) {
        builder.caseType(CASE_TYPE, "TEC Enforcement", "A TEC enforcement case");
        builder.jurisdiction("TEC", "Traffic Enforcement Centre", "Traffic Enforcement Centre");
        builder.hmctsServiceId("TEC1");
        builder.setCallbackHost(System.getenv().getOrDefault("API_URL", "http://localhost:4013"));

        configureAccessProfiles(builder);
        configureStateAccess(builder);
        configureCaseView(builder);
        configureEvents(builder);

        builder.shutterService(UserRole.LOCAL_AUTHORITY);
    }

    private void configureAccessProfiles(
        DecentralisedConfigBuilder<EnforcementCase, EnforcementCaseState, UserRole> builder
    ) {
        builder.caseRoleToAccessProfile(UserRole.SYSTEM)
            .accessProfiles(UserRole.SYSTEM.getRole())
            .legacyIdamRole();
        builder.caseRoleToAccessProfile(UserRole.CLERK)
            .accessProfiles(UserRole.CLERK.getRole())
            .legacyIdamRole();
    }

    private void configureStateAccess(
        DecentralisedConfigBuilder<EnforcementCase, EnforcementCaseState, UserRole> builder
    ) {
        for (EnforcementCaseState state : EnforcementCaseState.values()) {
            builder.grant(state, Permission.CRUD, UserRole.SYSTEM);
            builder.grant(state, Set.of(Permission.R, Permission.U), UserRole.CLERK);
        }
    }

    private void configureCaseView(
        DecentralisedConfigBuilder<EnforcementCase, EnforcementCaseState, UserRole> builder
    ) {
        builder.tab("tasks", "Tasks")
            .forRoles(UserRole.CLERK, UserRole.SYSTEM)
            .label("tasksMarkdownLabel", null, "${tasksMarkdown}")
            .field("tasksMarkdown", NEVER_SHOW);

        builder.tab("rolesAndAccess", "Roles and access")
            .label("rolesAndAccessLabel", null, "${rolesAndAccessMarkdown}")
            .field("rolesAndAccessMarkdown", NEVER_SHOW);

        builder.tab("caseDetails", "Case details")
            .field(EnforcementCase::getStatusDisplay)
            .field(EnforcementCase::getLocalAuthority)
            .field(EnforcementCase::getSubmitterEmail)
            .field(EnforcementCase::getReceivedVia);

        builder.tab("caseFileView", "Case File View")
            .field(EnforcementCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)")
            .field(EnforcementCase::getAllDocuments, NEVER_SHOW);

        // Explicit CaseHistory so History sits after Case File View (SDK otherwise prepends it).
        builder.tab("CaseHistory", "History")
            .field("caseHistory");

        builder.tab("caseLinks", "Linked Cases")
            .field(EnforcementCase::getLinkedCasesComponentLauncher, null, "#ARGUMENT(LinkedCases)")
            .field(
                EnforcementCase::getCaseLinks,
                "LinkedCasesComponentLauncher!=\"\"",
                "#ARGUMENT(LinkedCases)"
            );

        builder.searchInputFields()
            .field(EnforcementCase::getLocalAuthority, "Local authority")
            .field(EnforcementCase::getSubmitterEmail, "Submitter email")
            .field(EnforcementCase::getReceivedVia, "Received via");

        builder.searchResultFields()
            .caseReferenceField()
            .field("[STATE]", "State")
            .field(EnforcementCase::getLocalAuthority, "Local authority")
            .field(EnforcementCase::getSubmitterEmail, "Submitter email");

        builder.workBasketInputFields()
            .field(EnforcementCase::getLocalAuthority, "Local authority")
            .field(EnforcementCase::getReceivedVia, "Received via");

        builder.workBasketResultFields()
            .caseReferenceField()
            .field("[STATE]", "State")
            .field(EnforcementCase::getLocalAuthority, "Local authority");
    }

    private void configureEvents(
        DecentralisedConfigBuilder<EnforcementCase, EnforcementCaseState, UserRole> builder
    ) {
        builder.decentralisedEvent("createEnforcementCase", this::createEnforcementCase)
            .initialState(EnforcementCaseState.OPEN)
            .name("Enforcement case created")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .grant(Permission.R, UserRole.CLERK)
            .fields()
            .mandatory(EnforcementCase::getLocalAuthority)
            .mandatory(EnforcementCase::getSubmitterEmail)
            .mandatory(EnforcementCase::getReceivedVia);

        builder.decentralisedEvent("linkPcnCases", this::linkPcnCases)
            .forStates(EnforcementCaseState.values())
            .name("Link PCN cases")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .grant(Permission.R, UserRole.CLERK)
            .fields()
            .mandatory(EnforcementCase::getCaseLinks);

        builder.decentralisedEvent("attachCaseFileDocument", this::attachCaseFileDocument)
            .forStates(EnforcementCaseState.values())
            .name("Attach case file document")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .fields()
            .mandatory(EnforcementCase::getCaseFileDocument);
    }

    private SubmitResponse<EnforcementCaseState> createEnforcementCase(
        EventPayload<EnforcementCase, EnforcementCaseState> event
    ) {
        repository.create(event.caseReference(), event.caseData());
        return SubmitResponse.<EnforcementCaseState>builder()
            .state(EnforcementCaseState.OPEN)
            .build();
    }

    private SubmitResponse<EnforcementCaseState> linkPcnCases(
        EventPayload<EnforcementCase, EnforcementCaseState> event
    ) {
        List<ListValue<CaseLink>> caseLinks = event.caseData().getCaseLinks();
        if (caseLinks == null || caseLinks.isEmpty()) {
            throw new IllegalArgumentException("caseLinks is required");
        }

        List<Long> pcnReferences = new ArrayList<>();
        for (ListValue<CaseLink> entry : caseLinks) {
            if (entry == null || entry.getValue() == null || isBlank(entry.getValue().getCaseReference())) {
                throw new IllegalArgumentException("caseLinks[].value.CaseReference is required");
            }
            long pcnCaseReference = parseCaseReference(entry.getValue().getCaseReference());
            if (!repository.pcnExists(pcnCaseReference)) {
                throw new IllegalArgumentException(
                    "No TEC PCN case found for reference " + pcnCaseReference
                );
            }
            Long existingEnforcement = repository.findEnforcementCaseReferenceForPcn(pcnCaseReference);
            if (existingEnforcement != null && existingEnforcement.longValue() != event.caseReference()) {
                throw new IllegalArgumentException(
                    "PCN case " + pcnCaseReference
                        + " is already linked to enforcement case " + existingEnforcement
                );
            }
            pcnReferences.add(pcnCaseReference);
        }

        for (Long pcnCaseReference : pcnReferences) {
            repository.linkPcnCase(event.caseReference(), pcnCaseReference);
        }
        return SubmitResponse.defaultResponse();
    }

    private SubmitResponse<EnforcementCaseState> attachCaseFileDocument(
        EventPayload<EnforcementCase, EnforcementCaseState> event
    ) {
        Document document = event.caseData().getCaseFileDocument();
        if (document == null) {
            throw new IllegalArgumentException("caseFileDocument is required");
        }
        if (isBlank(document.getUrl())
            || isBlank(document.getBinaryUrl())
            || isBlank(document.getFilename())) {
            throw new IllegalArgumentException(
                "caseFileDocument requires document_url, document_binary_url and document_filename"
            );
        }

        String categoryId = document.getCategoryId();
        if (categoryId != null && categoryId.isBlank()) {
            categoryId = null;
        }
        repository.insertDocument(
            event.caseReference(),
            categoryId,
            CdamDocumentUrls.toCdamUrl(document.getUrl()),
            CdamDocumentUrls.toCdamUrl(document.getBinaryUrl()),
            document.getFilename()
        );
        return SubmitResponse.defaultResponse();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    static long parseCaseReference(String value) {
        String digits = value == null ? "" : value.replace("-", "").trim();
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(
                "Case reference must contain digits (hyphens optional): '" + value + "'"
            );
        }
        return Long.parseLong(digits);
    }
}
