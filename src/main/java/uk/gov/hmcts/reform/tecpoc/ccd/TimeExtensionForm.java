package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum TimeExtensionForm implements HasLabel {

    @JsonProperty("TE7")
    TE7("TE7"),

    @JsonProperty("PE2")
    PE2("PE2");

    private final String label;

    TimeExtensionForm(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
