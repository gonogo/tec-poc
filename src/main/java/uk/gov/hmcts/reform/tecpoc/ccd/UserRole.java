package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

/**
 * Roles supplied by Access Management. These roles do not carry CCD permissions directly.
 */
@RequiredArgsConstructor
public enum UserRole implements HasRole {

    TEC_BATCH_SUBMITTER("tec-batch-submitter"),
    TEC_BATCH_READER("tec-batch-reader");

    private final String role;

    @Override
    @JsonValue
    public String getRole() {
        return role;
    }

    @Override
    public String getCaseTypePermissions() {
        return "";
    }

    @JsonCreator
    public static UserRole fromRole(String role) {
        return Arrays.stream(values())
            .filter(candidate -> candidate.role.equals(role))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown TEC user role: " + role));
    }
}
