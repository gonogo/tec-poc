package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchCase {

    @CCD(label = "Batch identifier")
    private String batchIdentifier;

    @CCD(label = "Number of PCNs in batch")
    private Integer pcnCount;

    /**
     * Case-view display for how many PCNs have been processed. Populated by
     * {@link BatchCaseView}: emdash while queued, otherwise the processed count.
     */
    @CCD(label = "Number of PCNs processed", searchable = false)
    private String pcnProcessedCountDisplay;

    /**
     * Case-view display: registration fee paid once processing is complete
     * ({@code Number of PCNs processed × £11}). Populated by {@link BatchCaseView}.
     */
    @CCD(label = "Fees paid", typeOverride = FieldType.MoneyGBP, min = 0, max = 99999999, searchable = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer feesPaid;

    /**
     * Case-view display: registration fee due while queued
     * ({@code Number of PCNs in batch × £11}). Populated by {@link BatchCaseView}.
     */
    @CCD(label = "Fees due", typeOverride = FieldType.MoneyGBP, min = 0, max = 99999999, searchable = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer feesDue;

    @CCD(
        label = "Batch type",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchOperation"
    )
    private BatchOperation operation;

    /**
     * Upload batch file wizard only ({@code FixedRadioList} with descriptions). Copied to
     * {@link #operation} on submit; not searchable and not shown on Case details.
     */
    @CCD(
        label = "Select batch type",
        typeOverride = FieldType.FixedRadioList,
        typeParameterOverride = "BatchTypeOption",
        searchable = false
    )
    private BatchTypeOption batchTypeSelection;

    @CCD(
        label = "Received via",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchReceivedVia"
    )
    private BatchReceivedVia receivedVia;

    @CCD(label = "Email received at")
    private LocalDateTime receivedAt;

    @CCD(
        label = "Local authority",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "LocalAuthority"
    )
    private LocalAuthority localAuthority;

    /**
     * CCD case-access category (exact field id). Set from {@link #localAuthority} on create/upload;
     * not shown on case-view tabs.
     */
    @CCD(label = "Case access category", searchable = false)
    @JsonProperty("CaseAccessCategory")
    private String caseAccessCategory;

    /**
     * Case-view display for CCD state. Populated by {@link BatchCaseView} so ExUI can show
     * status at the top of Case details (state itself is not case data).
     */
    @CCD(label = "Status")
    private String statusDisplay;

    @CCD(
        label = "Batch validation result",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchValidationResult"
    )
    private BatchValidationResult batchValidationResult;

    /**
     * Case-view display for validation. Always populated so ExUI shows the row even when
     * {@link #batchValidationResult} is unset ({@code @JsonInclude(NON_NULL)} would otherwise omit it).
     * Unset results display as {@code Not yet validated}.
     */
    @CCD(label = "Batch validation result")
    private String batchValidationResultDisplay;

    /**
     * PCNs excluded from the batch after placeholder validation (Upload batch file journey).
     */
    @CCD(label = "Excluded PCNs", searchable = false)
    private Integer excludedPcnCount;

    /**
     * Statement of truth agreement (Upload batch file journey). MultiSelectList with one option
     * renders as a checkbox, matching the PCS pattern. Label is shown on Check your answers
     * (and beside the checkbox on the Statement of truth page).
     */
    @CCD(
        label = "Statement of truth",
        typeOverride = FieldType.MultiSelectList,
        typeParameterOverride = "BatchStatementOfTruthAgreement",
        searchable = false
    )
    private List<BatchStatementOfTruthAgreement> batchStatementOfTruth;

    /**
     * Input documents for Case details (media viewer). Populated by {@link BatchCaseView}.
     */
    @CCD(label = "Inputs", searchable = false)
    private List<ListValue<Document>> inputDocuments;

    /**
     * Output documents for Case details (media viewer). Populated by {@link BatchCaseView}
     * when the batch has outputs; otherwise {@link #outputsDisplay} is used.
     */
    @CCD(label = "Outputs", searchable = false)
    private List<ListValue<Document>> outputDocuments;

    /**
     * Shown as Outputs when there are no output documents ({@code -}).
     */
    @CCD(label = "Outputs", searchable = false)
    private String outputsDisplay;

    /**
     * Event-only field used by {@code attachBatchDocument} and {@code uploadBatch}.
     */
    @CCD(label = "Batch file", searchable = false)
    private Document batchFileDocument;

    @CCD(label = "Tasks", searchable = false)
    private String tasksMarkdown;
}
