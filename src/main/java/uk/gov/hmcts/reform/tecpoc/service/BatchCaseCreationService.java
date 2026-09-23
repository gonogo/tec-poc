package uk.gov.hmcts.reform.tecpoc.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchCaseConfiguration;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchCaseState;
import uk.gov.hmcts.reform.tecpoc.http.CreateBatchRequest;
import uk.gov.hmcts.reform.tecpoc.http.CreateBatchResponse;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class BatchCaseCreationService {

    private static final String CREATE_EVENT_ID = "createBatch";
    private static final String START_PROCESSING_EVENT_ID = "startBatchProcessing";
    private static final String COMPLETE_PROCESSING_EVENT_ID = "completeBatchProcessing";
    private static final String FAIL_PROCESSING_EVENT_ID = "failBatchProcessing";
    private static final String ATTACH_DOCUMENT_EVENT_ID = "attachBatchDocument";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator serviceTokenGenerator;

    public CreateBatchResponse create(CreateBatchRequest request, String authorisation) {
        String serviceAuthorisation = serviceTokenGenerator.generate();
        StartEventResponse startEvent = coreCaseDataApi.startCase(
            authorisation,
            serviceAuthorisation,
            BatchCaseConfiguration.CASE_TYPE,
            CREATE_EVENT_ID
        );

        CaseDetails createdCase = coreCaseDataApi.submitCaseCreation(
            authorisation,
            serviceAuthorisation,
            BatchCaseConfiguration.CASE_TYPE,
            CaseDataContent.builder()
                .event(Event.builder()
                    .id(CREATE_EVENT_ID)
                    .summary("Batch created")
                    .description("Batch created")
                    .build())
                .eventToken(startEvent.getToken())
                .data(request.toCaseData())
                .build()
        );

        long caseReference = requireNonNull(createdCase.getId(), "CCD returned no case reference");
        BatchCaseState state = BatchCaseState.valueOf(
            requireNonNull(createdCase.getState(), "CCD returned no case state")
        );

        BatchCaseState targetState = request.resolvedTargetState();
        if (targetState == BatchCaseState.PROCESSING_STARTED
            || targetState == BatchCaseState.PROCESSING_COMPLETE
            || targetState == BatchCaseState.PROCESSING_FAILED) {
            state = triggerTransition(
                authorisation,
                serviceAuthorisation,
                caseReference,
                START_PROCESSING_EVENT_ID,
                "Batch processing started"
            );
        }
        if (targetState == BatchCaseState.PROCESSING_COMPLETE) {
            state = triggerTransition(
                authorisation,
                serviceAuthorisation,
                caseReference,
                COMPLETE_PROCESSING_EVENT_ID,
                "Batch processing complete"
            );
        }
        if (targetState == BatchCaseState.PROCESSING_FAILED) {
            state = triggerTransition(
                authorisation,
                serviceAuthorisation,
                caseReference,
                FAIL_PROCESSING_EVENT_ID,
                "Batch processing failed"
            );
        }

        attachDocuments(authorisation, serviceAuthorisation, caseReference, request.documents());

        return new CreateBatchResponse(caseReference, state);
    }

    private BatchCaseState triggerTransition(
        String authorisation,
        String serviceAuthorisation,
        long caseReference,
        String eventId,
        String summary
    ) {
        String caseId = Long.toString(caseReference);
        StartEventResponse startEvent = coreCaseDataApi.startEvent(
            authorisation,
            serviceAuthorisation,
            caseId,
            eventId
        );
        CaseResource updated = coreCaseDataApi.createEvent(
            authorisation,
            serviceAuthorisation,
            caseId,
            CaseDataContent.builder()
                .event(Event.builder()
                    .id(eventId)
                    .summary(summary)
                    .description(summary)
                    .build())
                .eventToken(startEvent.getToken())
                .data(Map.of())
                .build()
        );
        return BatchCaseState.valueOf(requireNonNull(updated.getState(), "CCD returned no case state"));
    }

    private void attachDocuments(
        String authorisation,
        String serviceAuthorisation,
        long caseReference,
        List<CreateBatchRequest.BatchDocumentSeed> documents
    ) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        String caseId = Long.toString(caseReference);
        for (CreateBatchRequest.BatchDocumentSeed document : documents) {
            StartEventResponse startEvent = coreCaseDataApi.startEvent(
                authorisation,
                serviceAuthorisation,
                caseId,
                ATTACH_DOCUMENT_EVENT_ID
            );
            Map<String, Object> documentField = new LinkedHashMap<>();
            documentField.put("document_url", document.documentUrl());
            documentField.put("document_binary_url", document.documentBinaryUrl());
            documentField.put("document_filename", document.filename());
            documentField.put("category_id", document.categoryId());

            coreCaseDataApi.createEvent(
                authorisation,
                serviceAuthorisation,
                caseId,
                CaseDataContent.builder()
                    .event(Event.builder()
                        .id(ATTACH_DOCUMENT_EVENT_ID)
                        .summary("Attach batch document")
                        .description("Attach batch document")
                        .build())
                    .eventToken(startEvent.getToken())
                    .data(Map.of("batchFileDocument", documentField))
                    .build()
            );
        }
    }
}
