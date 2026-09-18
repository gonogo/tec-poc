package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnforcementCase {

    /**
     * Case-view display for CCD state. Populated by {@link EnforcementCaseView}.
     */
    @CCD(label = "Status")
    private String statusDisplay;

    @CCD(
        label = "Local authority",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "LocalAuthority"
    )
    private LocalAuthority localAuthority;

    @CCD(label = "Submitter email", typeOverride = FieldType.Email)
    private String submitterEmail;

    @CCD(
        label = "Received via",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "BatchReceivedVia"
    )
    private BatchReceivedVia receivedVia;

    /**
     * Standard CCD Linked Cases collection. Field id must remain {@code caseLinks}.
     * Populated by {@link EnforcementCaseView} from {@code tec_case.enforcement_case_reference}.
     */
    @CCD(
        label = "Linked cases",
        typeOverride = FieldType.Collection,
        typeParameterOverride = "CaseLink"
    )
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ListValue<CaseLink>> caseLinks;

    @CCD(label = "Component Launcher (for displaying Linked Cases data)")
    @JsonProperty("LinkedCasesComponentLauncher")
    private ComponentLauncher linkedCasesComponentLauncher;

    /**
     * Case File View source documents. Populated by {@link EnforcementCaseView}.
     */
    @CCD(label = "All documents", searchable = false)
    private List<ListValue<Document>> allDocuments;

    /**
     * Event-only field used by {@code attachCaseFileDocument}.
     */
    @CCD(label = "Case file document", searchable = false)
    private Document caseFileDocument;

    @CCD(label = "Case file view")
    private ComponentLauncher caseFileView;

    @CCD(label = "Tasks", searchable = false)
    private String tasksMarkdown;

    /**
     * CCD shell for the ExUI Roles and access tab.
     */
    @CCD(label = "Roles and access", searchable = false)
    private String rolesAndAccessMarkdown;
}
