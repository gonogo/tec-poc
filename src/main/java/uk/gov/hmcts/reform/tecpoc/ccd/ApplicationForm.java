package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum ApplicationForm implements HasLabel {

    @JsonProperty("TE9")
    TE9("TE9"),

    @JsonProperty("PE3")
    PE3("PE3");

    private final String label;

    ApplicationForm(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
