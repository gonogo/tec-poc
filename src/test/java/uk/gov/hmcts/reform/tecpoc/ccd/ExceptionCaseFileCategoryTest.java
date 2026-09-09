package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExceptionCaseFileCategoryTest {

    @Test
    void shouldResolveByIdIgnoringCase() {
        assertThat(ExceptionCaseFileCategory.resolve("hearingDocuments"))
            .contains(ExceptionCaseFileCategory.HEARING_DOCUMENTS);
        assertThat(ExceptionCaseFileCategory.resolve("APPLICATIONS"))
            .contains(ExceptionCaseFileCategory.APPLICATIONS);
    }

    @Test
    void shouldResolveByLabelIgnoringCase() {
        assertThat(ExceptionCaseFileCategory.resolve("Orders and notices of hearings"))
            .contains(ExceptionCaseFileCategory.ORDERS_AND_NOTICES_OF_HEARINGS);
        assertThat(ExceptionCaseFileCategory.resolve("Uncategorised"))
            .contains(ExceptionCaseFileCategory.UNCATEGORISED);
    }

    @Test
    void shouldNormaliseBlankCategoryToUncategorised() {
        assertThat(ExceptionCaseFileCategory.normalisedCategoryId(null))
            .isEqualTo("uncategorisedDocuments");
        assertThat(ExceptionCaseFileCategory.normalisedCategoryId("  "))
            .isEqualTo("uncategorisedDocuments");
    }

    @Test
    void shouldRejectUnknownFolder() {
        assertThatThrownBy(() -> ExceptionCaseFileCategory.require("unknown-folder"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown Case File View folder");
    }
}
