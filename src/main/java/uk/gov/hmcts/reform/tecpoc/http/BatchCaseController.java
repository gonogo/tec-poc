package uk.gov.hmcts.reform.tecpoc.http;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.tecpoc.service.BatchCaseCreationService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchCaseController {

    private final BatchCaseCreationService creationService;

    @PostMapping
    public ResponseEntity<CreateBatchResponse> create(
        @Valid @RequestBody CreateBatchRequest request,
        @RequestHeader("Authorization") String authorisation
    ) {
        return ResponseEntity.status(CREATED).body(creationService.create(request, authorisation));
    }
}
