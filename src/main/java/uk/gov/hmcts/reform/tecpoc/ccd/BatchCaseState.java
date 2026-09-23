package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BatchCaseState {

    @CCD(label = "Queued for processing", hint = "### Batch number: ${[CASE_REFERENCE]}")
    QUEUED_FOR_PROCESSING,

    @CCD(label = "Processing started", hint = "### Batch number: ${[CASE_REFERENCE]}")
    PROCESSING_STARTED,

    @CCD(label = "Processing complete", hint = "### Batch number: ${[CASE_REFERENCE]}")
    PROCESSING_COMPLETE,

    @CCD(label = "Processing failed", hint = "### Batch number: ${[CASE_REFERENCE]}")
    PROCESSING_FAILED
}
