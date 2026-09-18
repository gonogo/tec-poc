package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class EnforcementCaseViewTest {

    private EnforcementCaseRepository repository;
    private EnforcementCaseView view;

    @BeforeEach
    void setUp() {
        repository = mock(EnforcementCaseRepository.class);
        view = new EnforcementCaseView(repository);
    }

    @Test
    void statusLabelForOpen() {
        assertThat(EnforcementCaseView.statusLabel(EnforcementCaseState.OPEN)).isEqualTo("Open");
    }

    @Test
    void getCasePopulatesStatusLinksAndDocuments() {
        EnforcementCase stored = new EnforcementCase();
        stored.setLocalAuthority(LocalAuthority.WESTMINSTER);
        stored.setSubmitterEmail("la@example.com");
        stored.setReceivedVia(BatchReceivedVia.EMAIL);

        when(repository.find(42L)).thenReturn(stored);
        when(repository.findLinkedPcnCaseReferences(42L)).thenReturn(List.of(111L, 222L));
        when(repository.findDocuments(42L)).thenReturn(List.of(
            new EnforcementCaseDocument(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                null,
                "http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111",
                "http://localhost:4455/cases/documents/11111111-1111-1111-1111-111111111111/binary",
                "TE10.pdf",
                Instant.parse("2026-09-18T12:00:00Z")
            )
        ));

        EnforcementCase result = view.getCase(new CaseViewRequest<>(42L, EnforcementCaseState.OPEN));

        assertThat(result.getStatusDisplay()).isEqualTo("Open");
        assertThat(result.getCaseLinks()).hasSize(2);
        assertThat(result.getCaseLinks().get(0).getValue().getCaseType())
            .isEqualTo(TecCaseConfiguration.CASE_TYPE);
        assertThat(result.getAllDocuments()).hasSize(1);
        Document doc = result.getAllDocuments().get(0).getValue();
        assertThat(doc.getFilename()).isEqualTo("TE10.pdf");
        assertThat(doc.getCategoryId()).isNull();
    }

    @Test
    void toCaseLinksMapsPcnReferences() {
        List<ListValue<CaseLink>> links = EnforcementCaseView.toCaseLinks(List.of(99L));
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getId()).isEqualTo("99");
        assertThat(links.get(0).getValue().getCaseReference()).isEqualTo("99");
        assertThat(links.get(0).getValue().getCaseType()).isEqualTo("TEC");
    }
}
