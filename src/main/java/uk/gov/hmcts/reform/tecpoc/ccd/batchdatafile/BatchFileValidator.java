package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;

@Component
public class BatchFileValidator {

    public List<String> validate(Document batchFile, BatchType batchType) {
        if (batchFile == null) {
            return List.of("Batch file is required");
        }
        // Type-specific business validation is not yet defined in docs/state-event-model.md.
        return List.of();
    }
}
