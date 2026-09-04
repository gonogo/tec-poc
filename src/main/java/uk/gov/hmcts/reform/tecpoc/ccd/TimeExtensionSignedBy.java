package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum TimeExtensionSignedBy implements HasLabel {

    @JsonProperty("respondent")
    RESPONDENT("Respondent"),

    @JsonProperty("officerOfTheCompany")
    OFFICER_OF_THE_COMPANY("An officer of the company"),

    @JsonProperty("partnerOfTheFirm")
    PARTNER_OF_THE_FIRM("A Partner of the firm"),

    @JsonProperty("litigationFriend")
    LITIGATION_FRIEND("Litigation friend");

    private final String label;

    TimeExtensionSignedBy(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
