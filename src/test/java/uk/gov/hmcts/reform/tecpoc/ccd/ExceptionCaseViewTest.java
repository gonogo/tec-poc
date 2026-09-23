package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;

class ExceptionCaseViewTest {

    private ExceptionCaseRepository repository;
    private ExceptionCaseView view;

    @BeforeEach
    void setUp() {
        repository = mock(ExceptionCaseRepository.class);
        view = new ExceptionCaseView(repository);
    }

    @Test
    void getCaseAlwaysSetsPlaceholderDisplays() {
        ExceptionCase stored = new ExceptionCase();
        stored.setPenaltyChargeNumber("AB1234567A0");
        when(repository.find(99L)).thenReturn(stored);

        ExceptionCase result = view.getCase(
            new CaseViewRequest<>(99L, ExceptionCaseState.EXCEPTION_PENDING_REVIEW)
        );

        assertThat(result.getPenaltyChargeNumber()).isEqualTo("AB1234567A0");
        assertThat(result.getStatusDisplay()).isEqualTo("Exception pending review");
        assertThat(result.getFormValidationResultDisplay())
            .isEqualTo(ExceptionCaseView.PLACEHOLDER_DISPLAY);
        assertThat(result.getAssociatedTecCaseDisplay())
            .isEqualTo(ExceptionCaseView.PLACEHOLDER_DISPLAY);
        assertThat(result.getFormValidationResultDisplay()).isEqualTo("—");
        assertThat(result.getAssociatedTecCaseDisplay()).isEqualTo("—");
        assertThat(result.getTasksMarkdown()).contains("Reject item");
        assertThat(result.getTasksMarkdown()).contains("Edit PCN");
        assertThat(result.getTasksMarkdown()).contains("/trigger/rejectItem");
        assertThat(result.getTasksMarkdown()).contains("/trigger/editPcn");
        assertThat(result.getRolesAndAccessMarkdown()).contains("Roles and access");
        assertThat(result.getAllDocuments()).isEmpty();
    }

    @Test
    void caseTypeIdsIncludesTecException() {
        assertThat(view.caseTypeIds()).containsExactly(ExceptionCaseConfiguration.CASE_TYPE);
    }

    @Test
    void stateLabelMatchesCcdAnnotation() {
        assertThat(ExceptionCaseView.stateLabel(ExceptionCaseState.EXCEPTION_PENDING_REVIEW))
            .isEqualTo("Exception pending review");
    }
}
