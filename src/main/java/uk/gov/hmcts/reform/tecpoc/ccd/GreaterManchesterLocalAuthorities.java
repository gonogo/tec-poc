package uk.gov.hmcts.reform.tecpoc.ccd;

import java.util.List;

/**
 * Greater Manchester local authorities used to scope the demo LA user via CaseAccessCategories.
 */
public final class GreaterManchesterLocalAuthorities {

    private static final List<LocalAuthority> AUTHORITIES = List.of(
        LocalAuthority.BOLTON_BOROUGH_COUNCIL,
        LocalAuthority.BURY_BOROUGH_COUNCIL,
        LocalAuthority.MANCHESTER_CITY_COUNCIL,
        LocalAuthority.OLDHAM_BOROUGH_COUNCIL,
        LocalAuthority.ROCHDALE_BOROUGH_COUNCIL,
        LocalAuthority.SALFORD_CITY_COUNCIL,
        LocalAuthority.STOCKPORT_BOROUGH_COUNCIL,
        LocalAuthority.TAMESIDE_BOROUGH_COUNCIL,
        LocalAuthority.TRAFFORD_BOROUGH_COUNCIL,
        LocalAuthority.WIGAN_BOROUGH_COUNCIL
    );

    private GreaterManchesterLocalAuthorities() {
    }

    public static List<LocalAuthority> authorities() {
        return AUTHORITIES;
    }

    /** FixedList / CaseAccessCategory codes ({@code @JsonProperty} values). */
    public static String[] accessCategoryCodes() {
        return AUTHORITIES.stream()
            .map(LocalAuthority::getCode)
            .toArray(String[]::new);
    }

    public static boolean contains(LocalAuthority localAuthority) {
        return AUTHORITIES.contains(localAuthority);
    }
}
