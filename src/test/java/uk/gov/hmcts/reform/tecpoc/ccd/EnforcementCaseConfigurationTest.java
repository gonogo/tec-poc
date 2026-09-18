package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class EnforcementCaseConfigurationTest {

    private EnforcementCaseRepository repository;
    private EnforcementCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(EnforcementCaseRepository.class);
        configuration = new EnforcementCaseConfiguration(repository);
    }

    @Test
    void createEnforcementCasePersistsAndReturnsOpenState() {
        EnforcementCase data = new EnforcementCase();
        data.setLocalAuthority(LocalAuthority.WESTMINSTER);
        data.setSubmitterEmail("la@example.com");
        data.setReceivedVia(BatchReceivedVia.EMAIL);

        SubmitResponse<EnforcementCaseState> response = createEnforcementCase(5L, data);

        verify(repository).create(5L, data);
        assertThat(response.getState()).isEqualTo(EnforcementCaseState.OPEN);
    }

    @Test
    void linkPcnCasesPersistsWhenPcnExists() {
        when(repository.pcnExists(111L)).thenReturn(true);
        when(repository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(null);

        EnforcementCase data = new EnforcementCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .id(UUID.randomUUID().toString())
            .value(CaseLink.builder()
                .caseReference("111")
                .caseType(TecCaseConfiguration.CASE_TYPE)
                .build())
            .build()));

        linkPcnCases(999L, data);

        verify(repository).linkPcnCase(999L, 111L);
    }

    @Test
    void linkPcnCasesRejectsMissingPcn() {
        when(repository.pcnExists(999L)).thenReturn(false);

        EnforcementCase data = new EnforcementCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .value(CaseLink.builder().caseReference("999").build())
            .build()));

        assertThatThrownBy(() -> linkPcnCases(1L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No TEC PCN case found");
    }

    @Test
    void linkPcnCasesRejectsPcnAlreadyLinkedElsewhere() {
        when(repository.pcnExists(111L)).thenReturn(true);
        when(repository.findEnforcementCaseReferenceForPcn(111L)).thenReturn(888L);

        EnforcementCase data = new EnforcementCase();
        data.setCaseLinks(List.of(ListValue.<CaseLink>builder()
            .value(CaseLink.builder().caseReference("111").build())
            .build()));

        assertThatThrownBy(() -> linkPcnCases(999L, data))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already linked to enforcement case 888");
    }

    @Test
    void attachCaseFileDocumentPersistsWithoutCategory() {
        Document document = Document.builder()
            .url("http://localhost:4455/cases/documents/" + UUID.randomUUID())
            .binaryUrl("http://localhost:4455/cases/documents/" + UUID.randomUUID() + "/binary")
            .filename("TE10.pdf")
            .build();

        EnforcementCase data = new EnforcementCase();
        data.setCaseFileDocument(document);

        attachCaseFileDocument(7L, data);

        verify(repository).insertDocument(
            org.mockito.ArgumentMatchers.eq(7L),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq("TE10.pdf")
        );
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<EnforcementCaseState> createEnforcementCase(
        long caseReference,
        EnforcementCase data
    ) {
        return (SubmitResponse<EnforcementCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "createEnforcementCase",
            new EventPayload<>(caseReference, data, null)
        );
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<EnforcementCaseState> linkPcnCases(
        long caseReference,
        EnforcementCase data
    ) {
        return (SubmitResponse<EnforcementCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "linkPcnCases",
            new EventPayload<>(caseReference, data, null)
        );
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<EnforcementCaseState> attachCaseFileDocument(
        long caseReference,
        EnforcementCase data
    ) {
        return (SubmitResponse<EnforcementCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "attachCaseFileDocument",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
