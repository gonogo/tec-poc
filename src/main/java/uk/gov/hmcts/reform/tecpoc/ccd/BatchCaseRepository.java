package uk.gov.hmcts.reform.tecpoc.ccd;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BatchCaseRepository {

    private final NamedParameterJdbcTemplate database;

    public void create(long caseReference, BatchCase batchCase) {
        database.update("""
            insert into tec_batch (
                case_reference, batch_identifier, pcn_count, operation, received_via,
                received_at, local_authority, batch_validation_result
            ) values (
                :caseReference, :batchIdentifier, :pcnCount, :operation, :receivedVia,
                :receivedAt, :localAuthority, :batchValidationResult
            )
            """, parameters(caseReference, batchCase));
    }

    public BatchCase find(long caseReference) {
        return database.queryForObject("""
            select batch_identifier, pcn_count, operation, received_via, received_at,
                   local_authority, batch_validation_result, batch_validation_result_display
              from tec_batch
             where case_reference = :caseReference
            """, Map.of("caseReference", caseReference), (resultSet, rowNumber) -> {
                BatchCase result = new BatchCase();
                result.setBatchIdentifier(resultSet.getString("batch_identifier"));
                result.setPcnCount(resultSet.getInt("pcn_count"));
                result.setOperation(BatchOperation.valueOf(resultSet.getString("operation")));
                result.setReceivedVia(BatchReceivedVia.valueOf(resultSet.getString("received_via")));
                Timestamp receivedAt = resultSet.getTimestamp("received_at");
                if (receivedAt != null) {
                    result.setReceivedAt(receivedAt.toLocalDateTime());
                }
                result.setLocalAuthority(LocalAuthority.valueOf(resultSet.getString("local_authority")));
                String validationResult = resultSet.getString("batch_validation_result");
                if (validationResult != null) {
                    result.setBatchValidationResult(BatchValidationResult.valueOf(validationResult));
                }
                result.setBatchValidationResultDisplay(
                    resultSet.getString("batch_validation_result_display")
                );
                return result;
            });
    }

    public void updateValidationResultDisplay(long caseReference, String validationResultDisplay) {
        database.update("""
            update tec_batch
               set batch_validation_result_display = :validationResultDisplay
             where case_reference = :caseReference
            """, new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("validationResultDisplay", validationResultDisplay));
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
            insert into tec_batch_document (
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

    public List<BatchCaseDocument> findDocuments(long caseReference) {
        return database.query("""
            select id, category_id, document_url, document_binary_url, filename, created_at
              from tec_batch_document
             where case_reference = :caseReference
             order by created_at asc, id asc
            """, Map.of("caseReference", caseReference), (resultSet, rowNumber) -> {
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                return new BatchCaseDocument(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("category_id"),
                    resultSet.getString("document_url"),
                    resultSet.getString("document_binary_url"),
                    resultSet.getString("filename"),
                    createdAt == null ? null : createdAt.toInstant()
                );
            });
    }

    private MapSqlParameterSource parameters(long caseReference, BatchCase batchCase) {
        BatchValidationResult validationResult = batchCase.getBatchValidationResult();
        return new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("batchIdentifier", batchCase.getBatchIdentifier())
            .addValue("pcnCount", batchCase.getPcnCount())
            .addValue("operation", batchCase.getOperation().name())
            .addValue("receivedVia", batchCase.getReceivedVia().name())
            .addValue("receivedAt", batchCase.getReceivedAt())
            .addValue("localAuthority", batchCase.getLocalAuthority().name())
            .addValue(
                "batchValidationResult",
                validationResult == null ? null : validationResult.name()
            );
    }
}
