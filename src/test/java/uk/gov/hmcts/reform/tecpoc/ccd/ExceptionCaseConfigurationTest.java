package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;

class ExceptionCaseConfigurationTest {

    private ExceptionCaseRepository repository;
    private ExceptionCaseConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(ExceptionCaseRepository.class);
        configuration = new ExceptionCaseConfiguration(repository);
    }

    @Test
    void rejectItemMapsCommentToEventMetadataDescription() {
        ExceptionCase data = new ExceptionCase();
        data.setRejectReason(ExceptionRejectReason.PCN_COULD_NOT_BE_MATCHED);
        data.setRejectComment("  Could not find matching PCN  ");

        SubmitResponse<ExceptionCaseState> response = rejectItem(1L, data);

        verify(repository).recordRejectReason(1L, ExceptionRejectReason.PCN_COULD_NOT_BE_MATCHED);
        assertThat(response.getEventMetadata()).isNotNull();
        assertThat(response.getEventMetadata().getDescription()).isEqualTo("Could not find matching PCN");
        assertThat(response.getState()).isNull();
    }

    @Test
    void rejectItemOmitsEventMetadataWhenCommentBlank() {
        ExceptionCase data = new ExceptionCase();
        data.setRejectReason(ExceptionRejectReason.OTHER);
        data.setRejectComment("   ");

        SubmitResponse<ExceptionCaseState> response = rejectItem(2L, data);

        verify(repository).recordRejectReason(2L, ExceptionRejectReason.OTHER);
        assertThat(response.getEventMetadata()).isNull();
    }

    @Test
    void rejectItemOmitsEventMetadataWhenCommentNull() {
        ExceptionCase data = new ExceptionCase();
        data.setRejectReason(ExceptionRejectReason.ITEM_NOT_RELEVANT_TO_TEC_CASE);

        SubmitResponse<ExceptionCaseState> response = rejectItem(3L, data);

        verify(repository).recordRejectReason(3L, ExceptionRejectReason.ITEM_NOT_RELEVANT_TO_TEC_CASE);
        assertThat(response.getEventMetadata()).isNull();
    }

    @Test
    void editPcnUpdatesPenaltyChargeNumber() {
        ExceptionCase data = new ExceptionCase();
        data.setPenaltyChargeNumber("AB1234567A0");

        SubmitResponse<ExceptionCaseState> response = editPcn(4L, data);

        verify(repository).updatePenaltyChargeNumber(4L, "AB1234567A0");
        assertThat(response.getEventMetadata()).isNull();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createExceptionCasePersistsAndReturnsPendingReviewState() {
        ExceptionCase data = new ExceptionCase();
        data.setPenaltyChargeNumber("AB1234567A0");

        SubmitResponse<ExceptionCaseState> response = createExceptionCase(5L, data);

        verify(repository).create(5L, data);
        assertThat(response.getState()).isEqualTo(ExceptionCaseState.EXCEPTION_PENDING_REVIEW);
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<ExceptionCaseState> rejectItem(long caseReference, ExceptionCase data) {
        return (SubmitResponse<ExceptionCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "rejectItem",
            new EventPayload<>(caseReference, data, null)
        );
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<ExceptionCaseState> editPcn(long caseReference, ExceptionCase data) {
        return (SubmitResponse<ExceptionCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "editPcn",
            new EventPayload<>(caseReference, data, null)
        );
    }

    @SuppressWarnings("unchecked")
    private SubmitResponse<ExceptionCaseState> createExceptionCase(
        long caseReference,
        ExceptionCase data
    ) {
        return (SubmitResponse<ExceptionCaseState>) ReflectionTestUtils.invokeMethod(
            configuration,
            "createExceptionCase",
            new EventPayload<>(caseReference, data, null)
        );
    }
}
