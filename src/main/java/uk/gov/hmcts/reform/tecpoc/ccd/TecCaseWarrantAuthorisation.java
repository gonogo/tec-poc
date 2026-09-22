package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.LocalDate;
import java.util.UUID;

public record TecCaseWarrantAuthorisation(
    UUID id,
    LocalDate dateOfIssue,
    LocalDate dateOfExpiry,
    WarrantAuthorisationStatus status
) {
}
