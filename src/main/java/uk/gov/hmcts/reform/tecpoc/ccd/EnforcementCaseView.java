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
public class EnforcementCaseView implements CaseView<EnforcementCase, EnforcementCaseState> {

    private final EnforcementCaseRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(EnforcementCaseConfiguration.CASE_TYPE);
    }

    @Override
    public EnforcementCase getCase(CaseViewRequest<EnforcementCaseState> request) {
        EnforcementCase enforcementCase = repository.find(request.caseRef());
        enforcementCase.setStatusDisplay(statusLabel(request.state()));
        enforcementCase.setCaseLinks(toCaseLinks(repository.findLinkedPcnCaseReferences(request.caseRef())));
        enforcementCase.setTasksMarkdown(
            EnforcementPrototypeTasks.markdownFor(request.caseRef(), request.state())
        );
        enforcementCase.setRolesAndAccessMarkdown(
            "<p class=\"govuk-body\">Roles and access (CCD shell). "
                + "The Manage Case Work Allocation tab is not wired for TEC in this PoC.</p>"
        );
        enforcementCase.setAllDocuments(toAllDocuments(repository.findDocuments(request.caseRef())));
        return enforcementCase;
    }

    static String statusLabel(EnforcementCaseState state) {
        return switch (state) {
            case OPEN -> "Open";
        };
    }

    /**
     * ExUI Linked Cases reads {@code ListValue.id} as the linked case reference
     * ({@code getCaseViewV2(fieldValue.id)}), not {@code CaseReference}. Use the
     * PCN case reference as the collection id (same pattern as pcs-api).
     */
    static List<ListValue<CaseLink>> toCaseLinks(List<Long> pcnCaseReferences) {
        return pcnCaseReferences.stream()
            .map(ref -> {
                String caseReference = Long.toString(ref);
                return ListValue.<CaseLink>builder()
                    .id(caseReference)
                    .value(CaseLink.builder()
                        .caseReference(caseReference)
                        .caseType(TecCaseConfiguration.CASE_TYPE)
                        .build())
                    .build();
            })
            .toList();
    }

    static List<ListValue<Document>> toAllDocuments(List<EnforcementCaseDocument> documents) {
        return documents.stream()
            .map(EnforcementCaseView::toListValue)
            .toList();
    }

    private static ListValue<Document> toListValue(EnforcementCaseDocument document) {
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
