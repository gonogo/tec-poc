package uk.gov.hmcts.reform.tecpoc.ccd;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EnforcementCaseRepository {

    private final NamedParameterJdbcTemplate database;

    public void create(long caseReference, EnforcementCase enforcementCase) {
        database.update("""
            insert into tec_enforcement_case (
                case_reference, local_authority, submitter_email, received_via
            ) values (
                :caseReference, :localAuthority, :submitterEmail, :receivedVia
            )
            """, new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("localAuthority", enforcementCase.getLocalAuthority().name())
            .addValue("submitterEmail", enforcementCase.getSubmitterEmail())
            .addValue("receivedVia", enforcementCase.getReceivedVia().name()));
    }

    public boolean exists(long caseReference) {
        Integer count = database.queryForObject(
            """
            select count(*)
              from tec_enforcement_case
             where case_reference = :caseReference
            """,
            Map.of("caseReference", caseReference),
            Integer.class
        );
        return count != null && count > 0;
    }

    public EnforcementCase find(long caseReference) {
        return database.queryForObject("""
            select local_authority, submitter_email, received_via, created_at
              from tec_enforcement_case
             where case_reference = :caseReference
            """, Map.of("caseReference", caseReference), (resultSet, rowNumber) -> {
                EnforcementCase result = new EnforcementCase();
                result.setLocalAuthority(LocalAuthority.valueOf(resultSet.getString("local_authority")));
                result.setSubmitterEmail(resultSet.getString("submitter_email"));
                result.setReceivedVia(BatchReceivedVia.valueOf(resultSet.getString("received_via")));
                return result;
            });
    }

    public Instant findCreatedAt(long caseReference) {
        Timestamp createdAt = database.queryForObject("""
            select created_at
              from tec_enforcement_case
             where case_reference = :caseReference
            """, Map.of("caseReference", caseReference), Timestamp.class);
        return createdAt == null ? null : createdAt.toInstant();
    }

    /**
     * Linked PCN case references for this enforcement case.
     */
    public List<Long> findLinkedPcnCaseReferences(long enforcementCaseReference) {
        return database.queryForList("""
            select case_reference
              from tec_case
             where enforcement_case_reference = :enforcementCaseReference
             order by case_reference asc
            """, Map.of("enforcementCaseReference", enforcementCaseReference), Long.class);
    }

    public void linkPcnCase(long enforcementCaseReference, long pcnCaseReference) {
        int updated = database.update("""
            update tec_case
               set enforcement_case_reference = :enforcementCaseReference
             where case_reference = :pcnCaseReference
            """, new MapSqlParameterSource()
            .addValue("enforcementCaseReference", enforcementCaseReference)
            .addValue("pcnCaseReference", pcnCaseReference));
        if (updated == 0) {
            throw new IllegalArgumentException(
                "No TEC PCN case found for reference " + pcnCaseReference
            );
        }
    }

    public boolean pcnExists(long pcnCaseReference) {
        Integer count = database.queryForObject(
            """
            select count(*)
              from tec_case
             where case_reference = :caseReference
            """,
            Map.of("caseReference", pcnCaseReference),
            Integer.class
        );
        return count != null && count > 0;
    }

    /**
     * Returns the existing enforcement case reference for a PCN, or null if not linked.
     */
    public Long findEnforcementCaseReferenceForPcn(long pcnCaseReference) {
        try {
            Long ref = database.queryForObject("""
                select enforcement_case_reference
                  from tec_case
                 where case_reference = :caseReference
                """, Map.of("caseReference", pcnCaseReference), Long.class);
            return ref;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UUID insertDocument(
        long caseReference,
        String categoryId,
        String documentUrl,
        String documentBinaryUrl,
        String filename
    ) {
        UUID id = UUID.randomUUID();
        database.update("""
            insert into tec_enforcement_case_document (
                id, case_reference, category_id, document_url, document_binary_url, filename
            ) values (
                :id, :caseReference, :categoryId, :documentUrl, :documentBinaryUrl, :filename
            )
            """, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("caseReference", caseReference)
            .addValue("categoryId", categoryId)
            .addValue("documentUrl", documentUrl)
            .addValue("documentBinaryUrl", documentBinaryUrl)
            .addValue("filename", filename));
        return id;
    }

    public List<EnforcementCaseDocument> findDocuments(long caseReference) {
        return database.query("""
            select id, category_id, document_url, document_binary_url, filename, created_at
              from tec_enforcement_case_document
             where case_reference = :caseReference
             order by created_at asc, id asc
            """, Map.of("caseReference", caseReference), (resultSet, rowNumber) -> {
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                return new EnforcementCaseDocument(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("category_id"),
                    resultSet.getString("document_url"),
                    resultSet.getString("document_binary_url"),
                    resultSet.getString("filename"),
                    createdAt == null ? null : createdAt.toInstant()
                );
            });
    }
}
