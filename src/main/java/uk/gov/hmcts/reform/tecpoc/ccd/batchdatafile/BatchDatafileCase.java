package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.access.CaseFileViewAccess;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDatafileCase {

    @CCD(label = "Status")
    private BatchDatafileCaseState caseState;

    @CCD(label = "Batch file", categoryID = "submittedDatafile")
    private Document batchFile;

    @CCD(label = "File identifier")
    private String fileIdentifier;

    @CCD(label = "Batch identifier")
    private String batchIdentifier;

    @CCD(label = "Local authority")
    private String localAuthority;

    @CCD(label = "Submitter email")
    private String submitterEmail;

    @CCD(label = "Batch type")
    private BatchType batchType;

    @CCD(label = "Number of batches")
    private String numberOfBatches;

    @CCD(label = "Number of PCNs")
    private String numberOfPcns;

    @CCD(label = "Number of PCNs processed")
    private String numberOfPcnsProcessed;

    @CCD(label = "Fees due")
    private String feesDue;

    @CCD(label = "Received via")
    private String receivedVia;

    @CCD(label = "Received at")
    private String receivedAt;

    @CCD(label = "Email received at")
    private String emailReceivedAt;

    @CCD(label = "Generated documents", categoryID = "generatedDocuments", access = CaseFileViewAccess.class)
    private List<ListValue<Document>> generatedDocuments;

    @CCD(label = "Case file", access = CaseFileViewAccess.class)
    private ComponentLauncher caseFileView;

}
