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
        when(repository.findDocuments(99L)).thenReturn(List.of());

        BatchCase result = view.getCase(
            new CaseViewRequest<>(99L, BatchCaseState.QUEUED_FOR_PROCESSING)
        );

        assertThat(result.getFileIdentifier()).isEqualTo("file-1");
        assertThat(result.getRolesAndAccessMarkdown()).contains("Roles and access");
        assertThat(result.getTasksMarkdown()).isNotBlank();
    }

    @Test
    void caseTypeIdsIncludesTecBatch() {
        assertThat(view.caseTypeIds()).containsExactly(BatchCaseConfiguration.CASE_TYPE);
    }
}
