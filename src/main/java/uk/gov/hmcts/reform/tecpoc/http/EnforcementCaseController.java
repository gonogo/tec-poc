package uk.gov.hmcts.reform.tecpoc.http;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.tecpoc.service.EnforcementCaseCreationService;

@RestController
@RequestMapping("/enforcement-cases")
@RequiredArgsConstructor
public class EnforcementCaseController {

    private final EnforcementCaseCreationService creationService;

    @PostMapping
    public ResponseEntity<CreateEnforcementCaseResponse> create(
        @Valid @RequestBody CreateEnforcementCaseRequest request,
        @RequestHeader("Authorization") String authorisation
    ) {
        return ResponseEntity.status(CREATED).body(creationService.create(request, authorisation));
    }
}
