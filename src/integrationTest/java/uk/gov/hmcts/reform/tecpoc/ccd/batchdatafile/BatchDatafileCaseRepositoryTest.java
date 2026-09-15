package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uk.gov.hmcts.reform.tecpoc.support.PostgresIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BatchDatafileCaseRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private BatchDatafileCaseRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndLoadBatchDocumentMetadata() {
        BatchDatafileCaseEntity entity = BatchDatafileCaseEntity.builder()
            .caseReference(1234L)
            .submissionType(SubmissionType.PCN_REGISTRATION)
            .batchFileUrl("http://document-store/documents/68c89c98-399a-4eb4-b721-fd1d2109867f")
            .batchFileName("batch.csv")
            .build();

        repository.saveAndFlush(entity);
        entityManager.clear();

        BatchDatafileCaseEntity stored = repository.findById(1234L).orElseThrow();
        assertThat(stored.getBatchFileName()).isEqualTo("batch.csv");
        assertThat(stored.getSubmissionType()).isEqualTo(SubmissionType.PCN_REGISTRATION);
        assertThat(stored.getBatchFileUrl()).isEqualTo(entity.getBatchFileUrl());
    }
}
