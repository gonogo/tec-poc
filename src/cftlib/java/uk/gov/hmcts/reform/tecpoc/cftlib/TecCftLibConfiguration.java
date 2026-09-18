package uk.gov.hmcts.reform.tecpoc.cftlib;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.reform.tecpoc.ccd.AccessProfile;
import uk.gov.hmcts.reform.tecpoc.ccd.TecJurisdiction;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.BatchDatafileCaseConfiguration;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.BatchDatafileCaseState;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class TecCftLibConfiguration implements CFTLibConfigurer {

    private static final String CASEWORKER_GENERIC_ROLE = "caseworker";
    // Required by CCD's ExUI metadata endpoints for paths under jurisdiction TEC.
    // This role is routing/bootstrap metadata only; CCD business permissions use distinct profiles.
    private static final String CASEWORKER_JURISDICTION_ROLE = "caseworker-tec";
    private static final String ORGANISATION_MANAGER_ROLE = "pui-organisation-manager";
    // Provision the actor locally without adding undefined permissions to the CCD role enum.
    private static final String LA_MANAGER_ROLE = "caseworker-tec-la-manager";

    private static final String SYSTEM_USER = "tec-system@test.com";
    // ExUI's current Create Case component re-filters CCD's authorised events by matching event ACLs against
    // IDAM session roles. It cannot see access profiles derived from RAS, so local users with the
    // tec-batch-submitter assignment also need this IDAM-only shadow role. RAS remains authoritative for CCD;
    // keep this list aligned with cftlib-am-role-assignments.json and RoleToAccessProfiles.
    private static final List<String> LA_BATCH_SUBMITTER_EXUI_ROLES = List.of(
        AccessProfile.TEC_BATCH_CREATE.getRole()
    );
    // The local IDAM simulator uses UUID.nameUUIDFromBytes(email) as the actor ID.
    private static final Map<String, List<String>> DEMO_USERS = Map.of(
        "tec-clerk@test.com", List.of(AccessProfile.CLERK.getRole()),
        "tec-manager@test.com", List.of(AccessProfile.TEC_MANAGER.getRole()),
        "la-user@test.com", LA_BATCH_SUBMITTER_EXUI_ROLES,
        "la-colleague@test.com", LA_BATCH_SUBMITTER_EXUI_ROLES,
        "la-b-user@test.com", LA_BATCH_SUBMITTER_EXUI_ROLES,
        "la-manager@test.com", List.of(ORGANISATION_MANAGER_ROLE, LA_MANAGER_ROLE)
    );

    @Autowired
    @Lazy
    private CCDDefinitionGenerator definitionGenerator;

    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(
            CASEWORKER_GENERIC_ROLE,
            CASEWORKER_JURISDICTION_ROLE,
            AccessProfile.SYSTEM.getRole(),
            AccessProfile.CLERK.getRole(),
            AccessProfile.TEC_MANAGER.getRole(),
            AccessProfile.TEC_BATCH_CREATE.getRole(),
            AccessProfile.TEC_BATCH_READ.getRole(),
            UserRole.TEC_BATCH_SUBMITTER.getRole(),
            UserRole.TEC_BATCH_READER.getRole(),
            LA_MANAGER_ROLE
        );

        lib.createIdamUser(
            SYSTEM_USER,
            AccessProfile.SYSTEM.getRole()
        );
        for (var demoUser : DEMO_USERS.entrySet()) {
            List<String> roles = demoUser.getValue();
            String[] idamRoles = new String[roles.size() + 2];
            idamRoles[0] = CASEWORKER_GENERIC_ROLE;
            idamRoles[1] = CASEWORKER_JURISDICTION_ROLE;
            for (int index = 0; index < roles.size(); index++) {
                idamRoles[index + 2] = roles.get(index);
            }
            lib.createIdamUser(demoUser.getKey(), idamRoles);
        }
        lib.configureRoleAssignments(readResource("cftlib-am-role-assignments.json"));

        definitionGenerator.generateAllCaseTypesToJSON(new File("build/ccd-definition"));
        lib.importJsonDefinition(new File("build/ccd-definition/" + BatchDatafileCaseConfiguration.CASE_TYPE));
        // The decentralised runtime uses this snapshot to publish case events for Elasticsearch indexing.
        // Importing the definition alone is insufficient: without the snapshot, direct reads work but ExUI
        // case-list searches cannot find newly created cases.
        lib.dumpDefinitionSnapshots();
        for (String email : DEMO_USERS.keySet()) {
            lib.createProfile(
                email,
                TecJurisdiction.ID,
                BatchDatafileCaseConfiguration.CASE_TYPE,
                BatchDatafileCaseState.AWAITING_PROCESSING.name()
            );
        }
    }

    private String readResource(String name) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalStateException("CFTLib resource not found: " + name);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
