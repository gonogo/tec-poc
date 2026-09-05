package uk.gov.hmcts.reform.tecpoc.http;

import uk.gov.hmcts.reform.tecpoc.ccd.BatchCaseState;

public record CreateBatchResponse(long caseReference, BatchCaseState state) {
}
