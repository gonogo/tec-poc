package uk.gov.hmcts.reform.tecpoc.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import uk.gov.hmcts.reform.tecpoc.ccd.BatchValidationResult;
import uk.gov.hmcts.reform.tecpoc.ccd.LocalAuthority;

public record CreateBatchRequest(
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
    BatchValidationResult batchValidationResult,
    BatchCaseState targetState,
    List<BatchDocumentSeed> documents
) {

    public BatchCase toCaseData() {
        BatchCase batchCase = new BatchCase();
        batchCase.setBatchIdentifier(batchIdentifier);
        batchCase.setPcnCount(pcnCount);
        batchCase.setOperation(operation);
        batchCase.setReceivedVia(receivedVia);
        batchCase.setReceivedAt(receivedAt);
        batchCase.setLocalAuthority(localAuthority);
        batchCase.setBatchValidationResult(batchValidationResult);
        return batchCase;
    }

    @JsonIgnore
    public BatchCaseState resolvedTargetState() {
        return targetState == null ? BatchCaseState.QUEUED_FOR_PROCESSING : targetState;
    }

    public record BatchDocumentSeed(
        @NotBlank String categoryId,
        @NotBlank String documentUrl,
        @NotBlank String documentBinaryUrl,
        @NotBlank String filename
    ) {
    }
}
