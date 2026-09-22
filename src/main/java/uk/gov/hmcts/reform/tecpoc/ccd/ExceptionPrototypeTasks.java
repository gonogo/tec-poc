package uk.gov.hmcts.reform.tecpoc.ccd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Builds HTML that approximates ExUI's Work Allocation case Tasks tab for exception cases.
 */
final class ExceptionPrototypeTasks {

    private static final DateTimeFormatter DUE_DATE =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK);

    private static final String DEMO_USER = "tec-demo@test.com";

    private ExceptionPrototypeTasks() {
    }

    static String markdownFor(long caseReference, ExceptionCaseState state) {
        if (state != ExceptionCaseState.EXCEPTION_PENDING_REVIEW) {
            return """
                <h2 class="govuk-heading-m">Active tasks</h2>
                <p class="govuk-body">There are no active tasks for this case.</p>
                """;
        }

        return "<h2 class=\"govuk-heading-m\">Active tasks</h2>\n"
            + renderTask(
                caseReference,
                new PrototypeTask(
                    "Review exception item",
                    "High",
                    LocalDate.now().plusDays(2),
                    DEMO_USER,
                    List.of("Reassign", "Unassign", "Go to task"),
                    List.of(
                        new NextStep("Reject item", "rejectItem"),
                        new NextStep("Edit PCN", "editPcn")
                    )
                )
            );
    }

    private static String renderTask(long caseReference, PrototypeTask task) {
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

        if (task.assignee() != null && task.assignee().equals(DEMO_USER) && !task.nextSteps().isEmpty()) {
            appendRow(html, "Next steps", renderNextSteps(caseReference, task.nextSteps()));
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

    private static String renderNextSteps(long caseReference, List<NextStep> nextSteps) {
        StringBuilder links = new StringBuilder();
        for (int i = 0; i < nextSteps.size(); i++) {
            NextStep step = nextSteps.get(i);
            if (i > 0) {
                links.append("<br />");
            }
            String href = step.eventId() == null
                ? "#"
                : "/cases/case-details/" + caseReference + "/trigger/" + step.eventId();
            links.append("<a href=\"")
                .append(href)
                .append("\" class=\"govuk-link\">")
                .append(escape(step.label()))
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

    private record NextStep(String label, String eventId) {
    }

    private record PrototypeTask(
        String title,
        String priority,
        LocalDate dueDate,
        String assignee,
        List<String> manageActions,
        List<NextStep> nextSteps
    ) {
    }
}
