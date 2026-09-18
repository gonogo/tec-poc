package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

/**
 * CCD permission profiles. Incoming IDAM/RAS roles are mapped to these names separately.
 */
@RequiredArgsConstructor
public enum AccessProfile implements HasRole {

    SYSTEM("caseworker-tec-system", Permission.CRUD),
    CLERK("caseworker-tec-clerk", Permission.CRU),
    TEC_MANAGER("caseworker-tec-manager", Permission.CRU),
    TEC_BATCH_CREATE("caseworker-tec-batch-create", Permission.CR),
    TEC_BATCH_READ("tec-batch-read", Set.of(Permission.R));

    private final String role;
    private final Set<Permission> caseTypePermissions;

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getCaseTypePermissions() {
        return Permission.toString(caseTypePermissions);
    }
}
