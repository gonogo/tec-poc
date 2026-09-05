package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BatchCaseState {

    @CCD(label = "Queued for processing")
    QUEUED_FOR_PROCESSING,

    @CCD(label = "Processing started")
    PROCESSING_STARTED,

    @CCD(label = "Processing complete")
    PROCESSING_COMPLETE
}
