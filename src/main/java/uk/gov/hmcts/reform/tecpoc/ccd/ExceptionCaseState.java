package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ExceptionCaseState {

    @CCD(label = "Exception pending review", hint = "### Case number: ${[CASE_REFERENCE]}")
    EXCEPTION_PENDING_REVIEW
}
