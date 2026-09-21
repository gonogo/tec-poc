package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class TecCaseViewLinkedCasesTest {

    private TecCaseRepository repository;
    private EnforcementCaseRepository enforcementCaseRepository;
    private TecCaseView view;

    @BeforeEach
    void setUp() {
        repository = mock(TecCaseRepository.class);
        enforcementCaseRepository = mock(EnforcementCaseRepository.class);
        view = new TecCaseView(repository, enforcementCaseRepository);
    }

    @Test
    void getCaseBuildsCaseLinksFromEnforcementOnly() {
        TecCase stored = new TecCase();
        stored.setLocalAuthority(LocalAuthority.WESTMINSTER);
        stored.setBatchCase(CaseLink.builder()
            .caseReference("1755000000000099")
            .caseType(BatchCaseConfiguration.CASE_TYPE)
            .build());
        stored.setEnforcementCase(CaseLink.builder()
            .caseReference("1755000000000001")
            .caseType(EnforcementCaseConfiguration.CASE_TYPE)
            .build());

        when(repository.find(111L)).thenReturn(stored);
        when(repository.findDocuments(111L)).thenReturn(List.of());
        when(enforcementCaseRepository.findCreatedAt(1755000000000001L)).thenReturn(null);

        TecCase result = view.getCase(new CaseViewRequest<>(111L, CaseState.CASE_ISSUED));

        assertThat(result.getCaseLinks()).hasSize(1);
        assertThat(result.getCaseLinks().get(0).getId()).isEqualTo("1755000000000001");
        assertThat(result.getCaseLinks().get(0).getValue().getCaseType())
            .isEqualTo(EnforcementCaseConfiguration.CASE_TYPE);
    }

    @Test
    void getCaseLeavesCaseLinksEmptyWhenOnlyBatchLinked() {
        TecCase stored = new TecCase();
        stored.setLocalAuthority(LocalAuthority.WESTMINSTER);
        stored.setBatchCase(CaseLink.builder()
            .caseReference("1755000000000099")
            .caseType(BatchCaseConfiguration.CASE_TYPE)
            .build());

        when(repository.find(111L)).thenReturn(stored);
        when(repository.findDocuments(111L)).thenReturn(List.of());

        TecCase result = view.getCase(new CaseViewRequest<>(111L, CaseState.CASE_ISSUED));

        assertThat(result.getCaseLinks()).isEmpty();
    }

    @Test
    void toCaseLinksOmitsBatchCase() {
        TecCase tecCase = new TecCase();
        tecCase.setBatchCase(CaseLink.builder()
            .caseReference("99")
            .caseType(BatchCaseConfiguration.CASE_TYPE)
            .build());

        List<ListValue<CaseLink>> links = TecCaseView.toCaseLinks(tecCase);

        assertThat(links).isEmpty();
    }
}
