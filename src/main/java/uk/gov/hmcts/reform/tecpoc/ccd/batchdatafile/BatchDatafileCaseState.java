package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BatchDatafileCaseState {
    @CCD(label = "Awaiting processing")
    AWAITING_PROCESSING,
    @CCD(label = "Processing")
    PROCESSING,
    @CCD(label = "Complete")
    COMPLETE,
    @CCD(label = "Processing failed")
    PROCESSING_FAILED
}
