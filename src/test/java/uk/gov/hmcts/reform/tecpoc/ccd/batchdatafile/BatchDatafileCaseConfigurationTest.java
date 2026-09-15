package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TabField;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchDatafileCaseConfigurationTest {

    @Mock
    private BatchDatafileCaseRepository repository;

    @Mock
    private BatchFileValidator validator;

    private final BatchDatafileCaseMapper mapper = new BatchDatafileCaseMapper();

    @Test
    void shouldShowStateFileAndSubmissionTypeOnBatchDetailsTab() {
        var tabs = configuration().getTabs();
        assertThat(tabs).extracting(tab -> tab.getTabID()).containsExactly("batchDetails", "CaseHistory");
        var tab = tabs.stream()
            .filter(candidate -> candidate.getTabID().equals("batchDetails"))
            .findFirst().orElseThrow();

        assertThat(tab.getFields()).extracting(TabField::getId)
            .containsExactly("caseState", "batchFile", "submissionType");
    }

    @Test
    void shouldConfigureExactlyTheDocumentedGraphAndStateReaders() {
        var configured = configuration();
        for (var event : configured.getEvents().values()) {
            assertThat(event.getName().length()).as("CCD event name length for %s", event.getId())
                .isLessThanOrEqualTo(30);
        }
        assertThat(configured.getEvents()).containsOnlyKeys(
            "submitRegistrationDatafile", "submitWarrantDatafile", "submitWarrantReissueDatafile",
            "startProcessing", "recordProcessingSuccess", "recordProcessingFailure", "retryProcessing"
        );
        assertThat(BatchDatafileCaseState.values()).containsExactlyInAnyOrder(
            BatchDatafileCaseState.AWAITING_PROCESSING, BatchDatafileCaseState.PROCESSING,
            BatchDatafileCaseState.COMPLETE, BatchDatafileCaseState.PROCESSING_FAILED
        );
        for (var state : BatchDatafileCaseState.values()) {
            for (var role : List.of(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER, UserRole.SYSTEM)) {
                assertThat(configured.getStateRolePermissions().get(state, role)).contains(Permission.R);
            }
            assertThat(configured.getStateRolePermissions().row(state).keySet())
                .containsExactlyInAnyOrder(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER, UserRole.SYSTEM);
        }
    }

    @Test
    void shouldOnlyDeclareRolesWithImportableCaseTypePermissions() {
        for (UserRole role : UserRole.values()) {
            assertThat(role.getCaseTypePermissions()).as("Case-type permissions for %s", role).isNotBlank();
        }
        assertThat(UserRole.values()).extracting(UserRole::getRole)
            .doesNotContain("caseworker-tec-la-manager");
    }

    @ParameterizedTest
    @CsvSource({
        "submitRegistrationDatafile, PCN_REGISTRATION",
        "submitWarrantDatafile, WARRANT",
        "submitWarrantReissueDatafile, WARRANT_REISSUE"
    })
    void shouldCreateTypedSubmissionForBothActorTypes(String eventId, SubmissionType expectedType) {
        var event = configuration().getEvents().get(eventId);
        assertThat(event.getPreState()).isEmpty();
        assertThat(event.getPostState()).containsExactly(BatchDatafileCaseState.AWAITING_PROCESSING);
        assertThat(event.getGrants().keySet())
            .containsExactlyInAnyOrder(UserRole.LA_USER, UserRole.CLERK, UserRole.TEC_MANAGER, UserRole.SYSTEM);
        for (var role : event.getGrants().keySet()) {
            assertThat(event.getGrants().get(role)).containsExactlyInAnyOrderElementsOf(Permission.CRU);
        }
        assertThat(event.getFields().getPagesToMidEvent()).containsOnlyKeys("uploadFile");
        assertThat(event.getFields().getPageLabels()).containsKeys("uploadFile", "validationResults");

        var data = datafile();
        // A caller cannot choose a type different from the event's type.
        data.setSubmissionType(expectedType == SubmissionType.WARRANT
            ? SubmissionType.PCN_REGISTRATION : SubmissionType.WARRANT);
        when(validator.validate(data.getBatchFile(), expectedType)).thenReturn(List.of());

        var response = event.getSubmitHandler().submit(new EventPayload<>(1234L, data, null));

        var entityCaptor = ArgumentCaptor.forClass(BatchDatafileCaseEntity.class);
        verify(repository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getSubmissionType()).isEqualTo(expectedType);
        assertThat(entityCaptor.getValue().getBatchFileName()).isEqualTo("batch.csv");
        assertThat(response.getState()).isEqualTo(BatchDatafileCaseState.AWAITING_PROCESSING);
        verify(validator).validate(data.getBatchFile(), expectedType);
    }

    @ParameterizedTest
    @CsvSource({
        "startProcessing, AWAITING_PROCESSING, PROCESSING",
        "recordProcessingSuccess, PROCESSING, COMPLETE",
        "recordProcessingFailure, PROCESSING, PROCESSING_FAILED",
        "retryProcessing, PROCESSING_FAILED, AWAITING_PROCESSING"
    })
    void shouldRestrictTransitionsAndPreserveSubmittedData(
        String eventId, BatchDatafileCaseState source, BatchDatafileCaseState target
    ) {
        var event = configuration().getEvents().get(eventId);
        assertThat(event.getPreState()).containsExactly(source);
        assertThat(event.getPostState()).containsExactly(target);
        if (eventId.equals("retryProcessing")) {
            assertThat(event.getGrants().keySet()).containsExactlyInAnyOrder(UserRole.CLERK, UserRole.TEC_MANAGER);
        } else {
            assertThat(event.getGrants().keySet()).containsExactly(UserRole.SYSTEM);
        }
        var existing = BatchDatafileCaseEntity.builder()
            .caseReference(1234L)
            .submissionType(SubmissionType.WARRANT_REISSUE)
            .batchFileUrl("http://document-store/documents/1234")
            .batchFileName("batch.csv")
            .build();
        when(repository.findById(1234L)).thenReturn(Optional.of(existing));

        var response = event.getSubmitHandler().submit(new EventPayload<>(1234L, datafile(), null));

        assertThat(response.getState()).isEqualTo(target);
        verify(repository).findById(1234L);
        verify(repository, never()).save(any());
        verifyNoInteractions(validator);
    }

    @Test
    void shouldRejectSubmissionValidationErrorsWithoutPersisting() {
        var data = datafile();
        when(validator.validate(data.getBatchFile(), SubmissionType.PCN_REGISTRATION))
            .thenReturn(List.of("Invalid registration file"));

        var response = configuration().getEvents().get("submitRegistrationDatafile")
            .getSubmitHandler().submit(new EventPayload<>(1234L, data, null));

        assertThat(response.getErrors()).containsExactly("Invalid registration file");
        assertThat(response.getState()).isNull();
        verifyNoInteractions(repository);
    }

    @Test
    void shouldNotReplaceAnExistingSubmission() {
        var data = datafile();
        when(validator.validate(data.getBatchFile(), SubmissionType.WARRANT)).thenReturn(List.of());
        when(repository.existsById(1234L)).thenReturn(true);

        var response = configuration().getEvents().get("submitWarrantDatafile")
            .getSubmitHandler().submit(new EventPayload<>(1234L, data, null));

        assertThat(response.getErrors()).containsExactly("A datafile has already been submitted for this case");
        assertThat(response.getState()).isNull();
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDelegatePageValidationWithTheEventType() {
        var data = datafile();
        var details = CaseDetails.<BatchDatafileCase, BatchDatafileCaseState>builder().data(data).build();
        when(validator.validate(data.getBatchFile(), SubmissionType.WARRANT_REISSUE))
            .thenReturn(List.of("Invalid reissue file"));

        var response = new BatchDatafileCaseConfiguration(repository, mapper, validator)
            .validateBatchFile(details, SubmissionType.WARRANT_REISSUE);

        assertThat(response.getData()).isSameAs(data);
        assertThat(response.getErrors()).containsExactly("Invalid reissue file");
    }

    private ResolvedCCDConfig<BatchDatafileCase, BatchDatafileCaseState, UserRole> configuration() {
        var resolved = new ResolvedCCDConfig<>(BatchDatafileCase.class, BatchDatafileCaseState.class,
            UserRole.class, Map.of(), ImmutableSet.copyOf(BatchDatafileCaseState.values()));
        var builder = new ConfigBuilderImpl<>(resolved);
        new BatchDatafileCaseConfiguration(repository, mapper, validator).configureDecentralised(builder);
        return builder.build();
    }

    private BatchDatafileCase datafile() {
        var data = new BatchDatafileCase();
        data.setBatchFile(Document.builder()
            .url("http://document-store/documents/68c89c98-399a-4eb4-b721-fd1d2109867f")
            .filename("batch.csv")
            .build());
        return data;
    }
}
