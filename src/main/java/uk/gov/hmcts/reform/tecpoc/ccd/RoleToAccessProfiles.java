package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

/** Maps incoming Access Management roles to CCD permission profiles. */
public final class RoleToAccessProfiles {

    private RoleToAccessProfiles() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void configure(ConfigBuilder<?, ?, AccessProfile> builder) {
        // The SDK model accepts HasRole here, although the public builder method is over-constrained
        // to the case configuration's role enum. Keep the unchecked bridge in this one place.
        ConfigBuilder untypedBuilder = builder;
        untypedBuilder.caseRoleToAccessProfile(UserRole.TEC_BATCH_SUBMITTER)
            .accessProfiles(AccessProfile.TEC_BATCH_CREATE.getRole());
        untypedBuilder.caseRoleToAccessProfile(UserRole.TEC_BATCH_READER)
            .accessProfiles(AccessProfile.TEC_BATCH_READ.getRole())
            .readonly();
    }
}
