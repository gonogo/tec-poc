package uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.ccd.sdk.impl.IdamService;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.tecpoc.ccd.AccessProfile;
import uk.gov.hmcts.reform.tecpoc.ccd.TecJurisdiction;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;

/** Resolves LA ownership from the authenticated user's group-scoped role assignment. */
@Service
public class BatchDatafileOwnershipService {

    private static final String AUTHORIZATION = "Authorization";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final Set<String> TRUSTED_IDAM_ROLES = Set.of(
        AccessProfile.SYSTEM.getRole(),
        AccessProfile.CLERK.getRole(),
        AccessProfile.TEC_MANAGER.getRole()
    );

    private final HttpServletRequest request;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceTokenGenerator;
    private final RestClient roleAssignmentClient;

    public BatchDatafileOwnershipService(
        HttpServletRequest request,
        IdamService idamService,
        AuthTokenGenerator serviceTokenGenerator,
        RestClient.Builder restClientBuilder,
        @Value("${role-assignment-service.api.url:http://localhost:4096}") String roleAssignmentUrl
    ) {
        this.request = request;
        this.idamService = idamService;
        this.serviceTokenGenerator = serviceTokenGenerator;
        this.roleAssignmentClient = restClientBuilder.baseUrl(roleAssignmentUrl).build();
    }

    public OrganisationPolicy<UserRole> resolve(OrganisationPolicy<UserRole> suppliedPolicy) {
        String authorisation = request.getHeader(AUTHORIZATION);
        if (authorisation == null || authorisation.isBlank()) {
            throw new IllegalArgumentException("Authorization header is required to resolve case ownership");
        }

        var user = idamService.retrieveUser(authorisation).userDetails();
        if (user.getRoles().stream().anyMatch(TRUSTED_IDAM_ROLES::contains)) {
            return normalise(requireSuppliedOrganisation(suppliedPolicy));
        }

        RoleAssignmentResponse response = roleAssignmentClient.get()
            .uri("/am/role-assignments/actors/{actorId}", user.getUid())
            .header(AUTHORIZATION, authorisation)
            .header(SERVICE_AUTHORIZATION, serviceTokenGenerator.generate())
            .retrieve()
            .body(RoleAssignmentResponse.class);

        List<Organisation> organisations = response == null || response.assignments() == null
            ? List.of()
            : response.assignments().stream()
                .filter(this::isBatchReaderAssignment)
                .map(this::organisationFrom)
                .distinct()
                .toList();

        if (organisations.size() != 1) {
            throw new IllegalArgumentException(
                "Exactly one local authority reader assignment is required to create a batch case"
            );
        }

        Organisation organisation = organisations.getFirst();
        if (suppliedPolicy != null
            && suppliedPolicy.getOrganisation() != null
            && suppliedPolicy.getOrganisation().getOrganisationId() != null
            && !suppliedPolicy.getOrganisation().getOrganisationId().equals(organisation.getOrganisationId())) {
            throw new IllegalArgumentException("The supplied local authority does not match the authenticated user");
        }
        return normalise(organisation);
    }

    private boolean isBatchReaderAssignment(RoleAssignment assignment) {
        return UserRole.TEC_BATCH_READER.getRole().equals(assignment.roleName())
            && BatchDatafileCaseConfiguration.CASE_TYPE.equals(attribute(assignment, "caseType"))
            && TecJurisdiction.ID.equals(attribute(assignment, "jurisdiction"));
    }

    private Organisation organisationFrom(RoleAssignment assignment) {
        String organisationId = attribute(assignment, "organisation");
        if (organisationId == null || organisationId.isBlank()) {
            throw new IllegalArgumentException("Local authority reader assignment has no organisation");
        }
        String groupId = attribute(assignment, "caseAccessGroupId");
        if (!BatchDatafileAccessGroup.groupId(organisationId).equals(groupId)) {
            throw new IllegalArgumentException("Local authority reader assignment has an invalid access group");
        }
        return Organisation.builder().organisationId(organisationId).build();
    }

    private String attribute(RoleAssignment assignment, String name) {
        JsonNode value = assignment.attributes() == null ? null : assignment.attributes().get(name);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Organisation requireSuppliedOrganisation(OrganisationPolicy<UserRole> policy) {
        if (policy == null || policy.getOrganisation() == null
            || policy.getOrganisation().getOrganisationId() == null
            || policy.getOrganisation().getOrganisationId().isBlank()) {
            throw new IllegalArgumentException(
                "A trusted service or staff creator must supply the owning local authority"
            );
        }
        return policy.getOrganisation();
    }

    private OrganisationPolicy<UserRole> normalise(Organisation organisation) {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(organisation)
            .prepopulateToUsersOrganisation(YesOrNo.YES)
            .orgPolicyCaseAssignedRole(UserRole.TEC_BATCH_READER)
            .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RoleAssignmentResponse(
        @JsonProperty("roleAssignmentResponse") List<RoleAssignment> assignments
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RoleAssignment(
        String roleName,
        Map<String, JsonNode> attributes
    ) {
    }

}
