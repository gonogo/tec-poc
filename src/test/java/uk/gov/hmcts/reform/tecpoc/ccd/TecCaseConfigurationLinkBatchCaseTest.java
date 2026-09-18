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

class TecCaseConfigurationLinkBatchCaseTest {

    private TecCaseRepository repository;
    private BatchCaseRepository batchCaseRepository;
    private TecCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(TecCaseRepository.class);
        batchCaseRepository = mock(BatchCaseRepository.class);
        configuration = new TecCaseConfiguration(repository, batchCaseRepository);
    }

    @Test
    void linkBatchCasePersistsReferenceWhenBatchExists() {
        when(batchCaseRepository.exists(222L)).thenReturn(true);

        TecCase data = new TecCase();
        data.setBatchCase(CaseLink.builder()
            .caseReference("222")
            .caseType(BatchCaseConfiguration.CASE_TYPE)
            .build());

        SubmitResponse<CaseState> response = linkBatchCase(111L, data);

        verify(repository).linkBatchCase(111L, 222L);
        assertThat(response).isNotNull();
    }

    @Test
    void linkBatchCaseAcceptsHyphenatedReference() {
        when(batchCaseRepository.exists(1755000000000000L)).thenReturn(true);

        TecCase data = new TecCase();
        data.setBatchCase(CaseLink.builder()
            .caseReference("1755-0000-0000-0000")
            .build());

        linkBatchCase(111L, data);

        verify(repository).linkBatchCase(111L, 1755000000000000L);
    }

    @Test
    void linkBatchCaseRejectsMissingBatch() {
        when(batchCaseRepository.exists(999L)).thenReturn(false);

        TecCase data = new TecCase();
        data.setBatchCase(CaseLink.builder().caseReference("999").build());

        assertThatThrownBy(() -> linkBatchCase(111L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No TEC_BATCH case found");
    }

    @Test
    void parseCaseReferenceStripsHyphens() {
        assertThat(TecCaseConfiguration.parseCaseReference("1755-0000-0000-0001"))
            .isEqualTo(1755000000000001L);
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<CaseState> linkBatchCase(long caseReference, TecCase data) {
        return (SubmitResponse<CaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "linkBatchCase",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
