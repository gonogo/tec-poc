package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDatafileCase {

    @CCD(label = "State")
    private BatchDatafileCaseState caseState;

    @CCD(label = "Batch file")
    private Document batchFile;

    @CCD(label = "Submission type")
    private SubmissionType submissionType;

}
