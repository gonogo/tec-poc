package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum EnforcementCaseState {

    @CCD(label = "Open", hint = "### Case number: ${[CASE_REFERENCE]}")
    OPEN
}
