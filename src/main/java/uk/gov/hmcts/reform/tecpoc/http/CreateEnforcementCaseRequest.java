package uk.gov.hmcts.reform.tecpoc.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchReceivedVia;
import uk.gov.hmcts.reform.tecpoc.ccd.EnforcementCase;
import uk.gov.hmcts.reform.tecpoc.ccd.LocalAuthority;

public record CreateEnforcementCaseRequest(
    @NotNull LocalAuthority localAuthority,
    @NotBlank @Email String submitterEmail,
    @NotNull BatchReceivedVia receivedVia
) {

    public EnforcementCase toCaseData() {
        EnforcementCase enforcementCase = new EnforcementCase();
        enforcementCase.setLocalAuthority(localAuthority);
        enforcementCase.setSubmitterEmail(submitterEmail);
        enforcementCase.setReceivedVia(receivedVia);
        return enforcementCase;
    }
}
