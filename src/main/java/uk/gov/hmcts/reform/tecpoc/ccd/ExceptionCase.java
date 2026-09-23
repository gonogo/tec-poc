package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ExceptionCase {

    /**
     * Case-view display for CCD state. Populated by {@link ExceptionCaseView} so ExUI can show
     * State at the top of Case details (state itself is not case data).
     */
    @CCD(label = "State")
    private String statusDisplay;

    /**
     * Always populated by {@link ExceptionCaseView} as {@code —} for the PoC.
     */
    @CCD(label = "Form validation result", searchable = false)
    private String formValidationResultDisplay;

    /**
     * Always populated by {@link ExceptionCaseView} as {@code —} for the PoC.
     */
    @CCD(label = "Associated TEC case", searchable = false)
    private String associatedTecCaseDisplay;

    @CCD(label = "PCN")
    private String penaltyChargeNumber;

    @CCD(
        label = "Reject reason",
        typeOverride = FieldType.FixedRadioList,
        typeParameterOverride = "ExceptionRejectReason",
        searchable = false
    )
    private ExceptionRejectReason rejectReason;

    @CCD(label = "Comment", typeOverride = FieldType.TextArea, searchable = false)
    private String rejectComment;

    /**
     * Case File View source documents. Populated by {@link ExceptionCaseView}; not shown on Case details.
     */
    @CCD(label = "All documents", searchable = false)
    private List<ListValue<Document>> allDocuments;

    @CCD(label = "Case file view")
    private ComponentLauncher caseFileView;

    /**
     * CCD shell for the ExUI Roles and access tab. Not the real Work Allocation / CAA UI.
     */
    @CCD(label = "Roles and access", searchable = false)
    private String rolesAndAccessMarkdown;

    @CCD(label = "Tasks", searchable = false)
    private String tasksMarkdown;
}
