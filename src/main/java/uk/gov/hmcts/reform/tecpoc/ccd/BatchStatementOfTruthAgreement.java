package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Single-option MultiSelectList used as a checkbox on the Statement of truth page
 * (same pattern as PCS {@code StatementOfTruthAgreementClaimant}).
 */
public enum BatchStatementOfTruthAgreement implements HasLabel {

    @JsonProperty("believeTrue")
    BELIEVE_TRUE("I believe that the facts stated in this batch request are true.");

    private final String label;

    BatchStatementOfTruthAgreement(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
