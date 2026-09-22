package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.type.LinkReason;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

/**
 * Shared Linked Cases reasons for PCN ↔ batch links.
 *
 * <p>ExUI maps {@code Reason} through Reference Data {@code CaseLinkingReasonCode}
 * (e.g. {@code CLRC007} = Other). Free-text labels belong in {@code OtherDescription},
 * which ExUI renders as {@code Other - &lt;text&gt;}.
 */
final class BatchRegistrationCaseLinks {

    /** HMCTS common-data LOV key for "Other". */
    static final String REASON_CODE = "CLRC007";

    /** Free-text shown after "Other - " for registration batches. */
    static final String REASON = reasonText(BatchOperation.REGISTRATION);

    private BatchRegistrationCaseLinks() {
    }

    static String reasonText(BatchOperation operation) {
        if (operation == null) {
            return reasonText(BatchOperation.REGISTRATION);
        }
        return switch (operation) {
            case REGISTRATION -> "Linked as part of a batch of registrations";
            case WARRANT_AUTH_REQUESTS -> "Linked as part of a batch of warrant auth requests";
            case WARRANT_REISSUE_REQUESTS -> "Linked as part of a batch of warrant reissue requests";
            case OUT_OF_TIME_DECISIONS -> "Linked as part of a batch of out-of-time decisions";
            case CHANGE_OF_ADDRESS -> "Linked as part of a batch of change of address";
            case CASE_CLOSURE_REQUESTS -> "Linked as part of a batch of case closure requests";
            case TRANSFER_REQUEST -> "Linked as part of a batch of transfer requests";
        };
    }

    static List<ListValue<LinkReason>> reasonForLink() {
        return reasonForLink(BatchOperation.REGISTRATION);
    }

    static List<ListValue<LinkReason>> reasonForLink(BatchOperation operation) {
        return List.of(ListValue.<LinkReason>builder()
            .id("1")
            .value(LinkReason.builder()
                .reason(REASON_CODE)
                .description(reasonText(operation))
                .build())
            .build());
    }
}
