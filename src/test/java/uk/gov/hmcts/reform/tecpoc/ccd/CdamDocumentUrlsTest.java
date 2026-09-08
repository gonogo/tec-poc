package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CdamDocumentUrlsTest {

    @Test
    void shouldRewriteDmStoreUrlsToCdam() {
        String documentId = "b37a6915-2f31-44d8-8c76-d491929a5254";

        assertThat(CdamDocumentUrls.toCdamUrl("http://localhost:4506/documents/" + documentId))
            .isEqualTo("http://localhost:4455/cases/documents/" + documentId);
        assertThat(CdamDocumentUrls.toCdamUrl(
                "http://localhost:4506/documents/" + documentId + "/binary"
            ))
            .isEqualTo("http://localhost:4455/cases/documents/" + documentId + "/binary");
    }

    @Test
    void shouldLeaveCdamUrlsUnchanged() {
        String url = "http://localhost:4455/cases/documents/b37a6915-2f31-44d8-8c76-d491929a5254";
        String binary = url + "/binary";

        assertThat(CdamDocumentUrls.toCdamUrl(url)).isEqualTo(url);
        assertThat(CdamDocumentUrls.toCdamUrl(binary)).isEqualTo(binary);
    }

    @Test
    void shouldPassThroughBlankOrUnrecognisedUrls() {
        assertThat(CdamDocumentUrls.toCdamUrl(null)).isNull();
        assertThat(CdamDocumentUrls.toCdamUrl("  ")).isEqualTo("  ");
        assertThat(CdamDocumentUrls.toCdamUrl("http://example.com/file.xlsx"))
            .isEqualTo("http://example.com/file.xlsx");
    }
}
