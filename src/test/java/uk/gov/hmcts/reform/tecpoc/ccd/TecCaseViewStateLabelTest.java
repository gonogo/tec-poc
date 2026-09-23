package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TecCaseViewStateLabelTest {

    @ParameterizedTest
    @EnumSource(CaseState.class)
    void stateLabelMatchesCcdAnnotation(CaseState state) throws Exception {
        String expected = CaseState.class.getField(state.name()).getAnnotation(
            uk.gov.hmcts.ccd.sdk.api.CCD.class
        ).label();

        assertThat(TecCaseView.stateLabel(state)).isEqualTo(expected);
    }

    @Test
    void stateLabelForCaseIssued() {
        assertThat(TecCaseView.stateLabel(CaseState.CASE_ISSUED)).isEqualTo("Case Issued");
    }
}
