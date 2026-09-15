package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@RequiredArgsConstructor
public enum UserRole implements HasRole {

    SYSTEM("caseworker-tec-system", Permission.CRUD),
    CLERK("caseworker-tec", Permission.CRU),
    TEC_MANAGER("caseworker-tec-manager", Permission.CRU),
    LA_USER("caseworker-tec-la-user", Permission.CRU);

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
