package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds HTML that approximates ExUI's Work Allocation case Tasks tab for batches
 * ({@code exui-case-task} summary-list cards with Manage links), matching {@link TecPrototypeTasks}.
 */
final class BatchPrototypeTasks {

    private static final DateTimeFormatter DUE_DATE =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK);

    private static final String DEMO_USER = "tec-demo@test.com";

    private BatchPrototypeTasks() {
    }

    static String markdownFor(long caseReference, BatchCaseState state, BatchCase batchCase) {
        List<PrototypeTask> tasks = tasksFor(state, batchCase);
        if (tasks.isEmpty()) {
            return """
                <h2 class="govuk-heading-m">Active tasks</h2>
                <p class="govuk-body">There are no active tasks for this case.</p>
                """;
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2 class=\"govuk-heading-m\">Active tasks</h2>\n");
        for (PrototypeTask task : tasks) {
            html.append(renderTask(task));
        }
        return html.toString();
    }

    private static List<PrototypeTask> tasksFor(BatchCaseState state, BatchCase batchCase) {
        List<PrototypeTask> tasks = new ArrayList<>();

        if (state == BatchCaseState.QUEUED_FOR_PROCESSING) {
            tasks.add(new PrototypeTask(
                "Review queued batch",
                "Medium",
                LocalDate.now().plusDays(1),
                DEMO_USER,
                List.of("Reassign", "Unassign", "Go to task")
            ));
        }

        if (state == BatchCaseState.PROCESSING_STARTED) {
            tasks.add(new PrototypeTask(
                "Monitor batch processing",
                "High",
                LocalDate.now().plusDays(1),
                null,
                List.of("Assign to me")
            ));
        }

        if (state == BatchCaseState.PROCESSING_COMPLETE && batchCase.getPcnCount() != null) {
            tasks.add(new PrototypeTask(
                "Review batch outputs",
                "Low",
                LocalDate.now().plusDays(3),
                DEMO_USER,
                List.of("Reassign", "Unassign", "Go to task")
            ));
        }

        return tasks;
    }

    private static String renderTask(PrototypeTask task) {
        StringBuilder html = new StringBuilder();
        html.append("<hr class=\"govuk-section-break govuk-section-break--m govuk-section-break--visible\" />\n");
        html.append("<p class=\"govuk-body\"><strong>")
            .append(escape(task.title()))
            .append("</strong></p>\n");
        html.append("<dl class=\"govuk-summary-list govuk-summary-list--no-border\">\n");

        appendRow(html, "Priority", escape(task.priority()));
        appendRow(html, "Due date", escape(task.dueDate().format(DUE_DATE)));
        appendRow(html, "Assigned to", escape(task.assignee() == null ? "Unassigned" : task.assignee()));

        if (!task.manageActions().isEmpty()) {
            appendRow(html, "Manage", renderManageLinks(task.manageActions()));
        }

        html.append("</dl>\n");
        return html.toString();
    }

    private static void appendRow(StringBuilder html, String key, String valueHtml) {
        html.append("<div class=\"govuk-summary-list__row\">")
            .append("<dt class=\"govuk-summary-list__key\">")
            .append(escape(key))
            .append("</dt>")
            .append("<dd class=\"govuk-summary-list__value\">")
            .append(valueHtml)
            .append("</dd>")
            .append("</div>\n");
    }

    private static String renderManageLinks(List<String> actions) {
        StringBuilder links = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) {
                links.append("&nbsp;&nbsp;");
            }
            links.append("<a href=\"#\" class=\"govuk-link\">")
                .append(escape(actions.get(i)))
                .append("</a>");
        }
        return links.toString();
    }

    private static String escape(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private record PrototypeTask(
        String title,
        String priority,
        LocalDate dueDate,
        String assignee,
        List<String> manageActions
    ) {
    }
}
