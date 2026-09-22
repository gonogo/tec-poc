package uk.gov.hmcts.reform.tecpoc.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

class TecCaseViewWarrantAuthorisationsTest {

    @Test
    void toWarrantAuthorisationsMapsPersistedRows() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        List<ListValue<WarrantAuthorisation>> values = TecCaseView.toWarrantAuthorisations(List.of(
            new TecCaseWarrantAuthorisation(
                id,
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2027, 9, 22),
                WarrantAuthorisationStatus.ACTIVE
            )
        ));

        assertThat(values).hasSize(1);
        assertThat(values.get(0).getId()).isEqualTo(id.toString());
        assertThat(values.get(0).getValue().getDateOfIssue()).isEqualTo(LocalDate.of(2026, 9, 22));
        assertThat(values.get(0).getValue().getDateOfExpiry()).isEqualTo(LocalDate.of(2027, 9, 22));
        assertThat(values.get(0).getValue().getStatus()).isEqualTo(WarrantAuthorisationStatus.ACTIVE);
    }
}
