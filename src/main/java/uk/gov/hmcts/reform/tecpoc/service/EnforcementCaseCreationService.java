package uk.gov.hmcts.reform.tecpoc.service;

import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.tecpoc.ccd.EnforcementCaseConfiguration;
import uk.gov.hmcts.reform.tecpoc.ccd.EnforcementCaseState;
import uk.gov.hmcts.reform.tecpoc.http.CreateEnforcementCaseRequest;
import uk.gov.hmcts.reform.tecpoc.http.CreateEnforcementCaseResponse;

@Service
@RequiredArgsConstructor
public class EnforcementCaseCreationService {

    private static final String EVENT_ID = "createEnforcementCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator serviceTokenGenerator;

    public CreateEnforcementCaseResponse create(CreateEnforcementCaseRequest request, String authorisation) {
        String serviceAuthorisation = serviceTokenGenerator.generate();
        StartEventResponse startEvent = coreCaseDataApi.startCase(
            authorisation,
            serviceAuthorisation,
            EnforcementCaseConfiguration.CASE_TYPE,
            EVENT_ID
        );

        var createdCase = coreCaseDataApi.submitCaseCreation(
            authorisation,
            serviceAuthorisation,
            EnforcementCaseConfiguration.CASE_TYPE,
            CaseDataContent.builder()
                .event(Event.builder()
                    .id(EVENT_ID)
                    .summary("Enforcement case created")
                    .description("Enforcement case created")
                    .build())
                .eventToken(startEvent.getToken())
                .data(request.toCaseData())
                .build()
        );

        return new CreateEnforcementCaseResponse(
            requireNonNull(createdCase.getId(), "CCD returned no case reference"),
            EnforcementCaseState.valueOf(
                requireNonNull(createdCase.getState(), "CCD returned no case state")
            )
        );
    }
}
