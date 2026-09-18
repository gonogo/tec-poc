package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.tecpoc.ccd.AccessProfile;

/** Gives the group-scoped LA reader read-only access to a case field. */
public class BatchDatafileReadAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.put(AccessProfile.TEC_BATCH_READ, Permission.R);
        return grants;
    }
}
