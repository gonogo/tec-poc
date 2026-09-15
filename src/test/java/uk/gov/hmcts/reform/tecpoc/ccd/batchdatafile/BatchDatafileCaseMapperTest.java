package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.assertj.core.api.Assertions.assertThat;

class BatchDatafileCaseMapperTest {

    private final BatchDatafileCaseMapper mapper = new BatchDatafileCaseMapper();

    @Test
    void shouldMapDocumentMetadataToEntityAndBackWithoutCategoryOrHashToken() {
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 9, 15, 10, 30);
        Document document = Document.builder()
            .url("http://document-store/documents/68c89c98-399a-4eb4-b721-fd1d2109867f")
            .filename("batch.csv")
            .binaryUrl("http://document-store/documents/68c89c98-399a-4eb4-b721-fd1d2109867f/binary")
            .categoryId("uncategorisedDocuments")
            .uploadTimestamp(uploadedAt)
            .hashToken("transient-hash")
            .build();
        BatchDatafileCase batchBatchDatafileCase = new BatchDatafileCase();
        batchBatchDatafileCase.setBatchFile(document);
        batchBatchDatafileCase.setSubmissionType(SubmissionType.WARRANT_REISSUE);

        BatchDatafileCaseEntity entity = mapper.toEntity(1234L, batchBatchDatafileCase);
        BatchDatafileCase mappedBatchDatafileCase = mapper.toCase(entity);

        assertThat(entity.getCaseReference()).isEqualTo(1234L);
        assertThat(entity.getSubmissionType()).isEqualTo(SubmissionType.WARRANT_REISSUE);
        assertThat(mappedBatchDatafileCase.getSubmissionType()).isEqualTo(SubmissionType.WARRANT_REISSUE);
        assertThat(entity.getBatchFileName()).isEqualTo("batch.csv");
        assertThat(mappedBatchDatafileCase.getBatchFile())
            .extracting(
                Document::getUrl,
                Document::getFilename,
                Document::getBinaryUrl
            )
            .containsExactly(
                document.getUrl(),
                "batch.csv",
                document.getBinaryUrl()
            );
        assertThat(mappedBatchDatafileCase.getBatchFile().getHashToken()).isNull();
        assertThat(mappedBatchDatafileCase.getBatchFile().getCategoryId()).isNull();
    }
}
