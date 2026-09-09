package uk.gov.hmcts.reform.tecpoc.http;

import uk.gov.hmcts.reform.tecpoc.ccd.ExceptionCaseState;

public record CreateExceptionCaseResponse(long caseReference, ExceptionCaseState state) {
}
