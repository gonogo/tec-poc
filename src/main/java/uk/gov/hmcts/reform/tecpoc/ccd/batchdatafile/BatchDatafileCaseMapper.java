package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.Objects;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;

@Component
public class BatchDatafileCaseMapper {

    public BatchDatafileCaseEntity toEntity(long caseReference, BatchDatafileCase batchBatchDatafileCase) {
        Document batchFile = Objects.requireNonNull(
            batchBatchDatafileCase.getBatchFile(),
            "Batch file is required"
        );

        return BatchDatafileCaseEntity.builder()
            .caseReference(caseReference)
            .submissionType(batchBatchDatafileCase.getSubmissionType())
            .batchFileUrl(batchFile.getUrl())
            .batchFileName(batchFile.getFilename())
            .build();
    }

    public BatchDatafileCase toCase(BatchDatafileCaseEntity entity) {
        Document batchFile = Document.builder()
            .url(entity.getBatchFileUrl())
            .filename(entity.getBatchFileName())
            .binaryUrl(entity.getBatchFileUrl() + "/binary")
            .build();

        BatchDatafileCase batchBatchDatafileCase = new BatchDatafileCase();
        batchBatchDatafileCase.setBatchFile(batchFile);
        batchBatchDatafileCase.setSubmissionType(entity.getSubmissionType());
        return batchBatchDatafileCase;
    }
}
