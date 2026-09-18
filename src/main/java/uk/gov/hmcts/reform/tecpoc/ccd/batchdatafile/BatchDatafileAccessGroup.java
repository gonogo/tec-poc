package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import uk.gov.hmcts.ccd.sdk.type.CaseAccessGroup;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@UtilityClass
public class BatchDatafileAccessGroup {

    public static final String ACCESS_TYPE = "tec-batch-access";
    public static final String ORGANISATION_PROFILE = "LOCAL_AUTHORITY_PROFILE";
    public static final String GROUP_ID_TEMPLATE =
        "TEC:TEC_BATCH_DATAFILE:tec-batch-access:tec-batch-reader:$ORGID$";
    public static final String GROUP_TYPE = "CCD:all-cases-access";

    public static String groupId(String organisationId) {
        return GROUP_ID_TEMPLATE.replace("$ORGID$", organisationId);
    }

    public static List<ListValue<CaseAccessGroup>> projection(String organisationId) {
        String groupId = groupId(organisationId);
        String itemId = UUID.nameUUIDFromBytes(groupId.getBytes(StandardCharsets.UTF_8)).toString();
        return List.of(ListValue.<CaseAccessGroup>builder()
            .id(itemId)
            .value(CaseAccessGroup.builder()
                .caseAccessGroupType(GROUP_TYPE)
                .caseAccessGroupId(groupId)
                .build())
            .build());
    }
}
