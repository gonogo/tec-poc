package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum WarrantAuthorisationStatus implements HasLabel {

    @JsonProperty("active")
    ACTIVE("Active"),

    @JsonProperty("expired")
    EXPIRED("Expired"),

    @JsonProperty("cancelled")
    CANCELLED("Cancelled");

    private final String label;

    WarrantAuthorisationStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
