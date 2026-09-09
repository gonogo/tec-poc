package uk.gov.hmcts.reform.tecpoc.service;

import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.tecpoc.ccd.ExceptionCaseConfiguration;
import uk.gov.hmcts.reform.tecpoc.ccd.ExceptionCaseState;
import uk.gov.hmcts.reform.tecpoc.http.CreateExceptionCaseRequest;
import uk.gov.hmcts.reform.tecpoc.http.CreateExceptionCaseResponse;

@Service
@RequiredArgsConstructor
public class ExceptionCaseCreationService {

    private static final String EVENT_ID = "createExceptionCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator serviceTokenGenerator;

    public CreateExceptionCaseResponse create(CreateExceptionCaseRequest request, String authorisation) {
        String serviceAuthorisation = serviceTokenGenerator.generate();
        StartEventResponse startEvent = coreCaseDataApi.startCase(
            authorisation,
            serviceAuthorisation,
            ExceptionCaseConfiguration.CASE_TYPE,
            EVENT_ID
        );

        var createdCase = coreCaseDataApi.submitCaseCreation(
            authorisation,
            serviceAuthorisation,
            ExceptionCaseConfiguration.CASE_TYPE,
            CaseDataContent.builder()
                .event(Event.builder()
                    .id(EVENT_ID)
                    .summary("Exception case created")
                    .description("Exception case created")
                    .build())
                .eventToken(startEvent.getToken())
                .data(request.toCaseData())
                .build()
        );

        return new CreateExceptionCaseResponse(
            requireNonNull(createdCase.getId(), "CCD returned no case reference"),
            ExceptionCaseState.valueOf(
                requireNonNull(createdCase.getState(), "CCD returned no case state")
            )
        );
    }
}
