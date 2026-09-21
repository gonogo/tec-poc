package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;

class BatchCaseViewTest {

    private BatchCaseRepository repository;
    private BatchCaseView view;

    @BeforeEach
    void setUp() {
        repository = mock(BatchCaseRepository.class);
        view = new BatchCaseView(repository);
    }

    @Test
    void getCaseSetsRolesAndAccessMarkdown() {
        BatchCase stored = new BatchCase();
        stored.setFileIdentifier("file-1");
        when(repository.find(99L)).thenReturn(stored);
        when(repository.findLinkedPcnCaseReferences(99L)).thenReturn(List.of());
        when(repository.findDocuments(99L)).thenReturn(List.of());

        BatchCase result = view.getCase(
            new CaseViewRequest<>(99L, BatchCaseState.QUEUED_FOR_PROCESSING)
        );

        assertThat(result.getFileIdentifier()).isEqualTo("file-1");
        assertThat(result.getRolesAndAccessMarkdown()).contains("Roles and access");
        assertThat(result.getTasksMarkdown()).isNotBlank();
        assertThat(result.getCaseLinks()).isEmpty();
    }

    @Test
    void getCasePopulatesLinkedPcnCaseLinks() {
        BatchCase stored = new BatchCase();
        stored.setFileIdentifier("file-1");
        when(repository.find(99L)).thenReturn(stored);
        when(repository.findLinkedPcnCaseReferences(99L)).thenReturn(List.of(111L, 222L));
        when(repository.findDocuments(99L)).thenReturn(List.of());

        BatchCase result = view.getCase(
            new CaseViewRequest<>(99L, BatchCaseState.QUEUED_FOR_PROCESSING)
        );

        assertThat(result.getCaseLinks()).hasSize(2);
        assertThat(result.getCaseLinks().get(0).getValue().getCaseType())
            .isEqualTo(TecCaseConfiguration.CASE_TYPE);
        assertThat(result.getCaseLinks().get(0).getId()).isEqualTo("111");
        assertThat(result.getCaseLinks().get(0).getValue().getReasonForLink()).hasSize(1);
        var reason = result.getCaseLinks().get(0).getValue().getReasonForLink().get(0).getValue();
        assertThat(reason.getReason()).isEqualTo(BatchRegistrationCaseLinks.REASON_CODE);
        assertThat(reason.getDescription()).isEqualTo(BatchRegistrationCaseLinks.REASON);
    }

    @Test
    void toCaseLinksMapsPcnReferencesWithBatchRegistrationReason() {
        var links = BatchCaseView.toCaseLinks(List.of(99L));
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getId()).isEqualTo("99");
        assertThat(links.get(0).getValue().getCaseReference()).isEqualTo("99");
        assertThat(links.get(0).getValue().getCaseType()).isEqualTo("TEC");
        var reason = links.get(0).getValue().getReasonForLink().get(0).getValue();
        assertThat(reason.getReason()).isEqualTo(BatchRegistrationCaseLinks.REASON_CODE);
        assertThat(reason.getDescription()).isEqualTo(BatchRegistrationCaseLinks.REASON);
    }

    @Test
    void caseTypeIdsIncludesTecBatch() {
        assertThat(view.caseTypeIds()).containsExactly(BatchCaseConfiguration.CASE_TYPE);
    }
}
