package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarrantAuthorisation {

    @CCD(label = "Date of issue")
    private LocalDate dateOfIssue;

    @CCD(label = "Date of expiry")
    private LocalDate dateOfExpiry;

    @CCD(
        label = "Status",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "WarrantAuthorisationStatus"
    )
    private WarrantAuthorisationStatus status;
}
