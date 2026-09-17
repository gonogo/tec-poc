package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Document folders shown in the ExUI Case File View for every TEC batch.
 */
@RequiredArgsConstructor
@Getter
public enum BatchFileCategory {

    INPUTS("inputs", "Inputs", 1),
    OUTPUTS("outputs", "Outputs", 2);

    private final String id;
    private final String label;
    private final int displayOrder;

    public static Optional<BatchFileCategory> resolve(String folderOrCategoryId) {
        if (folderOrCategoryId == null || folderOrCategoryId.isBlank()) {
            return Optional.empty();
        }
        String needle = folderOrCategoryId.trim();
        return Arrays.stream(values())
            .filter(category -> category.id.equalsIgnoreCase(needle)
                || category.label.equalsIgnoreCase(needle))
            .findFirst();
    }

    public static BatchFileCategory require(String folderOrCategoryId) {
        return resolve(folderOrCategoryId).orElseThrow(() -> new IllegalArgumentException(
            "Unknown Batch File View folder: '" + folderOrCategoryId + "'. "
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

    /**
     * Normalises a blank category to Inputs; rejects unknown non-blank values.
     */
    public static String normalisedCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return INPUTS.getId();
        }
        return require(categoryId).getId();
    }
}
