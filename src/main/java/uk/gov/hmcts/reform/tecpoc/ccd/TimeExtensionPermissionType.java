package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum TimeExtensionPermissionType implements HasLabel {

    @JsonProperty("outsideTheGivenTime")
    OUTSIDE_THE_GIVEN_TIME("Outside the given time"),

    @JsonProperty("forMoreTime")
    FOR_MORE_TIME("For more time");

    private final String label;

    TimeExtensionPermissionType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
