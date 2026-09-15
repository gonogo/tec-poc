package uk.gov.hmcts.reform.tecpoc.cftlib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.reform.tecpoc.ccd.TecJurisdiction;
import uk.gov.hmcts.reform.tecpoc.ccd.UserRole;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.BatchDatafileCaseConfiguration;
import uk.gov.hmcts.reform.tecpoc.ccd.batchdatafile.BatchDatafileCaseState;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.File;
import java.util.Map;

@Component
public class TecCftLibConfiguration implements CFTLibConfigurer {

    private static final String CASEWORKER_GENERIC_ROLE = "caseworker";
    private static final String ORGANISATION_MANAGER_ROLE = "pui-organisation-manager";
    // Provision the actor locally without adding undefined permissions to the CCD role enum.
    private static final String LA_MANAGER_ROLE = "caseworker-tec-la-manager";

    private static final String SYSTEM_USER = "tec-system@test.com";
    private static final Map<String, String> DEMO_USERS = Map.of(
        "tec-clerk@test.com", UserRole.CLERK.getRole(),
        "tec-manager@test.com", UserRole.TEC_MANAGER.getRole(),
        "la-user@test.com", UserRole.LA_USER.getRole(),
        "la-manager@test.com", LA_MANAGER_ROLE
    );

    @Autowired
    @Lazy
    private CCDDefinitionGenerator definitionGenerator;

    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(
            CASEWORKER_GENERIC_ROLE,
            UserRole.SYSTEM.getRole(),
            UserRole.CLERK.getRole(),
            UserRole.TEC_MANAGER.getRole(),
            UserRole.LA_USER.getRole(),
            LA_MANAGER_ROLE
        );

        lib.createIdamUser(
            SYSTEM_USER,
            UserRole.SYSTEM.getRole()
        );
        for (var demoUser : DEMO_USERS.entrySet()) {
            lib.createIdamUser(
                demoUser.getKey(),
                CASEWORKER_GENERIC_ROLE,
                ORGANISATION_MANAGER_ROLE,
                demoUser.getValue()
            );
        }

        definitionGenerator.generateAllCaseTypesToJSON(new File("build/ccd-definition"));
        lib.importJsonDefinition(new File("build/ccd-definition/" + BatchDatafileCaseConfiguration.CASE_TYPE));
        for (String email : DEMO_USERS.keySet()) {
            lib.createProfile(
                email,
                TecJurisdiction.ID,
                BatchDatafileCaseConfiguration.CASE_TYPE,
                BatchDatafileCaseState.AWAITING_PROCESSING.name()
            );
        }
    }
}
