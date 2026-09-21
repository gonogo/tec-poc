package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.type.LinkReason;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

/**
 * Shared Linked Cases reason for PCN ↔ batch links created during batch registration.
 *
 * <p>ExUI maps {@code Reason} through Reference Data {@code CaseLinkingReasonCode}
 * (e.g. {@code CLRC007} = Other). Free-text labels belong in {@code OtherDescription},
 * which ExUI renders as {@code Other - &lt;text&gt;}.
 */
final class BatchRegistrationCaseLinks {

    /** HMCTS common-data LOV key for "Other". */
    static final String REASON_CODE = "CLRC007";

    /** Free-text shown after "Other - " in ExUI Reasons for case link. */
    static final String REASON =
        "Linked when creating the case during batch registration";

    private BatchRegistrationCaseLinks() {
    }

    static List<ListValue<LinkReason>> reasonForLink() {
        return List.of(ListValue.<LinkReason>builder()
            .id("1")
            .value(LinkReason.builder()
                .reason(REASON_CODE)
                .description(REASON)
                .build())
            .build());
    }
}
