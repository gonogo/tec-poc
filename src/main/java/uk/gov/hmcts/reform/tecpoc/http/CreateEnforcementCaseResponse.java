package uk.gov.hmcts.reform.tecpoc.http;

import uk.gov.hmcts.reform.tecpoc.ccd.EnforcementCaseState;

public record CreateEnforcementCaseResponse(long caseReference, EnforcementCaseState state) {
}
