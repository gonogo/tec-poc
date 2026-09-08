package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class BatchCaseViewDocumentsTest {

    @Test
    void shouldMapStatusLabels() {
        assertThat(BatchCaseView.statusLabel(BatchCaseState.QUEUED_FOR_PROCESSING))
            .isEqualTo("Queued for processing");
        assertThat(BatchCaseView.statusLabel(BatchCaseState.PROCESSING_STARTED))
            .isEqualTo("Processing started");
        assertThat(BatchCaseView.statusLabel(BatchCaseState.PROCESSING_COMPLETE))
            .isEqualTo("Processing complete");
    }

    @Test
    void shouldMapDocumentsForCaseFileView() {
        Instant createdAt = Instant.parse("2026-09-05T12:00:00Z");
        List<ListValue<Document>> allDocuments = BatchCaseView.toAllDocuments(List.of(
            new BatchCaseDocument(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "inputs",
                "http://localhost:4506/documents/input",
                "http://localhost:4506/documents/input/binary",
                "batch-input.csv",
                createdAt
            ),
            new BatchCaseDocument(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "outputs",
                "http://localhost:4506/documents/output",
                "http://localhost:4506/documents/output/binary",
                "exception-report.csv",
                createdAt
            )
        ));

        assertThat(allDocuments).hasSize(2);
        Document input = allDocuments.get(0).getValue();
        assertThat(input.getCategoryId()).isEqualTo("inputs");
        assertThat(input.getFilename()).isEqualTo("batch-input.csv");
        assertThat(input.getUploadTimestamp())
            .isEqualTo(createdAt.atZone(java.time.ZoneOffset.UTC).toLocalDateTime());

        Document output = allDocuments.get(1).getValue();
        assertThat(output.getCategoryId()).isEqualTo("outputs");
        assertThat(output.getFilename()).isEqualTo("exception-report.csv");
    }
}
