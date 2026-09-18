package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;

class TecCaseViewEnforcementSectionTest {

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
    void populatesEnforcementSectionWhenLinked() {
        TecCase stored = new TecCase();
        stored.setLocalAuthority(LocalAuthority.WESTMINSTER);
        stored.setEnforcementCase(CaseLink.builder()
            .caseReference("1755000000000001")
            .caseType(EnforcementCaseConfiguration.CASE_TYPE)
            .build());

        when(repository.find(111L)).thenReturn(stored);
        when(repository.findDocuments(111L)).thenReturn(java.util.List.of());
        when(enforcementCaseRepository.findCreatedAt(1755000000000001L))
            .thenReturn(Instant.parse("2026-09-18T10:00:00Z"));

        TecCase result = view.getCase(new CaseViewRequest<>(111L, CaseState.CASE_ISSUED));

        assertThat(result.getEnforcementLinked()).isEqualTo("Yes");
        assertThat(result.getEnforcementCase().getCaseReference()).isEqualTo("1755000000000001");
        assertThat(result.getEnforcementStatusDisplay()).isEqualTo("Open");
        assertThat(result.getEnforcementCreatedDate()).isEqualTo(LocalDate.of(2026, 9, 18));
    }

    @Test
    void omitsEnforcementSectionWhenNotLinked() {
        TecCase stored = new TecCase();
        stored.setLocalAuthority(LocalAuthority.WESTMINSTER);

        when(repository.find(111L)).thenReturn(stored);
        when(repository.findDocuments(111L)).thenReturn(java.util.List.of());

        TecCase result = view.getCase(new CaseViewRequest<>(111L, CaseState.CASE_ISSUED));

        assertThat(result.getEnforcementLinked()).isNull();
        assertThat(result.getEnforcementStatusDisplay()).isNull();
        assertThat(result.getEnforcementCreatedDate()).isNull();
    }
}
