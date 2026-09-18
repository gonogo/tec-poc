package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.Objects;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;

@Component
public class BatchDatafileCaseMapper {

    public BatchDatafileCaseEntity toEntity(long caseReference, BatchDatafileCase batchBatchDatafileCase) {
        Document batchFile = Objects.requireNonNull(
            batchBatchDatafileCase.getBatchFile(),
            "Batch file is required"
        );
        Organisation organisation = requireOwningOrganisation(batchBatchDatafileCase);

        return BatchDatafileCaseEntity.builder()
            .caseReference(caseReference)
            .batchType(batchBatchDatafileCase.getBatchType())
            .batchFileUrl(batchFile.getUrl())
            .batchFileName(batchFile.getFilename())
            .owningLocalAuthorityOrganisationId(organisation.getOrganisationId())
            .owningLocalAuthorityOrganisationName(organisation.getOrganisationName())
            .build();
    }

    public BatchDatafileCase toCase(BatchDatafileCaseEntity entity) {
        Document batchFile = Document.builder()
            .url(entity.getBatchFileUrl())
            .filename(entity.getBatchFileName())
            .binaryUrl(entity.getBatchFileUrl() + "/binary")
            .categoryId(CaseFileCategory.SUBMITTED_DATAFILE.getId())
            .build();

        BatchDatafileCase batchBatchDatafileCase = new BatchDatafileCase();
        batchBatchDatafileCase.setBatchFile(batchFile);
        batchBatchDatafileCase.setBatchType(entity.getBatchType());
        String organisationId = entity.getOwningLocalAuthorityOrganisationId();
        batchBatchDatafileCase.setOwningLocalAuthorityOrganisationId(organisationId);
        batchBatchDatafileCase.setOwningLocalAuthorityPolicy(OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation.builder()
                .organisationId(organisationId)
                .organisationName(entity.getOwningLocalAuthorityOrganisationName())
                .build())
            .prepopulateToUsersOrganisation(YesOrNo.YES)
            .orgPolicyCaseAssignedRole(UserRole.TEC_BATCH_READER)
            .build());
        batchBatchDatafileCase.setCaseAccessGroups(BatchDatafileAccessGroup.projection(organisationId));
        return batchBatchDatafileCase;
    }

    private Organisation requireOwningOrganisation(BatchDatafileCase caseData) {
        OrganisationPolicy<UserRole> policy = Objects.requireNonNull(
            caseData.getOwningLocalAuthorityPolicy(),
            "Owning local authority policy is required"
        );
        if (policy.getOrgPolicyCaseAssignedRole() != UserRole.TEC_BATCH_READER) {
            throw new IllegalArgumentException("Owning local authority policy must assign tec-batch-reader");
        }
        Organisation organisation = Objects.requireNonNull(
            policy.getOrganisation(),
            "Owning local authority organisation is required"
        );
        if (organisation.getOrganisationId() == null || organisation.getOrganisationId().isBlank()) {
            throw new IllegalArgumentException("Owning local authority organisation ID is required");
        }
        return organisation;
    }
}
