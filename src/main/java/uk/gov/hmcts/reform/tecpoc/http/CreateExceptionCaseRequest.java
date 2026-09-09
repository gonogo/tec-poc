package uk.gov.hmcts.reform.tecpoc.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import uk.gov.hmcts.reform.tecpoc.ccd.ExceptionCase;

public record CreateExceptionCaseRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Z]{2,3}[0-9]{7}[0-9A][0-9]$")
    String penaltyChargeNumber
) {

    public ExceptionCase toCaseData() {
        ExceptionCase exceptionCase = new ExceptionCase();
        exceptionCase.setPenaltyChargeNumber(penaltyChargeNumber);
        return exceptionCase;
    }
}
