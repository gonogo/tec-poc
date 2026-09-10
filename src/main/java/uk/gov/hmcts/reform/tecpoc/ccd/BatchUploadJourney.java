package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Placeholder Upload batch file journey helpers (validation copy and confirmation text).
 */
final class BatchUploadJourney {

    static final int PLACEHOLDER_EXCLUDED_PCN_COUNT = 23;
    static final int PLACEHOLDER_PCN_COUNT = 200;
    static final LocalAuthority PLACEHOLDER_LOCAL_AUTHORITY = LocalAuthority.WESTMINSTER;

    private BatchUploadJourney() {
    }

    static void applyValidationPlaceholder(BatchCase data) {
        data.setExcludedPcnCount(PLACEHOLDER_EXCLUDED_PCN_COUNT);
    }

    static void applySubmitDefaults(long caseReference, BatchCase data) {
        if (data.getReceivedVia() == null) {
            data.setReceivedVia(BatchReceivedVia.UPLOAD);
        }
        if (data.getReceivedAt() == null) {
            data.setReceivedAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        if (data.getLocalAuthority() == null) {
            data.setLocalAuthority(PLACEHOLDER_LOCAL_AUTHORITY);
        }
        if (data.getPcnCount() == null) {
            data.setPcnCount(PLACEHOLDER_PCN_COUNT);
        }
        if (data.getBatchIdentifier() == null || data.getBatchIdentifier().isBlank()) {
            data.setBatchIdentifier(placeholderBatchIdentifier(caseReference));
        }
        if (data.getExcludedPcnCount() == null) {
            data.setExcludedPcnCount(PLACEHOLDER_EXCLUDED_PCN_COUNT);
        }
        // Validation outcome is recorded later; new batches stay unset → "Not yet validated".
        data.setBatchValidationResult(null);
    }

    static boolean hasAcceptedStatementOfTruth(BatchCase data) {
        List<BatchStatementOfTruthAgreement> agreements = data.getBatchStatementOfTruth();
        return agreements != null && agreements.contains(BatchStatementOfTruthAgreement.BELIEVE_TRUE);
    }

    static String placeholderBatchIdentifier(long caseReference) {
        long number = Math.floorMod(caseReference, 1_000_000L);
        return "RUP" + String.format("%06d", number);
    }

    static String confirmationHeader(BatchCase data) {
        String batchType = data.getOperation() == null
            ? "Batch"
            : data.getOperation().getShortLabel();
        return "# " + batchType + " submitted";
    }

    static String confirmationBody(BatchCase data, long caseReference) {
        int excluded = data.getExcludedPcnCount() == null
            ? PLACEHOLDER_EXCLUDED_PCN_COUNT
            : data.getExcludedPcnCount();
        String submissionNoun = confirmationSubmissionNoun(data.getOperation());
        return """
            ## Case number: %s

            Your %s have been submitted and will be processed overnight. %d PCNs have not been \
            included in the submission and are included in an exception report which you can \
            access from the batch case.
            """.formatted(caseReference, submissionNoun, excluded).stripIndent().trim();
    }

    private static String confirmationSubmissionNoun(BatchOperation operation) {
        if (operation == BatchOperation.WARRANT_AUTH_REQUESTS) {
            return "PCN warrant authorisations";
        }
        if (operation == null) {
            return "batch requests";
        }
        return operation.getShortLabel().toLowerCase() + " batch";
    }
}
