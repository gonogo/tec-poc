package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;

public class CaseFileViewAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.put(UserRole.LA_USER, Permission.R);
        grants.put(UserRole.CLERK, Permission.R);
        grants.put(UserRole.TEC_MANAGER, Permission.R);
        grants.put(UserRole.SYSTEM, Permission.R);
        return grants;
    }
}
