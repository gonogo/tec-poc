package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Component
@RequiredArgsConstructor
public class BatchCaseView implements CaseView<BatchCase, BatchCaseState> {

    private static final String VALIDATION_NOT_RECORDED = "Not yet validated";
    private static final String OUTPUTS_EMPTY_DISPLAY = "-";
    private static final String PCN_PROCESSED_NOT_YET = "—";
    /** Registration fee per PCN in pence (£11.00). */
    static final int FEE_PENCE_PER_PCN = 1_100;

    private final BatchCaseRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(BatchCaseConfiguration.CASE_TYPE);
    }

    @Override
    public BatchCase getCase(CaseViewRequest<BatchCaseState> request) {
        BatchCase batchCase = repository.find(request.caseRef());
        if (batchCase.getLocalAuthority() != null) {
            batchCase.setCaseAccessCategory(batchCase.getLocalAuthority().getCode());
        }
        batchCase.setStatusDisplay(statusLabel(request.state()));
        batchCase.setPcnProcessedCountDisplay(pcnProcessedCountDisplay(request.state(), batchCase));
        applyFees(batchCase, request.state());
        batchCase.setTasksMarkdown(
            BatchPrototypeTasks.markdownFor(request.caseRef(), request.state(), batchCase)
        );
        String persistedValidationDisplay = batchCase.getBatchValidationResultDisplay();
        if (persistedValidationDisplay == null || persistedValidationDisplay.isBlank()) {
            BatchValidationResult validationResult = batchCase.getBatchValidationResult();
            batchCase.setBatchValidationResultDisplay(
                validationResult == null ? VALIDATION_NOT_RECORDED : validationResult.getLabel()
            );
        }
        List<BatchCaseDocument> documents = repository.findDocuments(request.caseRef());
        batchCase.setInputDocuments(toDocuments(documents, BatchFileCategory.INPUTS.getId()));
        applyOutputs(batchCase, toDocuments(documents, BatchFileCategory.OUTPUTS.getId()));
        return batchCase;
    }

    static String statusLabel(BatchCaseState state) {
        return switch (state) {
            case QUEUED_FOR_PROCESSING -> "Queued for processing";
            case PROCESSING_STARTED -> "Processing started";
            case PROCESSING_COMPLETE -> "Processing complete";
        };
    }

    static String pcnProcessedCountDisplay(BatchCaseState state, BatchCase batchCase) {
        if (state == BatchCaseState.QUEUED_FOR_PROCESSING) {
            return PCN_PROCESSED_NOT_YET;
        }
        Integer pcnCount = batchCase == null ? null : batchCase.getPcnCount();
        return pcnCount == null ? PCN_PROCESSED_NOT_YET : String.valueOf(pcnCount);
    }

    /**
     * Sets {@link BatchCase#getFeesPaid()} / {@link BatchCase#getFeesDue()} for registration
     * batches only. Values are MoneyGBP pence ({@value #FEE_PENCE_PER_PCN} per PCN).
     */
    static void applyFees(BatchCase batchCase, BatchCaseState state) {
        batchCase.setFeesPaid(null);
        batchCase.setFeesDue(null);
        if (batchCase.getOperation() != BatchOperation.REGISTRATION) {
            return;
        }
        Integer pcnCount = batchCase.getPcnCount();
        if (pcnCount == null) {
            return;
        }
        int feePence = pcnCount * FEE_PENCE_PER_PCN;
        if (state == BatchCaseState.QUEUED_FOR_PROCESSING) {
            batchCase.setFeesDue(feePence);
        } else if (state == BatchCaseState.PROCESSING_COMPLETE) {
            batchCase.setFeesPaid(feePence);
        }
    }

    static void applyOutputs(BatchCase batchCase, List<ListValue<Document>> outputs) {
        if (outputs.isEmpty()) {
            batchCase.setOutputDocuments(null);
            batchCase.setOutputsDisplay(OUTPUTS_EMPTY_DISPLAY);
        } else {
            batchCase.setOutputDocuments(outputs);
            batchCase.setOutputsDisplay(null);
        }
    }

    static List<ListValue<Document>> toDocuments(List<BatchCaseDocument> documents, String categoryId) {
        return documents.stream()
            .filter(document -> categoryId.equalsIgnoreCase(document.categoryId()))
            .map(BatchCaseView::toListValue)
            .toList();
    }

    private static ListValue<Document> toListValue(BatchCaseDocument document) {
        Document ccdDocument = Document.builder()
            .url(CdamDocumentUrls.toCdamUrl(document.documentUrl()))
            .binaryUrl(CdamDocumentUrls.toCdamUrl(document.documentBinaryUrl()))
            .filename(document.filename())
            .categoryId(document.categoryId())
            .uploadTimestamp(
                document.createdAt() == null
                    ? null
                    : document.createdAt().atZone(ZoneOffset.UTC).toLocalDateTime()
            )
            .build();
        return ListValue.<Document>builder()
            .id(document.id().toString())
            .value(ccdDocument)
            .build();
    }
}
