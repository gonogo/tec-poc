package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
            "Submit registration datafile", SubmissionType.PCN_REGISTRATION);
        configureSubmission(builder, SUBMIT_WARRANT_DATAFILE,
            "Submit warrant datafile", SubmissionType.WARRANT);
        configureSubmission(builder, SUBMIT_WARRANT_REISSUE_DATAFILE,
            "Submit warrant reissue file", SubmissionType.WARRANT_REISSUE);

        configureTransition(builder, START_PROCESSING, "Start processing",
            BatchDatafileCaseState.AWAITING_PROCESSING, BatchDatafileCaseState.PROCESSING, UserRole.SYSTEM);
        configureTransition(builder, RECORD_PROCESSING_SUCCESS, "Record processing success",
            BatchDatafileCaseState.PROCESSING, BatchDatafileCaseState.COMPLETE, UserRole.SYSTEM);
        configureTransition(builder, RECORD_PROCESSING_FAILURE, "Record processing failure",
            BatchDatafileCaseState.PROCESSING, BatchDatafileCaseState.PROCESSING_FAILED, UserRole.SYSTEM);
        configureTransition(builder, RETRY_PROCESSING, "Retry processing",
            BatchDatafileCaseState.PROCESSING_FAILED, BatchDatafileCaseState.AWAITING_PROCESSING,
            UserRole.CLERK, UserRole.TEC_MANAGER);

        builder.tab("batchDetails", "Batch details")
            .field(BatchDatafileCase::getCaseState)
            .field(BatchDatafileCase::getBatchFile)
            .field(BatchDatafileCase::getSubmissionType);

        // Declare History explicitly so the SDK does not insert it before Batch details.
        builder.tab("CaseHistory", "History").field("caseHistory");

        builder.searchInputFields().caseReferenceField();
        builder.searchResultFields().caseReferenceField();
        builder.workBasketInputFields().caseReferenceField();
        builder.workBasketResultFields().caseReferenceField();
    }

    private void configureSubmission(
        DecentralisedConfigBuilder<BatchDatafileCase, BatchDatafileCaseState, UserRole> builder,
        String eventId,
        String name,
        SubmissionType submissionType
    ) {
        builder.decentralisedEvent(eventId, event -> submitDatafile(event, submissionType))
            .initialState(BatchDatafileCaseState.AWAITING_PROCESSING)
            .name(name)
            .grant(Permission.CRU, UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER, UserRole.SYSTEM)
            .showSummary()
            .endButtonLabel("Submit")
            .fields()
            .page(UPLOAD_FILE_PAGE, (details, before) -> validateBatchFile(details, submissionType))
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
        SubmissionType submissionType
    ) {
        List<String> errors = validator.validate(details.getData().getBatchFile(), submissionType);

        return AboutToStartOrSubmitResponse.<BatchDatafileCase, BatchDatafileCaseState>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }

    SubmitResponse<BatchDatafileCaseState> submitDatafile(
        EventPayload<BatchDatafileCase, BatchDatafileCaseState> event,
        SubmissionType submissionType
    ) {
        List<String> errors = validator.validate(event.caseData().getBatchFile(), submissionType);
        if (!errors.isEmpty()) {
            return SubmitResponse.<BatchDatafileCaseState>builder().errors(errors).build();
        }
        if (repository.existsById(event.caseReference())) {
            return SubmitResponse.<BatchDatafileCaseState>builder()
                .errors(List.of("A datafile has already been submitted for this case"))
                .build();
        }
        // The event determines the type; never trust a caller-supplied type.
        event.caseData().setSubmissionType(submissionType);
        repository.save(mapper.toEntity(event.caseReference(), event.caseData()));

        return SubmitResponse.<BatchDatafileCaseState>builder()
            .state(BatchDatafileCaseState.AWAITING_PROCESSING)
            .confirmationHeader("Datafile submitted")
            .confirmationBody("The datafile is awaiting processing.")
            .build();
    }

}
