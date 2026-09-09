package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

public enum ExceptionRejectReason implements HasLabel {

    @JsonProperty("pcnCouldNotBeMatched")
    PCN_COULD_NOT_BE_MATCHED("PCN could not be matched"),

    @JsonProperty("itemNotRelevantToTecCase")
    ITEM_NOT_RELEVANT_TO_TEC_CASE("Item is not relevant to a TEC case"),

    @JsonProperty("other")
    OTHER("other");

    private final String label;

    ExceptionRejectReason(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
