package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum ApplicationDeclaration implements HasLabel {

    @JsonProperty("didNotReceivePcn")
    DID_NOT_RECEIVE_PCN("Did not receive the penalty charge notice"),

    @JsonProperty("madeRepresentationsNoRejection")
    MADE_REPRESENTATIONS_NO_REJECTION(
        "Made representations but did not receive a rejection notice"
    ),

    @JsonProperty("appealedToAdjudicator")
    APPEALED_TO_ADJUDICATOR(
        "Appealed to an adjudicator (no response / not determined / determined in favour)"
    ),

    @JsonProperty("paidInFull")
    PAID_IN_FULL("The penalty charge has been paid in full"),

    @JsonProperty("didNotReceiveNotice")
    DID_NOT_RECEIVE_NOTICE(
        "Did not receive the Notice to Owner / Enforcement Notice / Penalty Charge Notice"
    ),

    @JsonProperty("appealedNoResponse")
    APPEALED_NO_RESPONSE("Appealed but had no response to the appeal");

    private final String label;

    ApplicationDeclaration(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
