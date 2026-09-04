package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum ApplicationTimeliness implements HasLabel {

    @JsonProperty("inTime")
    IN_TIME("In time"),

    @JsonProperty("outOfTime")
    OUT_OF_TIME("Out of time");

    private final String label;

    ApplicationTimeliness(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
