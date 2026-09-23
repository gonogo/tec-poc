package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Component
@RequiredArgsConstructor
public class BatchCaseView implements CaseView<BatchCase, BatchCaseState> {

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
        batchCase.setRolesAndAccessMarkdown(
            "<p class=\"govuk-body\">Roles and access (CCD shell). "
                + "The Manage Case Work Allocation tab is not wired for TEC in this PoC.</p>"
        );
        batchCase.setCaseLinks(toCaseLinks(
            repository.findLinkedPcnCaseReferences(request.caseRef()),
            batchCase.getOperation()
        ));
        batchCase.setAllDocuments(toAllDocuments(repository.findDocuments(request.caseRef())));
        return batchCase;
    }

    /**
     * ExUI Linked Cases "linked to" reads {@code ListValue.id} as the linked case reference
     * ({@code getCaseViewV2(fieldValue.id)}), not {@code CaseReference}. Use the
     * PCN case reference as the collection id (same pattern as pcs-api).
     * Reason text depends on batch operation so batch "linked to" and PCN
     * "linked from" (via getLinkedCases) show the same label.
     */
    static List<ListValue<CaseLink>> toCaseLinks(List<Long> pcnCaseReferences) {
        return toCaseLinks(pcnCaseReferences, BatchOperation.REGISTRATION);
    }

    static List<ListValue<CaseLink>> toCaseLinks(List<Long> pcnCaseReferences, BatchOperation operation) {
        return pcnCaseReferences.stream()
            .map(ref -> {
                String caseReference = Long.toString(ref);
                return ListValue.<CaseLink>builder()
                    .id(caseReference)
                    .value(CaseLink.builder()
                        .caseReference(caseReference)
                        .caseType(TecCaseConfiguration.CASE_TYPE)
                        .reasonForLink(BatchRegistrationCaseLinks.reasonForLink(operation))
                        .build())
                    .build();
            })
            .toList();
    }

    static String statusLabel(BatchCaseState state) {
        return switch (state) {
            case QUEUED_FOR_PROCESSING -> "Queued for processing";
            case PROCESSING_STARTED -> "Processing started";
            case PROCESSING_COMPLETE -> "Processing complete";
            case PROCESSING_FAILED -> "Processing failed";
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

    static List<ListValue<Document>> toAllDocuments(List<BatchCaseDocument> documents) {
        return documents.stream()
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
