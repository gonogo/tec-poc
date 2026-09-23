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
        assertThat(BatchCaseView.statusLabel(BatchCaseState.PROCESSING_FAILED))
            .isEqualTo("Processing failed");
    }

    @Test
    void shouldShowEmdashForProcessedPcnsWhenQueued() {
        BatchCase batchCase = new BatchCase();
        batchCase.setPcnCount(200);

        assertThat(
            BatchCaseView.pcnProcessedCountDisplay(BatchCaseState.QUEUED_FOR_PROCESSING, batchCase)
        ).isEqualTo("—");
    }

    @Test
    void shouldShowPcnCountForProcessedPcnsWhenNotQueued() {
        BatchCase batchCase = new BatchCase();
        batchCase.setPcnCount(200);

        assertThat(
            BatchCaseView.pcnProcessedCountDisplay(BatchCaseState.PROCESSING_STARTED, batchCase)
        ).isEqualTo("200");
        assertThat(
            BatchCaseView.pcnProcessedCountDisplay(BatchCaseState.PROCESSING_COMPLETE, batchCase)
        ).isEqualTo("200");
        assertThat(
            BatchCaseView.pcnProcessedCountDisplay(BatchCaseState.PROCESSING_FAILED, batchCase)
        ).isEqualTo("200");
    }

    @Test
    void shouldShowFeesDueWhenRegistrationQueued() {
        BatchCase batchCase = new BatchCase();
        batchCase.setOperation(BatchOperation.REGISTRATION);
        batchCase.setPcnCount(200);

        BatchCaseView.applyFees(batchCase, BatchCaseState.QUEUED_FOR_PROCESSING);

        assertThat(batchCase.getFeesDue()).isEqualTo(200 * BatchCaseView.FEE_PENCE_PER_PCN);
        assertThat(batchCase.getFeesPaid()).isNull();
    }

    @Test
    void shouldShowFeesPaidWhenRegistrationComplete() {
        BatchCase batchCase = new BatchCase();
        batchCase.setOperation(BatchOperation.REGISTRATION);
        batchCase.setPcnCount(200);

        BatchCaseView.applyFees(batchCase, BatchCaseState.PROCESSING_COMPLETE);

        assertThat(batchCase.getFeesPaid()).isEqualTo(200 * BatchCaseView.FEE_PENCE_PER_PCN);
        assertThat(batchCase.getFeesDue()).isNull();
    }

    @Test
    void shouldOmitFeesWhenNotRegistrationOrNotApplicableState() {
        BatchCase warrantQueued = new BatchCase();
        warrantQueued.setOperation(BatchOperation.WARRANT_AUTH_REQUESTS);
        warrantQueued.setPcnCount(200);
        BatchCaseView.applyFees(warrantQueued, BatchCaseState.QUEUED_FOR_PROCESSING);
        assertThat(warrantQueued.getFeesDue()).isNull();
        assertThat(warrantQueued.getFeesPaid()).isNull();

        BatchCase registrationStarted = new BatchCase();
        registrationStarted.setOperation(BatchOperation.REGISTRATION);
        registrationStarted.setPcnCount(200);
        BatchCaseView.applyFees(registrationStarted, BatchCaseState.PROCESSING_STARTED);
        assertThat(registrationStarted.getFeesDue()).isNull();
        assertThat(registrationStarted.getFeesPaid()).isNull();

        BatchCase registrationFailed = new BatchCase();
        registrationFailed.setOperation(BatchOperation.REGISTRATION);
        registrationFailed.setPcnCount(200);
        BatchCaseView.applyFees(registrationFailed, BatchCaseState.PROCESSING_FAILED);
        assertThat(registrationFailed.getFeesDue()).isNull();
        assertThat(registrationFailed.getFeesPaid()).isNull();
    }

    @Test
    void shouldMapDocumentsForCaseFileView() {
        List<ListValue<Document>> allDocuments = BatchCaseView.toAllDocuments(DOCUMENTS);

        assertThat(allDocuments).hasSize(2);
        Document input = allDocuments.get(0).getValue();
        assertThat(input.getCategoryId()).isEqualTo("inputs");
        assertThat(input.getFilename()).isEqualTo("batch-input.csv");
        assertThat(input.getUrl())
            .isEqualTo("http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111");
        assertThat(input.getBinaryUrl())
            .isEqualTo(
                "http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111/binary"
            );

        Document output = allDocuments.get(1).getValue();
        assertThat(output.getCategoryId()).isEqualTo("outputs");
        assertThat(output.getFilename()).isEqualTo("exception-report.csv");
        assertThat(output.getUrl())
            .isEqualTo("http://localhost:4455/cases/documents/22222222-2222-2222-2222-222222222222");
    }
}
