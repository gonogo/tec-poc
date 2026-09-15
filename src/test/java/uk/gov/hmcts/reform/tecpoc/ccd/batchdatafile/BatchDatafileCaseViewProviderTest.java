package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchDatafileCaseViewProviderTest {

    @Mock
    private BatchDatafileCaseRepository repository;

    private final BatchDatafileCaseMapper mapper = new BatchDatafileCaseMapper();

    @ParameterizedTest
    @EnumSource(BatchDatafileCaseState.class)
    void shouldProjectThePersistedBatchFileAndCurrentCcdState(BatchDatafileCaseState state) {
        BatchDatafileCaseEntity entity = BatchDatafileCaseEntity.builder()
            .caseReference(1234L)
            .batchFileUrl("http://document-store/documents/68c89c98-399a-4eb4-b721-fd1d2109867f")
            .batchFileName("batch.csv")
            .build();
        when(repository.findById(1234L)).thenReturn(Optional.of(entity));
        BatchDatafileCaseViewProvider provider = new BatchDatafileCaseViewProvider(repository, mapper);

        BatchDatafileCase result = provider.getCase(
            new CaseViewRequest<>(1234L, state)
        );

        assertThat(result.getBatchFile().getFilename()).isEqualTo("batch.csv");
        assertThat(result.getCaseState()).isEqualTo(state);
    }

    @Test
    void shouldFailWhenTheServiceOwnedCaseDataIsMissing() {
        when(repository.findById(1234L)).thenReturn(Optional.empty());
        BatchDatafileCaseViewProvider provider = new BatchDatafileCaseViewProvider(repository, mapper);

        assertThatThrownBy(() -> provider.getCase(
            new CaseViewRequest<>(1234L, BatchDatafileCaseState.AWAITING_PROCESSING)
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Batch datafile case not found: 1234");
    }
}
