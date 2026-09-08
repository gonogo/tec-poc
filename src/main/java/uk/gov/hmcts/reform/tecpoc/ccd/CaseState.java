package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CaseState {

    @CCD(label = "Pending Case Issued", hint = "### Case number: ${[CASE_REFERENCE]}")
    PENDING_CASE_ISSUED,

    @CCD(label = "Case Issued", hint = "### Case number: ${[CASE_REFERENCE]}")
    CASE_ISSUED,

    @CCD(label = "Awaiting Respondent Response", hint = "### Case number: ${[CASE_REFERENCE]}")
    AWAITING_RESPONDENT_RESPONSE,

    @CCD(label = "Closed", hint = "### Case number: ${[CASE_REFERENCE]}")
    CLOSED
}
