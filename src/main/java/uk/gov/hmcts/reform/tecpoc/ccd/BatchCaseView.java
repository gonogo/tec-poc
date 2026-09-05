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

    private static final String VALIDATION_NOT_RECORDED = "Not validated";

    private final BatchCaseRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(BatchCaseConfiguration.CASE_TYPE);
    }

    @Override
    public BatchCase getCase(CaseViewRequest<BatchCaseState> request) {
        BatchCase batchCase = repository.find(request.caseRef());
        batchCase.setTasksMarkdown(
            BatchPrototypeTasks.markdownFor(request.caseRef(), request.state(), batchCase)
        );
        BatchValidationResult validationResult = batchCase.getBatchValidationResult();
        batchCase.setBatchValidationResultDisplay(
            validationResult == null ? VALIDATION_NOT_RECORDED : validationResult.getLabel()
        );
        batchCase.setAllDocuments(toAllDocuments(repository.findDocuments(request.caseRef())));
        return batchCase;
    }

    static List<ListValue<Document>> toAllDocuments(List<BatchCaseDocument> documents) {
        return documents.stream()
            .map(BatchCaseView::toListValue)
            .toList();
    }

    private static ListValue<Document> toListValue(BatchCaseDocument document) {
        Document ccdDocument = Document.builder()
            .url(document.documentUrl())
            .binaryUrl(document.documentBinaryUrl())
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
