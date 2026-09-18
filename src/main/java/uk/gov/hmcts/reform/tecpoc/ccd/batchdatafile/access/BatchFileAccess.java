package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.tecpoc.ccd.AccessProfile;

/** Access to the only LA-supplied case field. */
public class BatchFileAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(AccessProfile.SYSTEM, Permission.CRU);
        grants.putAll(AccessProfile.CLERK, Permission.CRU);
        grants.putAll(AccessProfile.TEC_MANAGER, Permission.CRU);
        grants.putAll(AccessProfile.TEC_BATCH_CREATE, Permission.CR);
        grants.put(AccessProfile.TEC_BATCH_READ, Permission.R);
        return grants;
    }
}
