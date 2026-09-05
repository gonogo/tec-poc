package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum BatchValidationResult implements HasLabel {

    @JsonProperty("batchValid")
    BATCH_VALID("Batch valid"),

    @JsonProperty("batchInvalid")
    BATCH_INVALID("Batch invalid");

    private final String label;

    BatchValidationResult(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
