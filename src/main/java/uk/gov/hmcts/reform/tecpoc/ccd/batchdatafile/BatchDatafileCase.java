package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.CaseAccessGroup;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access.BatchDatafileReadAccess;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access.BatchFileAccess;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access.CaseFileViewAccess;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access.OwnershipPolicyAccess;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDatafileCase {

    @CCD(label = "Status", access = BatchDatafileReadAccess.class)
    private BatchDatafileCaseState caseState;

    @CCD(
        label = "Batch file",
        categoryID = "submittedDatafile",
        access = {BatchDatafileReadAccess.class, BatchFileAccess.class}
    )
    private Document batchFile;

    @CCD(label = "File identifier", access = BatchDatafileReadAccess.class)
    private String fileIdentifier;

    @CCD(label = "Batch identifier", access = BatchDatafileReadAccess.class)
    private String batchIdentifier;

    @CCD(label = "Local authority", access = BatchDatafileReadAccess.class)
    private String localAuthority;

    @CCD(label = "Submitter email", access = BatchDatafileReadAccess.class)
    private String submitterEmail;

    @CCD(label = "Batch type", access = BatchDatafileReadAccess.class)
    private BatchType batchType;

    @CCD(label = "Number of batches", access = BatchDatafileReadAccess.class)
    private String numberOfBatches;

    @CCD(label = "Number of PCNs", access = BatchDatafileReadAccess.class)
    private String numberOfPcns;

    @CCD(label = "Number of PCNs processed", access = BatchDatafileReadAccess.class)
    private String numberOfPcnsProcessed;

    @CCD(label = "Fees due", access = BatchDatafileReadAccess.class)
    private String feesDue;

    @CCD(label = "Received via", access = BatchDatafileReadAccess.class)
    private String receivedVia;

    @CCD(label = "Received at", access = BatchDatafileReadAccess.class)
    private String receivedAt;

    @CCD(label = "Email received at", access = BatchDatafileReadAccess.class)
    private String emailReceivedAt;

    @CCD(label = "Owning local authority organisation ID", access = BatchDatafileReadAccess.class)
    private String owningLocalAuthorityOrganisationId;

    @CCD(
        label = "Owning local authority",
        access = {BatchDatafileReadAccess.class, OwnershipPolicyAccess.class}
    )
    private OrganisationPolicy<UserRole> owningLocalAuthorityPolicy;

    @JsonProperty("CaseAccessGroups")
    // CCD's group-aware Elasticsearch filter queries the nested caseAccessGroupId. Do not mark this field
    // searchable=false: that emits an `enabled: false` object mapping and makes matching cases disappear from lists.
    @CCD(label = "Case access groups", access = BatchDatafileReadAccess.class)
    private List<ListValue<CaseAccessGroup>> caseAccessGroups;

    @CCD(label = "Generated documents", categoryID = "generatedDocuments", access = CaseFileViewAccess.class)
    private List<ListValue<Document>> generatedDocuments;

    @CCD(label = "Case file", access = CaseFileViewAccess.class)
    private ComponentLauncher caseFileView;

}
