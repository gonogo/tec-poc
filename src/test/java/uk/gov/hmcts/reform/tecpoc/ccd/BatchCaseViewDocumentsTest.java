package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class BatchCaseViewDocumentsTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-05T12:00:00Z");

    private static final List<BatchCaseDocument> DOCUMENTS = List.of(
        new BatchCaseDocument(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "inputs",
            "http://localhost:4506/documents/11111111-1111-1111-1111-111111111111",
            "http://localhost:4506/documents/11111111-1111-1111-1111-111111111111/binary",
            "batch-input.csv",
            CREATED_AT
        ),
        new BatchCaseDocument(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "outputs",
            "http://localhost:4506/documents/22222222-2222-2222-2222-222222222222",
            "http://localhost:4506/documents/22222222-2222-2222-2222-222222222222/binary",
            "exception-report.csv",
            CREATED_AT
        )
    );

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
    void shouldMapInputDocumentsByCategory() {
        List<ListValue<Document>> inputs = BatchCaseView.toDocuments(
            DOCUMENTS,
            BatchFileCategory.INPUTS.getId()
        );

        assertThat(inputs).hasSize(1);
        Document input = inputs.get(0).getValue();
        assertThat(input.getCategoryId()).isEqualTo("inputs");
        assertThat(input.getFilename()).isEqualTo("batch-input.csv");
        assertThat(input.getUrl())
            .isEqualTo("http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111");
        assertThat(input.getBinaryUrl())
            .isEqualTo(
                "http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111/binary"
            );
    }

    @Test
    void shouldMapOutputDocumentsByCategory() {
        List<ListValue<Document>> outputs = BatchCaseView.toDocuments(
            DOCUMENTS,
            BatchFileCategory.OUTPUTS.getId()
        );

        assertThat(outputs).hasSize(1);
        Document output = outputs.get(0).getValue();
        assertThat(output.getCategoryId()).isEqualTo("outputs");
        assertThat(output.getFilename()).isEqualTo("exception-report.csv");
    }

    @Test
    void shouldShowDashWhenOutputsEmpty() {
        BatchCase batchCase = new BatchCase();
        BatchCaseView.applyOutputs(batchCase, List.of());

        assertThat(batchCase.getOutputDocuments()).isNull();
        assertThat(batchCase.getOutputsDisplay()).isEqualTo("-");
    }

    @Test
    void shouldShowDocumentsWhenOutputsPresent() {
        BatchCase batchCase = new BatchCase();
        List<ListValue<Document>> outputs = BatchCaseView.toDocuments(
            DOCUMENTS,
            BatchFileCategory.OUTPUTS.getId()
        );
        BatchCaseView.applyOutputs(batchCase, outputs);

        assertThat(batchCase.getOutputDocuments()).isEqualTo(outputs);
        assertThat(batchCase.getOutputsDisplay()).isNull();
    }
}
