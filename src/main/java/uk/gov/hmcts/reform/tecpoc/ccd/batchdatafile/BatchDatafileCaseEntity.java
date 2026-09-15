package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tec_batch_datafile_case")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BatchDatafileCaseEntity {

    @Id
    @Column(name = "case_reference", nullable = false)
    private Long caseReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", updatable = false)
    private SubmissionType submissionType;

    @Column(name = "batch_file_url", nullable = false)
    private String batchFileUrl;

    @Column(name = "batch_file_name", nullable = false)
    private String batchFileName;
}
