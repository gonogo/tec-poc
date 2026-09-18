package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.reform.tecpoc.ccd.TecJurisdiction;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;

@Component
public class BatchDatafileCaseConfiguration implements
    CCDConfig<BatchDatafileCase, BatchDatafileCaseState, UserRole> {

    public static final String CASE_TYPE = "TEC_BATCH_DATAFILE";
    static final String SUBMIT_REGISTRATION_DATAFILE = "submitRegistrationDatafile";
    static final String SUBMIT_WARRANT_DATAFILE = "submitWarrantDatafile";
    static final String SUBMIT_WARRANT_REISSUE_DATAFILE = "submitWarrantReissueDatafile";
    static final String START_PROCESSING = "startProcessing";
    static final String RECORD_PROCESSING_SUCCESS = "recordProcessingSuccess";
    static final String RECORD_PROCESSING_FAILURE = "recordProcessingFailure";
    static final String RETRY_PROCESSING = "retryProcessing";
    static final String UPLOAD_FILE_PAGE = "uploadFile";
    static final String VALIDATION_RESULTS_PAGE = "validationResults";
    static final String DEFAULT_CALLBACK_HOST = "http://localhost:4013";

    private final BatchDatafileCaseRepository repository;
    private final BatchDatafileCaseMapper mapper;
    private final BatchFileValidator validator;

    public BatchDatafileCaseConfiguration(
        @Lazy BatchDatafileCaseRepository repository,
        BatchDatafileCaseMapper mapper,
        BatchFileValidator validator
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public void configureDecentralised(
        DecentralisedConfigBuilder<BatchDatafileCase, BatchDatafileCaseState, UserRole> builder
    ) {
        builder.setCallbackHost(
            System.getenv().getOrDefault("CASE_API_URL", DEFAULT_CALLBACK_HOST)
        );
        builder.caseType(
            CASE_TYPE, "TEC Batch Datafile Case",
            "Represents a PCN registration, warrant or warrant reissue datafile");
        TecJurisdiction.configure(builder);

        for (BatchDatafileCaseState state : BatchDatafileCaseState.values()) {
            builder.grant(state, Permission.CRU, UserRole.SYSTEM);
            builder.grant(state, Set.of(Permission.R), UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER);
        }

        configureSubmission(builder, SUBMIT_REGISTRATION_DATAFILE,
            "Submit registration datafile", BatchType.PCN_REGISTRATION);
        configureSubmission(builder, SUBMIT_WARRANT_DATAFILE,
            "Submit warrant datafile", BatchType.WARRANT);
        configureSubmission(builder, SUBMIT_WARRANT_REISSUE_DATAFILE,
            "Submit warrant reissue file", BatchType.WARRANT_REISSUE);

        configureTransition(builder, START_PROCESSING, "Start processing",
            BatchDatafileCaseState.AWAITING_PROCESSING, BatchDatafileCaseState.PROCESSING, UserRole.SYSTEM);
        configureTransition(builder, RECORD_PROCESSING_SUCCESS, "Record processing success",
            BatchDatafileCaseState.PROCESSING, BatchDatafileCaseState.COMPLETE, UserRole.SYSTEM);
        configureTransition(builder, RECORD_PROCESSING_FAILURE, "Record processing failure",
            BatchDatafileCaseState.PROCESSING, BatchDatafileCaseState.PROCESSING_FAILED, UserRole.SYSTEM);
        configureTransition(builder, RETRY_PROCESSING, "Retry processing",
            BatchDatafileCaseState.PROCESSING_FAILED, BatchDatafileCaseState.AWAITING_PROCESSING,
            UserRole.CLERK, UserRole.TEC_MANAGER);

        builder.tab("datafileDetails", "Datafile Details")
            .field(BatchDatafileCase::getCaseState)
            .field(BatchDatafileCase::getFileIdentifier)
            .field(BatchDatafileCase::getBatchIdentifier)
            .field(BatchDatafileCase::getLocalAuthority)
            .field(BatchDatafileCase::getSubmitterEmail)
            .field(BatchDatafileCase::getBatchType)
            .field(BatchDatafileCase::getNumberOfBatches)
            .field(BatchDatafileCase::getNumberOfPcns)
            .field(BatchDatafileCase::getNumberOfPcnsProcessed)
            .field(BatchDatafileCase::getFeesDue)
            .field(BatchDatafileCase::getReceivedVia)
            .field(BatchDatafileCase::getEmailReceivedAt);

        builder.tab("caseFile", "Case file")
            .forRoles(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER)
            .field(BatchDatafileCase::getCaseFileView, null, "#ARGUMENT(CaseFileView)");

        // Declare History explicitly so the SDK does not insert it before Datafile Details.
        builder.tab("CaseHistory", "History").field("caseHistory");

        for (CaseFileCategory category : CaseFileCategory.values()) {
            builder.categories(UserRole.SYSTEM)
                .categoryID(category.getId())
                .categoryLabel(category.getLabel())
                .displayOrder(category.getDisplayOrder())
                .build();
        }

        // Jurisdiction, case type and state are standard ExUI search selectors.
        builder.searchInputFields()
            .field(BatchDatafileCase::getFileIdentifier, "File identifier")
            .field(BatchDatafileCase::getBatchIdentifier, "Batch identifier")
            .field(BatchDatafileCase::getLocalAuthority, "Local authority")
            .field(BatchDatafileCase::getSubmitterEmail, "Submitter email")
            .field(BatchDatafileCase::getBatchType, "Batch type")
            .field(BatchDatafileCase::getReceivedVia, "Received via");
        builder.searchResultFields()
            .caseReferenceField()
            .field("[STATE]", "State")
            .field(BatchDatafileCase::getFileIdentifier, "File identifier")
            .field(BatchDatafileCase::getLocalAuthority, "Local authority")
            .field(BatchDatafileCase::getBatchType, "Batch type")
            .field(BatchDatafileCase::getNumberOfPcns, "Number of PCNs in batch")
            .field(BatchDatafileCase::getSubmitterEmail, "Submitter email")
            .field(BatchDatafileCase::getReceivedVia, "Received via")
            .field(BatchDatafileCase::getReceivedAt, "Received at");
        builder.workBasketInputFields().caseReferenceField();
        builder.workBasketResultFields().caseReferenceField();
    }

    private void configureSubmission(
        DecentralisedConfigBuilder<BatchDatafileCase, BatchDatafileCaseState, UserRole> builder,
        String eventId,
        String name,
        BatchType batchType
    ) {
        builder.decentralisedEvent(eventId, event -> submitDatafile(event, batchType))
            .initialState(BatchDatafileCaseState.AWAITING_PROCESSING)
            .name(name)
            .grant(Permission.CRU, UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER, UserRole.SYSTEM)
            .showSummary()
            .endButtonLabel("Submit")
            .fields()
            .page(UPLOAD_FILE_PAGE, (details, before) -> validateBatchFile(details, batchType))
            .pageLabel("Upload batch file")
            .mandatory(BatchDatafileCase::getBatchFile)
            .page(VALIDATION_RESULTS_PAGE)
            .pageLabel("File validation complete")
            .label(
                "validationSuccessful",
                "## File validation successful\n\nThe file is ready to be submitted."
            );
    }

    private void configureTransition(
        DecentralisedConfigBuilder<BatchDatafileCase, BatchDatafileCaseState, UserRole> builder,
        String eventId,
        String name,
        BatchDatafileCaseState source,
        BatchDatafileCaseState target,
        UserRole... roles
    ) {
        builder.decentralisedEvent(eventId, event -> recordProcessingTransition(event.caseReference(), target))
            .forStateTransition(source, target)
            .name(name)
            .grant(Permission.CRU, roles);
    }

    private SubmitResponse<BatchDatafileCaseState> recordProcessingTransition(
        long caseReference, BatchDatafileCaseState target
    ) {
        repository.findById(caseReference)
            .orElseThrow(() -> new IllegalStateException("Batch datafile case not found: " + caseReference));
        return SubmitResponse.<BatchDatafileCaseState>builder().state(target).build();
    }

    AboutToStartOrSubmitResponse<BatchDatafileCase, BatchDatafileCaseState> validateBatchFile(
        CaseDetails<BatchDatafileCase, BatchDatafileCaseState> details,
        BatchType batchType
    ) {
        List<String> errors = validator.validate(details.getData().getBatchFile(), batchType);

        return AboutToStartOrSubmitResponse.<BatchDatafileCase, BatchDatafileCaseState>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }

    SubmitResponse<BatchDatafileCaseState> submitDatafile(
        EventPayload<BatchDatafileCase, BatchDatafileCaseState> event,
        BatchType batchType
    ) {
        List<String> errors = validator.validate(event.caseData().getBatchFile(), batchType);
        if (!errors.isEmpty()) {
            return SubmitResponse.<BatchDatafileCaseState>builder().errors(errors).build();
        }
        if (repository.existsById(event.caseReference())) {
            return SubmitResponse.<BatchDatafileCaseState>builder()
                .errors(List.of("A datafile has already been submitted for this case"))
                .build();
        }
        // The event determines the type; never trust a caller-supplied type.
        event.caseData().setBatchType(batchType);
        repository.save(mapper.toEntity(event.caseReference(), event.caseData()));

        return SubmitResponse.<BatchDatafileCaseState>builder()
            .state(BatchDatafileCaseState.AWAITING_PROCESSING)
            .confirmationHeader("Datafile submitted")
            .confirmationBody("The datafile is awaiting processing.")
            .build();
    }

}
