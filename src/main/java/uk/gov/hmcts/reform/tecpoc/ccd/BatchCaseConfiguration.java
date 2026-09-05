package uk.gov.hmcts.reform.tecpoc.ccd;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.util.Set;

@Component
public class BatchCaseConfiguration implements CCDConfig<BatchCase, BatchCaseState, UserRole> {

    public static final String CASE_TYPE = "TEC_BATCH";
    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private final BatchCaseRepository repository;

    public BatchCaseConfiguration(@Lazy BatchCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder) {
        builder.caseType(CASE_TYPE, "Batch", "A TEC batch");
        builder.jurisdiction("TEC", "Traffic Enforcement Centre", "Traffic Enforcement Centre");
        builder.hmctsServiceId("TEC1");
        builder.setCallbackHost(System.getenv().getOrDefault("API_URL", "http://localhost:4013"));

        configureAccessProfiles(builder);
        configureStateAccess(builder);
        configureCaseView(builder);
        configureCaseFileCategories(builder);
        configureEvents(builder);
    }

    private void configureAccessProfiles(DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder) {
        for (UserRole role : UserRole.values()) {
            builder.caseRoleToAccessProfile(role)
                .accessProfiles(role.getRole())
                .legacyIdamRole();
        }
    }

    private void configureStateAccess(DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder) {
        for (BatchCaseState state : BatchCaseState.values()) {
            builder.grant(state, Permission.CRUD, UserRole.SYSTEM);
            builder.grant(state, Set.of(Permission.R, Permission.U), UserRole.CLERK);
        }
    }

    private void configureCaseFileCategories(
        DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder
    ) {
        for (BatchFileCategory category : BatchFileCategory.values()) {
            builder.categories(UserRole.CLERK)
                .categoryID(category.getId())
                .categoryLabel(category.getLabel())
                .displayOrder(category.getDisplayOrder());
        }
    }

    private void configureCaseView(DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder) {
        builder.tab("tasks", "Tasks")
            .label("tasksMarkdownLabel", null, "${tasksMarkdown}")
            .field("tasksMarkdown", NEVER_SHOW);

        builder.tab("caseDetails", "Case details")
            .field(BatchCase::getBatchValidationResultDisplay)
            .field(BatchCase::getBatchIdentifier)
            .field(BatchCase::getLocalAuthority)
            .field(BatchCase::getOperation)
            .field(BatchCase::getPcnCount)
            .field(BatchCase::getReceivedVia)
            .field(BatchCase::getReceivedAt);

        builder.tab("caseFileView", "Case File View")
            .field(BatchCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)")
            .field(BatchCase::getAllDocuments, NEVER_SHOW);

        builder.searchInputFields()
            .field(BatchCase::getBatchIdentifier, "Batch identifier")
            .field(BatchCase::getLocalAuthority, "Local authority")
            .field(BatchCase::getOperation, "Operation")
            .field(BatchCase::getReceivedVia, "Received via");

        builder.searchResultFields()
            .caseReferenceField()
            .field(BatchCase::getBatchIdentifier, "Batch identifier")
            .field(BatchCase::getLocalAuthority, "Local authority")
            .field(BatchCase::getOperation, "Operation")
            .field(BatchCase::getPcnCount, "Number of PCNs")
            .field(BatchCase::getReceivedVia, "Received via")
            .field(BatchCase::getReceivedAt, "Received at");

        builder.workBasketInputFields()
            .field(BatchCase::getBatchIdentifier, "Batch identifier")
            .field(BatchCase::getLocalAuthority, "Local authority")
            .field(BatchCase::getOperation, "Operation")
            .field(BatchCase::getReceivedVia, "Received via");

        builder.workBasketResultFields()
            .caseReferenceField()
            .field(BatchCase::getBatchIdentifier, "Batch identifier")
            .field(BatchCase::getLocalAuthority, "Local authority")
            .field(BatchCase::getOperation, "Operation")
            .field(BatchCase::getPcnCount, "Number of PCNs")
            .field(BatchCase::getReceivedVia, "Received via")
            .field(BatchCase::getReceivedAt, "Received at");
    }

    private void configureEvents(DecentralisedConfigBuilder<BatchCase, BatchCaseState, UserRole> builder) {
        builder.decentralisedEvent("createBatch", this::createBatch)
            .initialState(BatchCaseState.QUEUED_FOR_PROCESSING)
            .name("Batch created")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .grant(Permission.R, UserRole.CLERK)
            .fields()
            .mandatory(BatchCase::getBatchIdentifier)
            .mandatory(BatchCase::getPcnCount)
            .mandatory(BatchCase::getOperation)
            .mandatory(BatchCase::getReceivedVia)
            .mandatory(BatchCase::getReceivedAt)
            .mandatory(BatchCase::getLocalAuthority)
            .optional(BatchCase::getBatchValidationResult);

        builder.decentralisedEvent("startBatchProcessing", this::startBatchProcessing)
            .forStateTransition(
                BatchCaseState.QUEUED_FOR_PROCESSING,
                BatchCaseState.PROCESSING_STARTED
            )
            .name("Batch processing started")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM);

        builder.decentralisedEvent("completeBatchProcessing", this::completeBatchProcessing)
            .forStateTransition(
                BatchCaseState.PROCESSING_STARTED,
                BatchCaseState.PROCESSING_COMPLETE
            )
            .name("Batch processing complete")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM);

        builder.decentralisedEvent("attachBatchDocument", this::attachBatchDocument)
            .forStates(BatchCaseState.values())
            .name("Attach batch document")
            .showCondition(NEVER_SHOW)
            .grant(Permission.CRUD, UserRole.SYSTEM)
            .fields()
            .mandatory(BatchCase::getBatchFileDocument);
    }

    private SubmitResponse<BatchCaseState> createBatch(EventPayload<BatchCase, BatchCaseState> event) {
        BatchCase data = event.caseData();
        if (data.getBatchValidationResult() == null) {
            data.setBatchValidationResult(BatchValidationResult.BATCH_VALID);
        }
        repository.create(event.caseReference(), data);
        return response(BatchCaseState.QUEUED_FOR_PROCESSING);
    }

    private SubmitResponse<BatchCaseState> startBatchProcessing(EventPayload<BatchCase, BatchCaseState> event) {
        return response(BatchCaseState.PROCESSING_STARTED);
    }

    private SubmitResponse<BatchCaseState> completeBatchProcessing(
        EventPayload<BatchCase, BatchCaseState> event
    ) {
        return response(BatchCaseState.PROCESSING_COMPLETE);
    }

    private SubmitResponse<BatchCaseState> attachBatchDocument(EventPayload<BatchCase, BatchCaseState> event) {
        Document document = event.caseData().getBatchFileDocument();
        if (document == null) {
            throw new IllegalArgumentException("batchFileDocument is required");
        }
        if (isBlank(document.getUrl())
            || isBlank(document.getBinaryUrl())
            || isBlank(document.getFilename())) {
            throw new IllegalArgumentException(
                "batchFileDocument requires document_url, document_binary_url and document_filename"
            );
        }

        String categoryId = BatchFileCategory.normalisedCategoryId(document.getCategoryId());
        repository.insertDocument(
            event.caseReference(),
            categoryId,
            document.getUrl(),
            document.getBinaryUrl(),
            document.getFilename()
        );
        return SubmitResponse.defaultResponse();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SubmitResponse<BatchCaseState> response(BatchCaseState state) {
        return SubmitResponse.<BatchCaseState>builder().state(state).build();
    }
}
