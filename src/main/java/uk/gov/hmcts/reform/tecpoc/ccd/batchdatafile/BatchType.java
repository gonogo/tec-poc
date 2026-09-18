package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BatchType {
    @CCD(label = "PCN registration")
    PCN_REGISTRATION,
    @CCD(label = "Warrant")
    WARRANT,
    @CCD(label = "Warrant reissue")
    WARRANT_REISSUE
}
