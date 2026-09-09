package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExceptionCaseRepository {

    private final NamedParameterJdbcTemplate database;

    public void create(long caseReference, ExceptionCase exceptionCase) {
        database.update("""
            insert into tec_exception_case (
                case_reference, penalty_charge_number
            ) values (
                :caseReference, :penaltyChargeNumber
            )
            """, new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("penaltyChargeNumber", exceptionCase.getPenaltyChargeNumber()));
    }

    public ExceptionCase find(long caseReference) {
        return database.queryForObject("""
            select penalty_charge_number, reject_reason
              from tec_exception_case
             where case_reference = :caseReference
            """, Map.of("caseReference", caseReference), (resultSet, rowNumber) -> {
                ExceptionCase result = new ExceptionCase();
                result.setPenaltyChargeNumber(resultSet.getString("penalty_charge_number"));
                String rejectReason = resultSet.getString("reject_reason");
                if (rejectReason != null) {
                    result.setRejectReason(ExceptionRejectReason.valueOf(rejectReason));
                }
                return result;
            });
    }

    public void recordRejectReason(long caseReference, ExceptionRejectReason reason) {
        database.update("""
            update tec_exception_case
               set reject_reason = :rejectReason
             where case_reference = :caseReference
            """, new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("rejectReason", reason == null ? null : reason.name()));
    }

    public void updatePenaltyChargeNumber(long caseReference, String penaltyChargeNumber) {
        database.update("""
            update tec_exception_case
               set penalty_charge_number = :penaltyChargeNumber
             where case_reference = :caseReference
            """, new MapSqlParameterSource()
            .addValue("caseReference", caseReference)
            .addValue("penaltyChargeNumber", penaltyChargeNumber));
    }
}
