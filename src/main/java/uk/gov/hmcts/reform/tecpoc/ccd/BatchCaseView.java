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

    private final BatchCaseRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(BatchCaseConfiguration.CASE_TYPE);
    }

    @Override
    public BatchCase getCase(CaseViewRequest<BatchCaseState> request) {
        BatchCase batchCase = repository.find(request.caseRef());
        batchCase.setStatusDisplay(statusLabel(request.state()));
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
