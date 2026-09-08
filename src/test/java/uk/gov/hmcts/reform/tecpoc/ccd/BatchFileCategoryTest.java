package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BatchFileCategoryTest {

    @Test
    void shouldResolveByIdIgnoringCase() {
        assertThat(BatchFileCategory.resolve("inputs"))
            .contains(BatchFileCategory.INPUTS);
        assertThat(BatchFileCategory.resolve("OUTPUTS"))
            .contains(BatchFileCategory.OUTPUTS);
    }

    @Test
    void shouldResolveByLabelIgnoringCase() {
        assertThat(BatchFileCategory.resolve("Inputs"))
            .contains(BatchFileCategory.INPUTS);
        assertThat(BatchFileCategory.resolve("outputs"))
            .contains(BatchFileCategory.OUTPUTS);
    }

    @Test
    void shouldNormaliseBlankCategoryToInputs() {
        assertThat(BatchFileCategory.normalisedCategoryId(null))
            .isEqualTo("inputs");
        assertThat(BatchFileCategory.normalisedCategoryId("  "))
            .isEqualTo("inputs");
    }

    @Test
    void shouldRejectUnknownFolder() {
        assertThatThrownBy(() -> BatchFileCategory.require("unknown-folder"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown batch document category");
    }
}
