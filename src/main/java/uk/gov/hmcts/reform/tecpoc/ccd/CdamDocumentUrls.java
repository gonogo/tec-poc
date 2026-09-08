package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExUI media viewer / download proxies through Case Document AM ({@code /cases/documents/...}).
 * Local uploads often return raw dm-store links ({@code /documents/...}); rewrite those so
 * Manage Case can open and download files.
 */
final class CdamDocumentUrls {

    private static final Pattern DOCUMENT_ID = Pattern.compile(
        ".*/documents/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"
    );

    private CdamDocumentUrls() {
    }

    static String cdamBaseUrl() {
        String configured = System.getenv("CASE_DOCUMENT_AM_URL");
        if (configured == null || configured.isBlank()) {
            return "http://localhost:4455";
        }
        return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    }

    static String toCdamUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (url.contains("/cases/documents/")) {
            return url;
        }
        return documentId(url)
            .map(id -> {
                String base = cdamBaseUrl() + "/cases/documents/" + id;
                return url.contains("/binary") ? base + "/binary" : base;
            })
            .orElse(url);
    }

    static Optional<String> documentId(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = DOCUMENT_ID.matcher(url);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(matcher.group(1));
    }
}
