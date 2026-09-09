package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Document folders shown in the ExUI Case File View for TEC exception cases.
 */
@RequiredArgsConstructor
@Getter
public enum ExceptionCaseFileCategory {

    HEARING_DOCUMENTS("hearingDocuments", "Hearing documents", 1),
    ORDERS_AND_NOTICES_OF_HEARINGS(
        "ordersAndNoticesOfHearings",
        "Orders and notices of hearings",
        2
    ),
    APPLICATIONS("applications", "Applications", 3),
    CORRESPONDENCE("correspondence", "Correspondence", 4),
    UNCATEGORISED("uncategorisedDocuments", "Uncategorised", 5);

    private final String id;
    private final String label;
    private final int displayOrder;

    public static Optional<ExceptionCaseFileCategory> resolve(String folderOrCategoryId) {
        if (folderOrCategoryId == null || folderOrCategoryId.isBlank()) {
            return Optional.empty();
        }
        String needle = folderOrCategoryId.trim();
        return Arrays.stream(values())
            .filter(category -> category.id.equalsIgnoreCase(needle)
                || category.label.equalsIgnoreCase(needle))
            .findFirst();
    }

    public static ExceptionCaseFileCategory require(String folderOrCategoryId) {
        return resolve(folderOrCategoryId).orElseThrow(() -> new IllegalArgumentException(
            "Unknown Case File View folder: '" + folderOrCategoryId + "'. "
                + "Use a category id or label from: "
                + knownFoldersDescription()
        ));
    }

    public static String knownFoldersDescription() {
        return Arrays.stream(values())
            .map(category -> category.id + " (\"" + category.label + "\")")
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }

    public static String normalisedCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return UNCATEGORISED.getId();
        }
        return require(categoryId).getId();
    }
}
