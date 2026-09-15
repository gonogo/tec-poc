package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchDatafileCaseRepository extends JpaRepository<BatchDatafileCaseEntity, Long> {
}
