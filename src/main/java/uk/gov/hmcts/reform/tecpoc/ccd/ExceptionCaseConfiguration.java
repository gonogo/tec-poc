package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventMetadata;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;

@Component
public class ExceptionCaseConfiguration implements CCDConfig<ExceptionCase, ExceptionCaseState, UserRole> {

    public static final String CASE_TYPE = "TEC_EXCEPTION";
    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private final ExceptionCaseRepository repository;

    public ExceptionCaseConfiguration(@Lazy ExceptionCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void configureDecentralised(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        builder.caseType(CASE_TYPE, "TEC Exception", "A TEC exception case");
        builder.jurisdiction("TEC", "Traffic Enforcement Centre", "Traffic Enforcement Centre");
        builder.hmctsServiceId("TEC1");
        builder.setCallbackHost(System.getenv().getOrDefault("API_URL", "http://localhost:4013"));

        configureAccessProfiles(builder);
        configureStateAccess(builder);
        configureCaseView(builder);
        configureCaseFileCategories(builder);
        configureEvents(builder);
    }

    private void configureAccessProfiles(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        for (UserRole role : UserRole.values()) {
            builder.caseRoleToAccessProfile(role)
                .accessProfiles(role.getRole())
                .legacyIdamRole();
        }
    }

    private void configureStateAccess(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        for (ExceptionCaseState state : ExceptionCaseState.values()) {
            builder.grant(state, Permission.CRUD, UserRole.SYSTEM);
            builder.grant(state, Set.of(Permission.R, Permission.U), UserRole.CLERK);
        }
    }

    private void configureCaseView(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        builder.tab("tasks", "Tasks")
            .label("tasksMarkdownLabel", null, "${tasksMarkdown}")
            .field("tasksMarkdown", NEVER_SHOW);

        // CCD shell only — real ExUI Roles and access is prepended when WA is enabled for the jurisdiction.
        builder.tab("rolesAndAccess", "Roles and access")
            .label("rolesAndAccessLabel", null, "${rolesAndAccessMarkdown}")
            .field("rolesAndAccessMarkdown", NEVER_SHOW);

        builder.tab("caseDetails", "Case details")
            .field(ExceptionCase::getFormValidationResultDisplay)
            .field(ExceptionCase::getAssociatedTecCaseDisplay)
            .field(ExceptionCase::getPenaltyChargeNumber);

        builder.tab("caseFileView", "Case File View")
            .field(ExceptionCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)")
            .field(ExceptionCase::getAllDocuments, NEVER_SHOW);

        builder.searchInputFields()
            .field(ExceptionCase::getPenaltyChargeNumber, "PCN");

        builder.searchResultFields()
            .caseReferenceField()
            .field(ExceptionCase::getPenaltyChargeNumber, "PCN");

        builder.workBasketInputFields()
            .field(ExceptionCase::getPenaltyChargeNumber, "PCN");

        builder.workBasketResultFields()
            .caseReferenceField()
            .field(ExceptionCase::getPenaltyChargeNumber, "PCN");
    }

    private void configureCaseFileCategories(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        for (ExceptionCaseFileCategory category : ExceptionCaseFileCategory.values()) {
            builder.categories(UserRole.CLERK)
                .categoryID(category.getId())
                .categoryLabel(category.getLabel())
                .displayOrder(category.getDisplayOrder());
        }
    }

    private void configureEvents(
        DecentralisedConfigBuilder<ExceptionCase, ExceptionCaseState, UserRole> builder
    ) {
        builder.decentralisedEvent("createExceptionCase", this::createExceptionCase)
            .initialState(ExceptionCaseState.OPEN)
            .name("Exception case created")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .grant(Permission.R, UserRole.CLERK)
            .fields()
            .mandatory(ExceptionCase::getPenaltyChargeNumber);

        builder.decentralisedEvent("rejectItem", this::rejectItem)
            .forStates(ExceptionCaseState.OPEN)
            .name("Reject item")
            .grant(Permission.CRU, UserRole.CLERK)
            .fields()
            .mandatory(ExceptionCase::getRejectReason)
            .optional(ExceptionCase::getRejectComment);

        builder.decentralisedEvent("editPcn", this::editPcn)
            .forStates(ExceptionCaseState.OPEN)
            .name("Edit PCN")
            .description("Update the penalty charge number")
            .grant(Permission.CRU, UserRole.CLERK)
            .fields()
            .mandatory(ExceptionCase::getPenaltyChargeNumber);
    }

    private SubmitResponse<ExceptionCaseState> createExceptionCase(
        EventPayload<ExceptionCase, ExceptionCaseState> event
    ) {
        repository.create(event.caseReference(), event.caseData());
        return SubmitResponse.<ExceptionCaseState>builder()
            .state(ExceptionCaseState.OPEN)
            .build();
    }

    private SubmitResponse<ExceptionCaseState> rejectItem(
        EventPayload<ExceptionCase, ExceptionCaseState> event
    ) {
        repository.recordRejectReason(event.caseReference(), event.caseData().getRejectReason());
        String comment = event.caseData().getRejectComment();
        if (comment == null || comment.isBlank()) {
            return SubmitResponse.defaultResponse();
        }
        return SubmitResponse.<ExceptionCaseState>builder()
            .eventMetadata(EventMetadata.builder().description(comment.trim()).build())
            .build();
    }

    private SubmitResponse<ExceptionCaseState> editPcn(
        EventPayload<ExceptionCase, ExceptionCaseState> event
    ) {
        repository.updatePenaltyChargeNumber(
            event.caseReference(),
            event.caseData().getPenaltyChargeNumber()
        );
        return SubmitResponse.defaultResponse();
    }
}
