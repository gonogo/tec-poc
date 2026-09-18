package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;

class TecCaseConfigurationTest {

    private TecCaseRepository repository;
    private TecCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(TecCaseRepository.class);
        configuration = new TecCaseConfiguration(
            repository,
            mock(BatchCaseRepository.class),
            mock(EnforcementCaseRepository.class)
        );
    }

    @Test
    void verifyFormValidationMapsCommentToEventMetadataDescription() {
        TecCase data = new TecCase();
        data.setFormValidationResult(FormValidationResult.FORM_VALID);
        data.setFormValidationComment("  Looks complete  ");

        SubmitResponse<CaseState> response = verifyFormValidation(1L, data);

        verify(repository).recordFormValidation(1L, FormValidationResult.FORM_VALID);
        assertThat(response.getEventMetadata()).isNotNull();
        assertThat(response.getEventMetadata().getDescription()).isEqualTo("Looks complete");
    }

    @Test
    void verifyFormValidationOmitsEventMetadataWhenCommentBlank() {
        TecCase data = new TecCase();
        data.setFormValidationResult(FormValidationResult.FORM_INVALID);
        data.setFormValidationComment("   ");

        SubmitResponse<CaseState> response = verifyFormValidation(2L, data);

        verify(repository).recordFormValidation(2L, FormValidationResult.FORM_INVALID);
        assertThat(response.getEventMetadata()).isNull();
    }

    @Test
    void verifyFormValidationOmitsEventMetadataWhenCommentNull() {
        TecCase data = new TecCase();
        data.setFormValidationResult(FormValidationResult.FORM_VALID);

        SubmitResponse<CaseState> response = verifyFormValidation(3L, data);

        verify(repository).recordFormValidation(3L, FormValidationResult.FORM_VALID);
        assertThat(response.getEventMetadata()).isNull();
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<CaseState> verifyFormValidation(long caseReference, TecCase data) {
        return (SubmitResponse<CaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "verifyFormValidation",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
