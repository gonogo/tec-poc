package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum BatchOperation implements HasLabel {

    @JsonProperty("registration")
    REGISTRATION(
        "Registration",
        "Create new PCN registrations from an uploaded batch file"
    ),

    @JsonProperty("warrantAuthRequests")
    WARRANT_AUTH_REQUESTS(
        "Warrant auth requests",
        "Submit PCNs for warrant authorisation"
    ),

    @JsonProperty("warrantReissueRequests")
    WARRANT_REISSUE_REQUESTS(
        "Warrant reissue requests",
        "Request reissue of warrants for PCNs"
    ),

    @JsonProperty("outOfTimeDecisions")
    OUT_OF_TIME_DECISIONS(
        "Out-of-time decisions",
        "Submit out-of-time application decisions"
    ),

    @JsonProperty("changeOfAddress")
    CHANGE_OF_ADDRESS(
        "Change of address",
        "Update respondent addresses from a batch file"
    ),

    @JsonProperty("caseClosureRequests")
    CASE_CLOSURE_REQUESTS(
        "Case closure requests",
        "Request closure of PCN cases in bulk"
    );

    private final String shortLabel;
    private final String description;

    BatchOperation(String shortLabel, String description) {
        this.shortLabel = shortLabel;
        this.description = description;
    }

    /**
     * Radio / list label. CCD FixedRadioList has no per-option hint, so the short
     * description is appended for the Create batch journey.
     */
    @Override
    public String getLabel() {
        return shortLabel + " — " + description;
    }

    public String getShortLabel() {
        return shortLabel;
    }
}
