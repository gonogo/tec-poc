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
import uk.gov.hmcts.reform.tecpoc.service.ExceptionCaseCreationService;

@RestController
@RequestMapping("/exception-cases")
@RequiredArgsConstructor
public class ExceptionCaseController {

    private final ExceptionCaseCreationService creationService;

    @PostMapping
    public ResponseEntity<CreateExceptionCaseResponse> create(
        @Valid @RequestBody CreateExceptionCaseRequest request,
        @RequestHeader("Authorization") String authorisation
    ) {
        return ResponseEntity.status(CREATED).body(creationService.create(request, authorisation));
    }
}
