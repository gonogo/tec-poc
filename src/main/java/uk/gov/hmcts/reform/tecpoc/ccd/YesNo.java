package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum YesNo implements HasLabel {

    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No");

    private final String label;

    YesNo(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
