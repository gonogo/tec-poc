package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseFileCategory {

    SUBMITTED_DATAFILE("submittedDatafile", "Submitted datafile", 1),
    GENERATED_DOCUMENTS("generatedDocuments", "Generated documents", 2);

    private final String id;
    private final String label;
    private final int displayOrder;
}
