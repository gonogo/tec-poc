package uk.gov.hmcts.reform.tecpoc.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchCase;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchCaseState;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchOperation;
import uk.gov.hmcts.reform.tecpoc.ccd.BatchReceivedVia;
import uk.gov.hmcts.reform.tecpoc.ccd.LocalAuthority;

public record CreateBatchRequest(
    @NotBlank
    @Pattern(regexp = "^R[A-Z]{2,3}[0-9]{5}$")
    String fileIdentifier,
    @NotBlank
    @Pattern(regexp = "^R[A-Z]{2,3}[0-9]{6}$")
    String batchIdentifier,
    @NotNull
    @Min(1)
    @Max(100000)
    Integer pcnCount,
    @NotNull BatchOperation operation,
    @NotNull BatchReceivedVia receivedVia,
    @NotNull LocalDateTime receivedAt,
    @NotNull LocalAuthority localAuthority,
    @NotBlank
    @Email
    String submitterEmail,
    BatchCaseState targetState,
    List<BatchDocumentSeed> documents
) {

    public BatchCase toCaseData() {
        BatchCase batchCase = new BatchCase();
        batchCase.setFileIdentifier(fileIdentifier);
        batchCase.setBatchIdentifier(batchIdentifier);
        batchCase.setPcnCount(pcnCount);
        batchCase.setOperation(operation);
        batchCase.setReceivedVia(receivedVia);
        batchCase.setReceivedAt(receivedAt);
        batchCase.setLocalAuthority(localAuthority);
        if (localAuthority != null) {
            batchCase.setCaseAccessCategory(localAuthority.getCode());
        }
        batchCase.setSubmitterEmail(submitterEmail);
        return batchCase;
    }

    @JsonIgnore
    public BatchCaseState resolvedTargetState() {
        return targetState == null ? BatchCaseState.QUEUED_FOR_PROCESSING : targetState;
    }

    @AssertTrue(message = "file and batch identifiers must have the same authority prefix")
    @JsonIgnore
    public boolean isIdentifierPrefixConsistent() {
        if (fileIdentifier == null || batchIdentifier == null
            || !fileIdentifier.matches("^R[A-Z]{2,3}[0-9]{5}$")
            || !batchIdentifier.matches("^R[A-Z]{2,3}[0-9]{6}$")) {
            return true;
        }
        String filePrefix = fileIdentifier.substring(1, fileIdentifier.length() - 5);
        String batchPrefix = batchIdentifier.substring(1, batchIdentifier.length() - 6);
        return filePrefix.equals(batchPrefix);
    }

    public record BatchDocumentSeed(
        @NotBlank String categoryId,
        @NotBlank String documentUrl,
        @NotBlank String documentBinaryUrl,
        @NotBlank String filename
    ) {
    }
}
