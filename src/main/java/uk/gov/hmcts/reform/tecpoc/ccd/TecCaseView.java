package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
public class TecCaseView implements CaseView<TecCase, CaseState> {

    private final TecCaseRepository repository;
    private final EnforcementCaseRepository enforcementCaseRepository;

    private static final String FORM_VALIDATION_NOT_RECORDED = "Not validated";

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(TecCaseConfiguration.CASE_TYPE);
    }

    @Override
    public TecCase getCase(CaseViewRequest<CaseState> request) {
        TecCase tecCase = repository.find(request.caseRef());
        // Event-only fields — never surface on Case details / case_link sync from CaseView.
        tecCase.setBatchLinkCase(null);
        tecCase.setBatchLinkType(null);
        tecCase.setTasksMarkdown(
            TecPrototypeTasks.markdownFor(request.caseRef(), request.state(), tecCase)
        );
        tecCase.setRolesAndAccessMarkdown(
            "<p class=\"govuk-body\">Roles and access (CCD shell). "
                + "The Manage Case Work Allocation tab is not wired for TEC in this PoC.</p>"
        );
        FormValidationResult validationResult = tecCase.getFormValidationResult();
        String validationDisplay = validationResult == null
            ? FORM_VALIDATION_NOT_RECORDED
            : validationResult.getLabel();
        tecCase.setFormValidationResultDisplay(validationDisplay);
        tecCase.setTimeExtensionFormValidationResultDisplay(validationDisplay);
        if (tecCase.getLocalAuthority() != null) {
            tecCase.setCaseAccessCategory(tecCase.getLocalAuthority().getCode());
        }
        populateEnforcementSection(tecCase);
        tecCase.setCaseLinks(toCaseLinks(tecCase));
        tecCase.setWarrantAuthorisations(
            toWarrantAuthorisations(repository.findWarrantAuthorisations(request.caseRef()))
        );
        tecCase.setAllDocuments(toAllDocuments(repository.findDocuments(request.caseRef())));
        return tecCase;
    }

    /**
     * ExUI Linked Cases "linked to" reads this case's {@code caseLinks}.
     * Batch links are owned by the batch case (PCN appears under "linked from"
     * via CCD {@code case_link} / getLinkedCases). Only enforcement remains outbound here.
     */
    static List<ListValue<CaseLink>> toCaseLinks(TecCase tecCase) {
        List<ListValue<CaseLink>> links = new ArrayList<>();
        addCaseLink(links, tecCase.getEnforcementCase());
        return links;
    }

    private static void addCaseLink(List<ListValue<CaseLink>> links, CaseLink caseLink) {
        if (caseLink == null || caseLink.getCaseReference() == null || caseLink.getCaseReference().isBlank()) {
            return;
        }
        String caseReference = caseLink.getCaseReference().replace("-", "").trim();
        links.add(ListValue.<CaseLink>builder()
            .id(caseReference)
            .value(CaseLink.builder()
                .caseReference(caseReference)
                .caseType(caseLink.getCaseType())
                .build())
            .build());
    }

    private void populateEnforcementSection(TecCase tecCase) {
        CaseLink enforcementCase = tecCase.getEnforcementCase();
        if (enforcementCase == null || enforcementCase.getCaseReference() == null
            || enforcementCase.getCaseReference().isBlank()) {
            return;
        }
        long enforcementRef = Long.parseLong(enforcementCase.getCaseReference().replace("-", ""));
        Instant createdAt = enforcementCaseRepository.findCreatedAt(enforcementRef);

        tecCase.setEnforcementLinked("Yes");
        tecCase.setEnforcementStatusDisplay(EnforcementCaseView.statusLabel(EnforcementCaseState.OPEN));
        tecCase.setEnforcementCreatedDate(
            createdAt == null ? null : LocalDate.ofInstant(createdAt, ZoneOffset.UTC)
        );
    }

    static List<ListValue<Document>> toAllDocuments(List<TecCaseDocument> documents) {
        return documents.stream()
            .map(TecCaseView::toListValue)
            .toList();
    }

    static List<ListValue<WarrantAuthorisation>> toWarrantAuthorisations(
        List<TecCaseWarrantAuthorisation> authorisations
    ) {
        return authorisations.stream()
            .map(TecCaseView::toWarrantAuthorisationListValue)
            .toList();
    }

    private static ListValue<WarrantAuthorisation> toWarrantAuthorisationListValue(
        TecCaseWarrantAuthorisation authorisation
    ) {
        WarrantAuthorisation value = new WarrantAuthorisation();
        value.setDateOfIssue(authorisation.dateOfIssue());
        value.setDateOfExpiry(authorisation.dateOfExpiry());
        value.setStatus(authorisation.status());
        return ListValue.<WarrantAuthorisation>builder()
            .id(authorisation.id().toString())
            .value(value)
            .build();
    }

    private static ListValue<Document> toListValue(TecCaseDocument document) {
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
