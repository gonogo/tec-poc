package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;

@Component
public class BatchDatafileCaseViewProvider implements CaseView<BatchDatafileCase, BatchDatafileCaseState> {

    private final BatchDatafileCaseRepository repository;
    private final BatchDatafileCaseMapper mapper;

    public BatchDatafileCaseViewProvider(
        @Lazy BatchDatafileCaseRepository repository,
        BatchDatafileCaseMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BatchDatafileCase getCase(CaseViewRequest<BatchDatafileCaseState> request) {
        BatchDatafileCaseEntity entity = repository.findById(request.caseRef())
            .orElseThrow(() -> new IllegalStateException(
                "Batch datafile case not found: " + request.caseRef()
            ));

        BatchDatafileCase caseData = mapper.toCase(entity);
        caseData.setCaseState(request.state());
        return caseData;
    }
}
