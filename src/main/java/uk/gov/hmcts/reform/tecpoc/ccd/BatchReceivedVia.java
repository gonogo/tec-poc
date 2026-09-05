package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum BatchReceivedVia implements HasLabel {

    @JsonProperty("email")
    EMAIL("Email"),

    @JsonProperty("upload")
    UPLOAD("Upload");

    private final String label;

    BatchReceivedVia(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
