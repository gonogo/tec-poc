package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BatchPrototypeTasksTest {

    @Test
    void shouldShowQueuedTask() {
        BatchCase batchCase = new BatchCase();
        batchCase.setPcnCount(500);
        String html = BatchPrototypeTasks.markdownFor(
            1234L,
            BatchCaseState.QUEUED_FOR_PROCESSING,
            batchCase
        );
        assertThat(html).contains("Review queued batch");
        assertThat(html).contains("Active tasks");
        assertThat(html).contains("govuk-summary-list__key\">Manage</dt>");
        assertThat(html).contains("class=\"govuk-link\">Reassign</a>");
        assertThat(html).doesNotContain("<ul class=\"govuk-list\">");
    }

    @Test
    void shouldShowCompleteTask() {
        BatchCase batchCase = new BatchCase();
        batchCase.setPcnCount(500);
        String html = BatchPrototypeTasks.markdownFor(
            1234L,
            BatchCaseState.PROCESSING_COMPLETE,
            batchCase
        );
        assertThat(html).contains("Review batch outputs");
    }

    @Test
    void shouldShowFailedTask() {
        BatchCase batchCase = new BatchCase();
        batchCase.setPcnCount(500);
        String html = BatchPrototypeTasks.markdownFor(
            1234L,
            BatchCaseState.PROCESSING_FAILED,
            batchCase
        );
        assertThat(html).contains("Investigate processing failure");
    }
}
