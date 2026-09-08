package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Batch type values for case data and Case list / Find case filters ({@code FixedList}).
 * Create batch radios use {@link BatchTypeOption}.
 * ExUI prepends an empty “Any”/unset option to FixedList filter dropdowns.
 */
public enum BatchOperation implements HasLabel {

    @JsonProperty("registration")
    REGISTRATION("Registration"),

    @JsonProperty("warrantAuthRequests")
    WARRANT_AUTH_REQUESTS("Warrant auth requests"),

    @JsonProperty("warrantReissueRequests")
    WARRANT_REISSUE_REQUESTS("Warrant reissue requests"),

    @JsonProperty("outOfTimeDecisions")
    OUT_OF_TIME_DECISIONS("Out-of-time decisions"),

    @JsonProperty("changeOfAddress")
    CHANGE_OF_ADDRESS("Change of address"),

    @JsonProperty("caseClosureRequests")
    CASE_CLOSURE_REQUESTS("Case closure requests");

    private final String label;

    BatchOperation(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return label;
    }
}
