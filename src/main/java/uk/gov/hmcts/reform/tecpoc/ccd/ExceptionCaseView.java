package uk.gov.hmcts.reform.tecpoc.ccd;

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
public class ExceptionCaseView implements CaseView<ExceptionCase, ExceptionCaseState> {

    static final String PLACEHOLDER_DISPLAY = "—";

    private final ExceptionCaseRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ExceptionCaseConfiguration.CASE_TYPE);
    }

    @Override
    public ExceptionCase getCase(CaseViewRequest<ExceptionCaseState> request) {
        ExceptionCase exceptionCase = repository.find(request.caseRef());
        exceptionCase.setFormValidationResultDisplay(PLACEHOLDER_DISPLAY);
        exceptionCase.setAssociatedTecCaseDisplay(PLACEHOLDER_DISPLAY);
        exceptionCase.setTasksMarkdown(
            ExceptionPrototypeTasks.markdownFor(request.caseRef(), request.state())
        );
        exceptionCase.setRolesAndAccessMarkdown(
            "<p class=\"govuk-body\">Roles and access (CCD shell). "
                + "The Manage Case Work Allocation tab is not wired for TEC in this PoC.</p>"
        );
        exceptionCase.setAllDocuments(List.<ListValue<Document>>of());
        return exceptionCase;
    }
}
