package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class BatchCaseConfigurationLinkPcnCasesTest {

    private BatchCaseRepository repository;
    private TecCaseRepository tecCaseRepository;
    private BatchCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(BatchCaseRepository.class);
        tecCaseRepository = mock(TecCaseRepository.class);
        configuration = new BatchCaseConfiguration(repository, tecCaseRepository);
    }

    @Test
    void linkPcnCasesPersistsWhenPcnExists() {
        when(tecCaseRepository.exists(111L)).thenReturn(true);
        when(tecCaseRepository.findBatchCaseReference(111L)).thenReturn(null);

        BatchCase data = new BatchCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .id(UUID.randomUUID().toString())
            .value(CaseLink.builder()
                .caseReference("111")
                .caseType(TecCaseConfiguration.CASE_TYPE)
                .reasonForLink(BatchRegistrationCaseLinks.reasonForLink())
                .build())
            .build()));

        linkPcnCases(999L, data);

        verify(tecCaseRepository).linkBatchCase(111L, 999L);
    }

    @Test
    void linkPcnCasesRejectsMissingPcn() {
        when(tecCaseRepository.exists(999L)).thenReturn(false);

        BatchCase data = new BatchCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .value(CaseLink.builder().caseReference("999").build())
            .build()));

        assertThatThrownBy(() -> linkPcnCases(1L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No TEC PCN case found");
    }

    @Test
    void linkPcnCasesAllowsIdempotentRelinkToSameBatch() {
        // Long identity != must not reject when values are equal (CCD case refs are outside cache)
        when(tecCaseRepository.exists(1789997297857502L)).thenReturn(true);
        when(tecCaseRepository.findBatchCaseReference(1789997297857502L))
            .thenReturn(Long.valueOf(1789997265350555L));

        BatchCase data = new BatchCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .value(CaseLink.builder()
                .caseReference("1789997297857502")
                .caseType(TecCaseConfiguration.CASE_TYPE)
                .build())
            .build()));

        linkPcnCases(1789997265350555L, data);

        verify(tecCaseRepository).linkBatchCase(1789997297857502L, 1789997265350555L);
    }

    @Test
    void linkPcnCasesRejectsPcnAlreadyLinkedElsewhere() {
        when(tecCaseRepository.exists(111L)).thenReturn(true);
        when(tecCaseRepository.findBatchCaseReference(111L)).thenReturn(888L);

        BatchCase data = new BatchCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .value(CaseLink.builder().caseReference("111").build())
            .build()));

        assertThatThrownBy(() -> linkPcnCases(999L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already linked to batch case 888");
    }

    @SuppressWarnings("unchecked")
    private void linkPcnCases(long caseReference, BatchCase data) {
        ReflectionTestUtils.invokeMethod(
            configuration,
            "linkPcnCases",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
