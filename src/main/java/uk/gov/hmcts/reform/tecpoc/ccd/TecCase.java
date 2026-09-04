package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDate;
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
public class TecCase {

    @CCD(label = "File identifier")
    private String fileIdentifier;

    @CCD(label = "Batch identifier")
    private String batchIdentifier;

    @CCD(label = "Penalty charge number")
    private String penaltyChargeNumber;

    @CCD(label = "Respondent details 1")
    private String respondentDetails1;

    @CCD(label = "Respondent details 2")
    private String respondentDetails2;

    @CCD(label = "Respondent details 3")
    private String respondentDetails3;

    @CCD(label = "Respondent details 4")
    private String respondentDetails4;

    @CCD(label = "Respondent details 5")
    private String respondentDetails5;

    @CCD(label = "Respondent details 6")
    private String respondentDetails6;

    @CCD(label = "Vehicle registration number")
    private String vehicleRegistrationNumber;

    @CCD(label = "Nature of offence")
    private String natureOfOffence;

    @CCD(label = "Date charge certificate served")
    private String dateChargeCertificateServed;

    @CCD(label = "Amount due", typeOverride = FieldType.MoneyGBP, min = 0, max = 999999)
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer amountDue;

    @CCD(label = "Payment status")
    private String paymentStatus;

    @CCD(label = "Payment reference")
    private String paymentReference;

    @CCD(label = "Closure reason")
    private String closureReason;

    @CCD(label = "Registration authorisation document")
    private String registrationDocument;

    @CCD(label = "Registration date")
    private LocalDate registrationDate;

    @CCD(
        label = "Form validation result",
        typeOverride = FieldType.FixedRadioList,
        typeParameterOverride = "FormValidationResult"
    )
    private FormValidationResult formValidationResult;

    /**
     * Case-view display for form validation. Always populated so ExUI shows the row even when
     * {@link #formValidationResult} is unset ({@code @JsonInclude(NON_NULL)} would otherwise omit it).
     */
    @CCD(label = "Form validation result")
    private String formValidationResultDisplay;

    @CCD(label = "Date received")
    private LocalDate applicationDateReceived;

    @CCD(
        label = "Type",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "ApplicationTimeliness"
    )
    private ApplicationTimeliness applicationType;

    @CCD(
        label = "TE7 submitted?",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "YesNo"
    )
    private YesNo applicationTe7Submitted;

    @CCD(
        label = "Form",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "ApplicationForm"
    )
    private ApplicationForm applicationForm;

    @CCD(label = "Penalty Charge Number")
    private String applicationPenaltyChargeNumber;

    @CCD(label = "Vehicle reg")
    private String applicationVehicleRegistration;

    @CCD(label = "Applicant")
    private String applicationApplicant;

    @CCD(label = "Location of contravention")
    private String applicationLocationOfContravention;

    @CCD(label = "Date of contravention")
    private LocalDate applicationDateOfContravention;

    @CCD(label = "Title")
    private String applicationTitle;

    @CCD(label = "Full name")
    private String applicationFullName;

    @CCD(label = "Company name")
    private String applicationCompanyName;

    @CCD(label = "Address")
    private String applicationAddress;

    @CCD(label = "Postcode")
    private String applicationPostcode;

    @CCD(
        label = "Declaration",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "ApplicationDeclaration"
    )
    private ApplicationDeclaration applicationDeclaration;

    @CCD(
        label = "Reasons given",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "YesNo"
    )
    private YesNo applicationReasonsGiven;

    @CCD(label = "Date it was paid")
    private LocalDate applicationDatePaid;

    @CCD(label = "How it was paid")
    private String applicationHowPaid;

    @CCD(label = "To whom it was paid")
    private String applicationPaidTo;

    @CCD(
        label = "Form validation result",
        typeOverride = FieldType.FixedRadioList,
        typeParameterOverride = "FormValidationResult"
    )
    private FormValidationResult timeExtensionFormValidationResult;

    /**
     * Case-view display for time-extension form validation. Always populated so ExUI shows the row
     * even when {@link #timeExtensionFormValidationResult} is unset.
     */
    @CCD(label = "Form validation result")
    private String timeExtensionFormValidationResultDisplay;

    @CCD(
        label = "Form",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "TimeExtensionForm"
    )
    private TimeExtensionForm timeExtensionForm;

    @CCD(label = "Penalty Charge Number")
    private String timeExtensionPenaltyChargeNumber;

    @CCD(label = "Vehicle reg")
    private String timeExtensionVehicleRegistration;

    @CCD(label = "Applicant")
    private String timeExtensionApplicant;

    @CCD(label = "Location of contravention")
    private String timeExtensionLocationOfContravention;

    @CCD(label = "Date of contravention")
    private LocalDate timeExtensionDateOfContravention;

    @CCD(label = "Title")
    private String timeExtensionTitle;

    @CCD(label = "Other title")
    private String timeExtensionOtherTitle;

    @CCD(label = "Full name")
    private String timeExtensionFullName;

    @CCD(label = "Company name")
    private String timeExtensionCompanyName;

    @CCD(label = "Address")
    private String timeExtensionAddress;

    @CCD(label = "Postcode")
    private String timeExtensionPostcode;

    @CCD(
        label = "Permission sought",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "TimeExtensionPermissionType"
    )
    private TimeExtensionPermissionType timeExtensionPermissionType;

    @CCD(
        label = "Reasons given",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "YesNo"
    )
    private YesNo timeExtensionReasonsGiven;

    @CCD(
        label = "Signed and dated",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "YesNo"
    )
    private YesNo timeExtensionSignedAndDated;

    @CCD(
        label = "Signed by",
        typeOverride = FieldType.FixedList,
        typeParameterOverride = "TimeExtensionSignedBy"
    )
    private TimeExtensionSignedBy timeExtensionSignedBy;

    @CCD(label = "Date signed")
    private LocalDate timeExtensionDateSigned;

    @CCD(label = "Print full name")
    private String timeExtensionPrintFullName;

    /**
     * Case File View source documents. Populated by {@link TecCaseView}; not shown on Case details.
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
}
