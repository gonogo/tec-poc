package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchCase {

    @CCD(label = "Batch identifier")
    private String batchIdentifier;

    @CCD(label = "Number of PCNs")
    private Integer pcnCount;

    @CCD(
        label = "Operation",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchOperation"
    )
    private BatchOperation operation;

    @CCD(
        label = "Received via",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchReceivedVia"
    )
    private BatchReceivedVia receivedVia;

    @CCD(label = "Received at")
    private LocalDateTime receivedAt;

    @CCD(
        label = "Local authority",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "LocalAuthority"
    )
    private LocalAuthority localAuthority;

    @CCD(
        label = "Batch validation result",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchValidationResult"
    )
    private BatchValidationResult batchValidationResult;

    /**
     * Case-view display for validation. Always populated so ExUI shows the row even when
     * {@link #batchValidationResult} is unset ({@code @JsonInclude(NON_NULL)} would otherwise omit it).
     */
    @CCD(label = "Batch validation result")
    private String batchValidationResultDisplay;

    /**
     * Case File View source documents. Populated by {@link BatchCaseView}; not shown on Case details.
     */
    @CCD(label = "All documents", searchable = false)
    private List<ListValue<Document>> allDocuments;

    /**
     * Event-only field used by {@code attachBatchDocument}.
     */
    @CCD(label = "Batch file document", searchable = false)
    private Document batchFileDocument;

    @CCD(label = "Case file view")
    private ComponentLauncher caseFileView;

    @CCD(label = "Tasks", searchable = false)
    private String tasksMarkdown;
}
