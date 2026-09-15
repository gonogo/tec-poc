package uk.gov.hmcts.reform.tecpoc.ccd;

import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

public final class TecJurisdiction {

    public static final String ID = "TEC";

    private static final String NAME = "Traffic Enforcement Centre";
    private static final String DESCRIPTION = "Traffic Enforcement Centre";

    public static <C, S, R extends HasRole> void configure(
        DecentralisedConfigBuilder<C, S, R> builder
    ) {
        builder.jurisdiction(ID, NAME, DESCRIPTION);
    }

}
