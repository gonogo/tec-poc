package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;

class TecCaseConfigurationLinkEnforcementCaseTest {

    private TecCaseRepository repository;
    private EnforcementCaseRepository enforcementCaseRepository;
    private TecCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(TecCaseRepository.class);
        enforcementCaseRepository = mock(EnforcementCaseRepository.class);
        configuration = new TecCaseConfiguration(
            repository,
            mock(BatchCaseRepository.class),
            enforcementCaseRepository
        );
    }

    @Test
    void linkEnforcementCasePersistsReferenceWhenEnforcementExists() {
        when(enforcementCaseRepository.exists(222L)).thenReturn(true);
        when(enforcementCaseRepository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(null);

        TecCase data = new TecCase();
        data.setEnforcementCase(CaseLink.builder()
            .caseReference("222")
            .caseType(EnforcementCaseConfiguration.CASE_TYPE)
            .build());

        SubmitResponse<CaseState> response = linkEnforcementCase(111L, data);

        verify(repository).linkEnforcementCase(111L, 222L);
        assertThat(response).isNotNull();
    }

    @Test
    void linkEnforcementCaseAcceptsHyphenatedReference() {
        when(enforcementCaseRepository.exists(1755000000000000L)).thenReturn(true);
        when(enforcementCaseRepository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(null);

        TecCase data = new TecCase();
        data.setEnforcementCase(CaseLink.builder()
            .caseReference("1755-0000-0000-0000")
            .build());

        linkEnforcementCase(111L, data);

        verify(repository).linkEnforcementCase(111L, 1755000000000000L);
    }

    @Test
    void linkEnforcementCaseAllowsIdempotentRelinkToSameEnforcement() {
        when(enforcementCaseRepository.exists(222L)).thenReturn(true);
        when(enforcementCaseRepository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(222L);

        TecCase data = new TecCase();
        data.setEnforcementCase(CaseLink.builder().caseReference("222").build());

        linkEnforcementCase(111L, data);

        verify(repository).linkEnforcementCase(111L, 222L);
    }

    @Test
    void linkEnforcementCaseRejectsMissingEnforcement() {
        when(enforcementCaseRepository.exists(999L)).thenReturn(false);

        TecCase data = new TecCase();
        data.setEnforcementCase(CaseLink.builder().caseReference("999").build());

        assertThatThrownBy(() -> linkEnforcementCase(111L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No TEC_ENFORCEMENT case found");
    }

    @Test
    void linkEnforcementCaseRejectsAlreadyLinkedElsewhere() {
        when(enforcementCaseRepository.exists(222L)).thenReturn(true);
        when(enforcementCaseRepository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(333L);

        TecCase data = new TecCase();
        data.setEnforcementCase(CaseLink.builder().caseReference("222").build());

        assertThatThrownBy(() -> linkEnforcementCase(111L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already linked to enforcement case 333");
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<CaseState> linkEnforcementCase(long caseReference, TecCase data) {
        return (SubmitResponse<CaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "linkEnforcementCase",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
