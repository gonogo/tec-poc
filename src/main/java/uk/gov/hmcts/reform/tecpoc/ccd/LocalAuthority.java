package uk.gov.hmcts.reform.tecpoc.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * English local authorities from the 2023 list of councils in England (317).
 */
public enum LocalAuthority implements HasLabel {

    @JsonProperty("barnsleyBoroughCouncil")
    BARNSLEY_BOROUGH_COUNCIL("Barnsley Borough Council"),

    @JsonProperty("birminghamCityCouncil")
    BIRMINGHAM_CITY_COUNCIL("Birmingham City Council"),

    @JsonProperty("boltonBoroughCouncil")
    BOLTON_BOROUGH_COUNCIL("Bolton Borough Council"),

    @JsonProperty("bradfordCityCouncil")
    BRADFORD_CITY_COUNCIL("Bradford City Council"),

    @JsonProperty("buryBoroughCouncil")
    BURY_BOROUGH_COUNCIL("Bury Borough Council"),

    @JsonProperty("calderdaleBoroughCouncil")
    CALDERDALE_BOROUGH_COUNCIL("Calderdale Borough Council"),

    @JsonProperty("coventryCityCouncil")
    COVENTRY_CITY_COUNCIL("Coventry City Council"),

    @JsonProperty("doncasterBoroughCouncil")
    DONCASTER_BOROUGH_COUNCIL("Doncaster Borough Council"),

    @JsonProperty("dudleyBoroughCouncil")
    DUDLEY_BOROUGH_COUNCIL("Dudley Borough Council"),

    @JsonProperty("gatesheadBoroughCouncil")
    GATESHEAD_BOROUGH_COUNCIL("Gateshead Borough Council"),

    @JsonProperty("kirkleesBoroughCouncil")
    KIRKLEES_BOROUGH_COUNCIL("Kirklees Borough Council"),

    @JsonProperty("knowsleyBoroughCouncil")
    KNOWSLEY_BOROUGH_COUNCIL("Knowsley Borough Council"),

    @JsonProperty("leedsCityCouncil")
    LEEDS_CITY_COUNCIL("Leeds City Council"),

    @JsonProperty("liverpoolCityCouncil")
    LIVERPOOL_CITY_COUNCIL("Liverpool City Council"),

    @JsonProperty("manchesterCityCouncil")
    MANCHESTER_CITY_COUNCIL("Manchester City Council"),

    @JsonProperty("northTynesideBoroughCouncil")
    NORTH_TYNESIDE_BOROUGH_COUNCIL("North Tyneside Borough Council"),

    @JsonProperty("newcastleUponTyneCityCouncil")
    NEWCASTLE_UPON_TYNE_CITY_COUNCIL("Newcastle Upon Tyne City Council"),

    @JsonProperty("oldhamBoroughCouncil")
    OLDHAM_BOROUGH_COUNCIL("Oldham Borough Council"),

    @JsonProperty("rochdaleBoroughCouncil")
    ROCHDALE_BOROUGH_COUNCIL("Rochdale Borough Council"),

    @JsonProperty("rotherhamBoroughCouncil")
    ROTHERHAM_BOROUGH_COUNCIL("Rotherham Borough Council"),

    @JsonProperty("southTynesideBoroughCouncil")
    SOUTH_TYNESIDE_BOROUGH_COUNCIL("South Tyneside Borough Council"),

    @JsonProperty("salfordCityCouncil")
    SALFORD_CITY_COUNCIL("Salford City Council"),

    @JsonProperty("sandwellBoroughCouncil")
    SANDWELL_BOROUGH_COUNCIL("Sandwell Borough Council"),

    @JsonProperty("seftonBoroughCouncil")
    SEFTON_BOROUGH_COUNCIL("Sefton Borough Council"),

    @JsonProperty("sheffieldCityCouncil")
    SHEFFIELD_CITY_COUNCIL("Sheffield City Council"),

    @JsonProperty("solihullBoroughCouncil")
    SOLIHULL_BOROUGH_COUNCIL("Solihull Borough Council"),

    @JsonProperty("stHelensBoroughCouncil")
    ST_HELENS_BOROUGH_COUNCIL("St Helens Borough Council"),

    @JsonProperty("stockportBoroughCouncil")
    STOCKPORT_BOROUGH_COUNCIL("Stockport Borough Council"),

    @JsonProperty("sunderlandCityCouncil")
    SUNDERLAND_CITY_COUNCIL("Sunderland City Council"),

    @JsonProperty("tamesideBoroughCouncil")
    TAMESIDE_BOROUGH_COUNCIL("Tameside Borough Council"),

    @JsonProperty("traffordBoroughCouncil")
    TRAFFORD_BOROUGH_COUNCIL("Trafford Borough Council"),

    @JsonProperty("wakefieldCityCouncil")
    WAKEFIELD_CITY_COUNCIL("Wakefield City Council"),

    @JsonProperty("walsallBoroughCouncil")
    WALSALL_BOROUGH_COUNCIL("Walsall Borough Council"),

    @JsonProperty("wiganBoroughCouncil")
    WIGAN_BOROUGH_COUNCIL("Wigan Borough Council"),

    @JsonProperty("wirralBoroughCouncil")
    WIRRAL_BOROUGH_COUNCIL("Wirral Borough Council"),

    @JsonProperty("wolverhamptonCityCouncil")
    WOLVERHAMPTON_CITY_COUNCIL("Wolverhampton City Council"),

    @JsonProperty("barkingAndDagenham")
    BARKING_AND_DAGENHAM("Barking and Dagenham"),

    @JsonProperty("barnet")
    BARNET("Barnet"),

    @JsonProperty("bexley")
    BEXLEY("Bexley"),

    @JsonProperty("brent")
    BRENT("Brent"),

    @JsonProperty("bromley")
    BROMLEY("Bromley"),

    @JsonProperty("camden")
    CAMDEN("Camden"),

    @JsonProperty("croydon")
    CROYDON("Croydon"),

    @JsonProperty("ealing")
    EALING("Ealing"),

    @JsonProperty("enfield")
    ENFIELD("Enfield"),

    @JsonProperty("greenwich")
    GREENWICH("Greenwich"),

    @JsonProperty("hackney")
    HACKNEY("Hackney"),

    @JsonProperty("hammersmithAndFulham")
    HAMMERSMITH_AND_FULHAM("Hammersmith and Fulham"),

    @JsonProperty("haringey")
    HARINGEY("Haringey"),

    @JsonProperty("harrow")
    HARROW("Harrow"),

    @JsonProperty("havering")
    HAVERING("Havering"),

    @JsonProperty("hillingdon")
    HILLINGDON("Hillingdon"),

    @JsonProperty("hounslow")
    HOUNSLOW("Hounslow"),

    @JsonProperty("islington")
    ISLINGTON("Islington"),

    @JsonProperty("kensingtonAndChelsea")
    KENSINGTON_AND_CHELSEA("Kensington and Chelsea"),

    @JsonProperty("kingstonUponThames")
    KINGSTON_UPON_THAMES("Kingston upon Thames"),

    @JsonProperty("lambeth")
    LAMBETH("Lambeth"),

    @JsonProperty("lewisham")
    LEWISHAM("Lewisham"),

    @JsonProperty("merton")
    MERTON("Merton"),

    @JsonProperty("newham")
    NEWHAM("Newham"),

    @JsonProperty("redbridge")
    REDBRIDGE("Redbridge"),

    @JsonProperty("richmondUponThames")
    RICHMOND_UPON_THAMES("Richmond upon Thames"),

    @JsonProperty("southwark")
    SOUTHWARK("Southwark"),

    @JsonProperty("sutton")
    SUTTON("Sutton"),

    @JsonProperty("towerHamlets")
    TOWER_HAMLETS("Tower Hamlets"),

    @JsonProperty("walthamForest")
    WALTHAM_FOREST("Waltham Forest"),

    @JsonProperty("wandsworth")
    WANDSWORTH("Wandsworth"),

    @JsonProperty("westminster")
    WESTMINSTER("Westminster"),

    @JsonProperty("cityOfLondon")
    CITY_OF_LONDON("City of London"),

    @JsonProperty("bathAndNorthEastSomersetCouncil")
    BATH_AND_NORTH_EAST_SOMERSET_COUNCIL("Bath and North East Somerset Council"),

    @JsonProperty("bedfordBoroughCouncil")
    BEDFORD_BOROUGH_COUNCIL("Bedford Borough Council"),

    @JsonProperty("blackburnWithDarwenBoroughCouncil")
    BLACKBURN_WITH_DARWEN_BOROUGH_COUNCIL("Blackburn with Darwen Borough Council"),

    @JsonProperty("blackpoolCouncil")
    BLACKPOOL_COUNCIL("Blackpool Council"),

    @JsonProperty("bournemouthChristchurchAndPooleCouncil")
    BOURNEMOUTH_CHRISTCHURCH_AND_POOLE_COUNCIL("Bournemouth, Christchurch and Poole Council"),

    @JsonProperty("bracknellForestBoroughCouncil")
    BRACKNELL_FOREST_BOROUGH_COUNCIL("Bracknell Forest Borough Council"),

    @JsonProperty("brightonAndHoveCityCouncil")
    BRIGHTON_AND_HOVE_CITY_COUNCIL("Brighton and Hove City Council"),

    @JsonProperty("bristolCityCouncil")
    BRISTOL_CITY_COUNCIL("Bristol City Council"),

    @JsonProperty("buckinghamshireCouncil")
    BUCKINGHAMSHIRE_COUNCIL("Buckinghamshire Council"),

    @JsonProperty("centralBedfordshireCouncil")
    CENTRAL_BEDFORDSHIRE_COUNCIL("Central Bedfordshire Council"),

    @JsonProperty("cheshireEastCouncil")
    CHESHIRE_EAST_COUNCIL("Cheshire East Council"),

    @JsonProperty("cheshireWestAndChesterCouncil")
    CHESHIRE_WEST_AND_CHESTER_COUNCIL("Cheshire West and Chester Council"),

    @JsonProperty("cornwallCouncil")
    CORNWALL_COUNCIL("Cornwall Council"),

    @JsonProperty("cumberlandCouncil")
    CUMBERLAND_COUNCIL("Cumberland Council"),

    @JsonProperty("durhamCountyCouncil")
    DURHAM_COUNTY_COUNCIL("Durham County Council"),

    @JsonProperty("darlingtonBoroughCouncil")
    DARLINGTON_BOROUGH_COUNCIL("Darlington Borough Council"),

    @JsonProperty("derbyCityCouncil")
    DERBY_CITY_COUNCIL("Derby City Council"),

    @JsonProperty("dorsetCouncil")
    DORSET_COUNCIL("Dorset Council"),

    @JsonProperty("eastRidingOfYorkshireCouncil")
    EAST_RIDING_OF_YORKSHIRE_COUNCIL("East Riding of Yorkshire Council"),

    @JsonProperty("haltonBoroughCouncil")
    HALTON_BOROUGH_COUNCIL("Halton Borough Council"),

    @JsonProperty("hartlepoolBoroughCouncil")
    HARTLEPOOL_BOROUGH_COUNCIL("Hartlepool Borough Council"),

    @JsonProperty("herefordshireCouncil")
    HEREFORDSHIRE_COUNCIL("Herefordshire Council"),

    @JsonProperty("isleOfWightCouncil")
    ISLE_OF_WIGHT_COUNCIL("Isle of Wight Council"),

    @JsonProperty("hullCityCouncil")
    HULL_CITY_COUNCIL("Hull City Council"),

    @JsonProperty("leicesterCityCouncil")
    LEICESTER_CITY_COUNCIL("Leicester City Council"),

    @JsonProperty("lutonBoroughCouncil")
    LUTON_BOROUGH_COUNCIL("Luton Borough Council"),

    @JsonProperty("medwayCouncil")
    MEDWAY_COUNCIL("Medway Council"),

    @JsonProperty("middlesbroughBoroughCouncil")
    MIDDLESBROUGH_BOROUGH_COUNCIL("Middlesbrough Borough Council"),

    @JsonProperty("miltonKeynesCouncil")
    MILTON_KEYNES_COUNCIL("Milton Keynes Council"),

    @JsonProperty("northEastLincolnshireCouncil")
    NORTH_EAST_LINCOLNSHIRE_COUNCIL("North East Lincolnshire Council"),

    @JsonProperty("northLincolnshireCouncil")
    NORTH_LINCOLNSHIRE_COUNCIL("North Lincolnshire Council"),

    @JsonProperty("northNorthamptonshireCouncil")
    NORTH_NORTHAMPTONSHIRE_COUNCIL("North Northamptonshire Council"),

    @JsonProperty("northSomersetCouncil")
    NORTH_SOMERSET_COUNCIL("North Somerset Council"),

    @JsonProperty("northYorkshireCouncil")
    NORTH_YORKSHIRE_COUNCIL("North Yorkshire Council"),

    @JsonProperty("northumberlandCountyCouncil")
    NORTHUMBERLAND_COUNTY_COUNCIL("Northumberland County Council"),

    @JsonProperty("nottinghamCityCouncil")
    NOTTINGHAM_CITY_COUNCIL("Nottingham City Council"),

    @JsonProperty("peterboroughCityCouncil")
    PETERBOROUGH_CITY_COUNCIL("Peterborough City Council"),

    @JsonProperty("plymouthCityCouncil")
    PLYMOUTH_CITY_COUNCIL("Plymouth City Council"),

    @JsonProperty("portsmouthCityCouncil")
    PORTSMOUTH_CITY_COUNCIL("Portsmouth City Council"),

    @JsonProperty("readingBoroughCouncil")
    READING_BOROUGH_COUNCIL("Reading Borough Council"),

    @JsonProperty("redcarAndClevelandBoroughCouncil")
    REDCAR_AND_CLEVELAND_BOROUGH_COUNCIL("Redcar and Cleveland Borough Council"),

    @JsonProperty("rutlandCountyCouncil")
    RUTLAND_COUNTY_COUNCIL("Rutland County Council"),

    @JsonProperty("shropshireCouncil")
    SHROPSHIRE_COUNCIL("Shropshire Council"),

    @JsonProperty("sloughBoroughCouncil")
    SLOUGH_BOROUGH_COUNCIL("Slough Borough Council"),

    @JsonProperty("somersetCouncil")
    SOMERSET_COUNCIL("Somerset Council"),

    @JsonProperty("southamptonCityCouncil")
    SOUTHAMPTON_CITY_COUNCIL("Southampton City Council"),

    @JsonProperty("southendOnSeaBoroughCouncil")
    SOUTHEND_ON_SEA_BOROUGH_COUNCIL("Southend-on-Sea Borough Council"),

    @JsonProperty("southGloucestershireCouncil")
    SOUTH_GLOUCESTERSHIRE_COUNCIL("South Gloucestershire Council"),

    @JsonProperty("stocktonOnTeesBoroughCouncil")
    STOCKTON_ON_TEES_BOROUGH_COUNCIL("Stockton-on-Tees Borough Council"),

    @JsonProperty("stokeOnTrentCityCouncil")
    STOKE_ON_TRENT_CITY_COUNCIL("Stoke-on-Trent City Council"),

    @JsonProperty("swindonBoroughCouncil")
    SWINDON_BOROUGH_COUNCIL("Swindon Borough Council"),

    @JsonProperty("telfordAndWrekinBoroughCouncil")
    TELFORD_AND_WREKIN_BOROUGH_COUNCIL("Telford and Wrekin Borough Council"),

    @JsonProperty("thurrockCouncil")
    THURROCK_COUNCIL("Thurrock Council"),

    @JsonProperty("torbayCouncil")
    TORBAY_COUNCIL("Torbay Council"),

    @JsonProperty("warringtonBoroughCouncil")
    WARRINGTON_BOROUGH_COUNCIL("Warrington Borough Council"),

    @JsonProperty("westmorlandAndFurnessCouncil")
    WESTMORLAND_AND_FURNESS_COUNCIL("Westmorland and Furness Council"),

    @JsonProperty("westBerkshireCouncil")
    WEST_BERKSHIRE_COUNCIL("West Berkshire Council"),

    @JsonProperty("westNorthamptonshireCouncil")
    WEST_NORTHAMPTONSHIRE_COUNCIL("West Northamptonshire Council"),

    @JsonProperty("wiltshireCouncil")
    WILTSHIRE_COUNCIL("Wiltshire Council"),

    @JsonProperty("windsorAndMaidenheadBoroughCouncil")
    WINDSOR_AND_MAIDENHEAD_BOROUGH_COUNCIL("Windsor and Maidenhead Borough Council"),

    @JsonProperty("wokinghamBoroughCouncil")
    WOKINGHAM_BOROUGH_COUNCIL("Wokingham Borough Council"),

    @JsonProperty("cityOfYorkCouncil")
    CITY_OF_YORK_COUNCIL("City of York Council"),

    @JsonProperty("councilOfTheIslesOfScilly")
    COUNCIL_OF_THE_ISLES_OF_SCILLY("Council of the Isles of Scilly"),

    @JsonProperty("cambridgeshireCountyCouncil")
    CAMBRIDGESHIRE_COUNTY_COUNCIL("Cambridgeshire County Council"),

    @JsonProperty("derbyshireCountyCouncil")
    DERBYSHIRE_COUNTY_COUNCIL("Derbyshire County Council"),

    @JsonProperty("devonCountyCouncil")
    DEVON_COUNTY_COUNCIL("Devon County Council"),

    @JsonProperty("eastSussexCountyCouncil")
    EAST_SUSSEX_COUNTY_COUNCIL("East Sussex County Council"),

    @JsonProperty("essexCountyCouncil")
    ESSEX_COUNTY_COUNCIL("Essex County Council"),

    @JsonProperty("gloucestershireCountyCouncil")
    GLOUCESTERSHIRE_COUNTY_COUNCIL("Gloucestershire County Council"),

    @JsonProperty("hampshireCountyCouncil")
    HAMPSHIRE_COUNTY_COUNCIL("Hampshire County Council"),

    @JsonProperty("hertfordshireCountyCouncil")
    HERTFORDSHIRE_COUNTY_COUNCIL("Hertfordshire County Council"),

    @JsonProperty("kentCountyCouncil")
    KENT_COUNTY_COUNCIL("Kent County Council"),

    @JsonProperty("lancashireCountyCouncil")
    LANCASHIRE_COUNTY_COUNCIL("Lancashire County Council"),

    @JsonProperty("leicestershireCountyCouncil")
    LEICESTERSHIRE_COUNTY_COUNCIL("Leicestershire County Council"),

    @JsonProperty("lincolnshireCountyCouncil")
    LINCOLNSHIRE_COUNTY_COUNCIL("Lincolnshire County Council"),

    @JsonProperty("norfolkCountyCouncil")
    NORFOLK_COUNTY_COUNCIL("Norfolk County Council"),

    @JsonProperty("nottinghamshireCountyCouncil")
    NOTTINGHAMSHIRE_COUNTY_COUNCIL("Nottinghamshire County Council"),

    @JsonProperty("oxfordshireCountyCouncil")
    OXFORDSHIRE_COUNTY_COUNCIL("Oxfordshire County Council"),

    @JsonProperty("staffordshireCountyCouncil")
    STAFFORDSHIRE_COUNTY_COUNCIL("Staffordshire County Council"),

    @JsonProperty("suffolkCountyCouncil")
    SUFFOLK_COUNTY_COUNCIL("Suffolk County Council"),

    @JsonProperty("surreyCountyCouncil")
    SURREY_COUNTY_COUNCIL("Surrey County Council"),

    @JsonProperty("warwickshireCountyCouncil")
    WARWICKSHIRE_COUNTY_COUNCIL("Warwickshire County Council"),

    @JsonProperty("westSussexCountyCouncil")
    WEST_SUSSEX_COUNTY_COUNCIL("West Sussex County Council"),

    @JsonProperty("worcestershireCountyCouncil")
    WORCESTERSHIRE_COUNTY_COUNCIL("Worcestershire County Council"),

    @JsonProperty("adurDistrictCouncil")
    ADUR_DISTRICT_COUNCIL("Adur District Council"),

    @JsonProperty("amberValleyBoroughCouncil")
    AMBER_VALLEY_BOROUGH_COUNCIL("Amber Valley Borough Council"),

    @JsonProperty("arunDistrictCouncil")
    ARUN_DISTRICT_COUNCIL("Arun District Council"),

    @JsonProperty("ashfieldDistrictCouncil")
    ASHFIELD_DISTRICT_COUNCIL("Ashfield District Council"),

    @JsonProperty("ashfordBoroughCouncil")
    ASHFORD_BOROUGH_COUNCIL("Ashford Borough Council"),

    @JsonProperty("baberghDistrictCouncil")
    BABERGH_DISTRICT_COUNCIL("Babergh District Council"),

    @JsonProperty("basildonBoroughCouncil")
    BASILDON_BOROUGH_COUNCIL("Basildon Borough Council"),

    @JsonProperty("basingstokeAndDeaneBoroughCouncil")
    BASINGSTOKE_AND_DEANE_BOROUGH_COUNCIL("Basingstoke & Deane Borough Council"),

    @JsonProperty("bassetlawDistrictCouncil")
    BASSETLAW_DISTRICT_COUNCIL("Bassetlaw District Council"),

    @JsonProperty("blabyDistrictCouncil")
    BLABY_DISTRICT_COUNCIL("Blaby District Council"),

    @JsonProperty("bolsoverDistrictCouncil")
    BOLSOVER_DISTRICT_COUNCIL("Bolsover District Council"),

    @JsonProperty("bostonBoroughCouncil")
    BOSTON_BOROUGH_COUNCIL("Boston Borough Council"),

    @JsonProperty("braintreeDistrictCouncil")
    BRAINTREE_DISTRICT_COUNCIL("Braintree District Council"),

    @JsonProperty("brecklandDistrictCouncil")
    BRECKLAND_DISTRICT_COUNCIL("Breckland District Council"),

    @JsonProperty("brentwoodBoroughCouncil")
    BRENTWOOD_BOROUGH_COUNCIL("Brentwood Borough Council"),

    @JsonProperty("broadlandDistrictCouncil")
    BROADLAND_DISTRICT_COUNCIL("Broadland District Council"),

    @JsonProperty("bromsgroveDistrictCouncil")
    BROMSGROVE_DISTRICT_COUNCIL("Bromsgrove District Council"),

    @JsonProperty("broxbourneBoroughCouncil")
    BROXBOURNE_BOROUGH_COUNCIL("Broxbourne Borough Council"),

    @JsonProperty("broxtoweBoroughCouncil")
    BROXTOWE_BOROUGH_COUNCIL("Broxtowe Borough Council"),

    @JsonProperty("burnleyBoroughCouncil")
    BURNLEY_BOROUGH_COUNCIL("Burnley Borough Council"),

    @JsonProperty("cambridgeCityCouncil")
    CAMBRIDGE_CITY_COUNCIL("Cambridge City Council"),

    @JsonProperty("cannockChaseDistrictCouncil")
    CANNOCK_CHASE_DISTRICT_COUNCIL("Cannock Chase District Council"),

    @JsonProperty("canterburyCityCouncil")
    CANTERBURY_CITY_COUNCIL("Canterbury City Council"),

    @JsonProperty("castlePointDistrictCouncil")
    CASTLE_POINT_DISTRICT_COUNCIL("Castle Point District Council"),

    @JsonProperty("charnwoodBoroughCouncil")
    CHARNWOOD_BOROUGH_COUNCIL("Charnwood Borough Council"),

    @JsonProperty("chelmsfordCityCouncil")
    CHELMSFORD_CITY_COUNCIL("Chelmsford City Council"),

    @JsonProperty("cheltenhamBoroughCouncil")
    CHELTENHAM_BOROUGH_COUNCIL("Cheltenham Borough Council"),

    @JsonProperty("cherwellDistrictCouncil")
    CHERWELL_DISTRICT_COUNCIL("Cherwell District Council"),

    @JsonProperty("chesterfieldBoroughCouncil")
    CHESTERFIELD_BOROUGH_COUNCIL("Chesterfield Borough Council"),

    @JsonProperty("chichesterDistrictCouncil")
    CHICHESTER_DISTRICT_COUNCIL("Chichester District Council"),

    @JsonProperty("chorleyBoroughCouncil")
    CHORLEY_BOROUGH_COUNCIL("Chorley Borough Council"),

    @JsonProperty("colchesterCityCouncil")
    COLCHESTER_CITY_COUNCIL("Colchester City Council"),

    @JsonProperty("cotswoldDistrictCouncil")
    COTSWOLD_DISTRICT_COUNCIL("Cotswold District Council"),

    @JsonProperty("crawleyBoroughCouncil")
    CRAWLEY_BOROUGH_COUNCIL("Crawley Borough Council"),

    @JsonProperty("dacorumBoroughCouncil")
    DACORUM_BOROUGH_COUNCIL("Dacorum Borough Council"),

    @JsonProperty("dartfordBoroughCouncil")
    DARTFORD_BOROUGH_COUNCIL("Dartford Borough Council"),

    @JsonProperty("derbyshireDalesDistrictCouncil")
    DERBYSHIRE_DALES_DISTRICT_COUNCIL("Derbyshire Dales District Council"),

    @JsonProperty("doverDistrictCouncil")
    DOVER_DISTRICT_COUNCIL("Dover District Council"),

    @JsonProperty("eastCambridgeshireDistrictCouncil")
    EAST_CAMBRIDGESHIRE_DISTRICT_COUNCIL("East Cambridgeshire District Council"),

    @JsonProperty("eastDevonDistrictCouncil")
    EAST_DEVON_DISTRICT_COUNCIL("East Devon District Council"),

    @JsonProperty("eastHampshireDistrictCouncil")
    EAST_HAMPSHIRE_DISTRICT_COUNCIL("East Hampshire District Council"),

    @JsonProperty("eastHertfordshireDistrictCouncil")
    EAST_HERTFORDSHIRE_DISTRICT_COUNCIL("East Hertfordshire District Council"),

    @JsonProperty("eastLindseyDistrictCouncil")
    EAST_LINDSEY_DISTRICT_COUNCIL("East Lindsey District Council"),

    @JsonProperty("eastStaffordshireBoroughCouncil")
    EAST_STAFFORDSHIRE_BOROUGH_COUNCIL("East Staffordshire Borough Council"),

    @JsonProperty("eastSuffolkCouncil")
    EAST_SUFFOLK_COUNCIL("East Suffolk Council"),

    @JsonProperty("eastbourneBoroughCouncil")
    EASTBOURNE_BOROUGH_COUNCIL("Eastbourne Borough Council"),

    @JsonProperty("eastleighBoroughCouncil")
    EASTLEIGH_BOROUGH_COUNCIL("Eastleigh Borough Council"),

    @JsonProperty("elmbridgeBoroughCouncil")
    ELMBRIDGE_BOROUGH_COUNCIL("Elmbridge Borough Council"),

    @JsonProperty("eppingForestDistrictCouncil")
    EPPING_FOREST_DISTRICT_COUNCIL("Epping Forest District Council"),

    @JsonProperty("epsomAndEwellBoroughCouncil")
    EPSOM_AND_EWELL_BOROUGH_COUNCIL("Epsom & Ewell Borough Council"),

    @JsonProperty("erewashBoroughCouncil")
    EREWASH_BOROUGH_COUNCIL("Erewash Borough Council"),

    @JsonProperty("exeterCityCouncil")
    EXETER_CITY_COUNCIL("Exeter City Council"),

    @JsonProperty("farehamBoroughCouncil")
    FAREHAM_BOROUGH_COUNCIL("Fareham Borough Council"),

    @JsonProperty("fenlandDistrictCouncil")
    FENLAND_DISTRICT_COUNCIL("Fenland District Council"),

    @JsonProperty("folkestoneAndHytheDistrictCouncil")
    FOLKESTONE_AND_HYTHE_DISTRICT_COUNCIL("Folkestone and Hythe District Council"),

    @JsonProperty("forestOfDeanDistrictCouncil")
    FOREST_OF_DEAN_DISTRICT_COUNCIL("Forest of Dean District Council"),

    @JsonProperty("fyldeBoroughCouncil")
    FYLDE_BOROUGH_COUNCIL("Fylde Borough Council"),

    @JsonProperty("gedlingBoroughCouncil")
    GEDLING_BOROUGH_COUNCIL("Gedling Borough Council"),

    @JsonProperty("gloucesterCityCouncil")
    GLOUCESTER_CITY_COUNCIL("Gloucester City Council"),

    @JsonProperty("gosportBoroughCouncil")
    GOSPORT_BOROUGH_COUNCIL("Gosport Borough Council"),

    @JsonProperty("graveshamBoroughCouncil")
    GRAVESHAM_BOROUGH_COUNCIL("Gravesham Borough Council"),

    @JsonProperty("greatYarmouthBoroughCouncil")
    GREAT_YARMOUTH_BOROUGH_COUNCIL("Great Yarmouth Borough Council"),

    @JsonProperty("guildfordBoroughCouncil")
    GUILDFORD_BOROUGH_COUNCIL("Guildford Borough Council"),

    @JsonProperty("harboroughDistrictCouncil")
    HARBOROUGH_DISTRICT_COUNCIL("Harborough District Council"),

    @JsonProperty("harlowDistrictCouncil")
    HARLOW_DISTRICT_COUNCIL("Harlow District Council"),

    @JsonProperty("hartDistrictCouncil")
    HART_DISTRICT_COUNCIL("Hart District Council"),

    @JsonProperty("hastingsBoroughCouncil")
    HASTINGS_BOROUGH_COUNCIL("Hastings Borough Council"),

    @JsonProperty("havantBoroughCouncil")
    HAVANT_BOROUGH_COUNCIL("Havant Borough Council"),

    @JsonProperty("hertsmereBoroughCouncil")
    HERTSMERE_BOROUGH_COUNCIL("Hertsmere Borough Council"),

    @JsonProperty("highPeakBoroughCouncil")
    HIGH_PEAK_BOROUGH_COUNCIL("High Peak Borough Council"),

    @JsonProperty("hinckleyAndBosworthBoroughCouncil")
    HINCKLEY_AND_BOSWORTH_BOROUGH_COUNCIL("Hinckley and Bosworth Borough Council"),

    @JsonProperty("horshamDistrictCouncil")
    HORSHAM_DISTRICT_COUNCIL("Horsham District Council"),

    @JsonProperty("huntingdonshireDistrictCouncil")
    HUNTINGDONSHIRE_DISTRICT_COUNCIL("Huntingdonshire District Council"),

    @JsonProperty("hyndburnBoroughCouncil")
    HYNDBURN_BOROUGH_COUNCIL("Hyndburn Borough Council"),

    @JsonProperty("ipswichBoroughCouncil")
    IPSWICH_BOROUGH_COUNCIL("Ipswich Borough Council"),

    @JsonProperty("kingsLynnAndWestNorfolkBoroughCouncil")
    KINGS_LYNN_AND_WEST_NORFOLK_BOROUGH_COUNCIL("Kings Lynn & West Norfolk Borough Council"),

    @JsonProperty("lancasterCityCouncil")
    LANCASTER_CITY_COUNCIL("Lancaster City Council"),

    @JsonProperty("lewesDistrictCouncil")
    LEWES_DISTRICT_COUNCIL("Lewes District Council"),

    @JsonProperty("lichfieldCityCouncil")
    LICHFIELD_CITY_COUNCIL("Lichfield City Council"),

    @JsonProperty("lincolnCityCouncil")
    LINCOLN_CITY_COUNCIL("Lincoln City Council"),

    @JsonProperty("maidstoneBoroughCouncil")
    MAIDSTONE_BOROUGH_COUNCIL("Maidstone Borough Council"),

    @JsonProperty("maldonDistrictCouncil")
    MALDON_DISTRICT_COUNCIL("Maldon District Council"),

    @JsonProperty("malvernHillsDistrictCouncil")
    MALVERN_HILLS_DISTRICT_COUNCIL("Malvern Hills District Council"),

    @JsonProperty("mansfieldDistrictCouncil")
    MANSFIELD_DISTRICT_COUNCIL("Mansfield District Council"),

    @JsonProperty("meltonBoroughCouncil")
    MELTON_BOROUGH_COUNCIL("Melton Borough Council"),

    @JsonProperty("midDevonDistrictCouncil")
    MID_DEVON_DISTRICT_COUNCIL("Mid Devon District Council"),

    @JsonProperty("midSuffolkDistrictCouncil")
    MID_SUFFOLK_DISTRICT_COUNCIL("Mid Suffolk District Council"),

    @JsonProperty("midSussexDistrictCouncil")
    MID_SUSSEX_DISTRICT_COUNCIL("Mid Sussex District Council"),

    @JsonProperty("moleValleyDistrictCouncil")
    MOLE_VALLEY_DISTRICT_COUNCIL("Mole Valley District Council"),

    @JsonProperty("newForestDistrictCouncil")
    NEW_FOREST_DISTRICT_COUNCIL("New Forest District Council"),

    @JsonProperty("newarkAndSherwoodDistrictCouncil")
    NEWARK_AND_SHERWOOD_DISTRICT_COUNCIL("Newark & Sherwood District Council"),

    @JsonProperty("newcastleUnderLymeBoroughCouncil")
    NEWCASTLE_UNDER_LYME_BOROUGH_COUNCIL("Newcastle-Under-Lyme Borough Council"),

    @JsonProperty("northDevonDistrictCouncil")
    NORTH_DEVON_DISTRICT_COUNCIL("North Devon District Council"),

    @JsonProperty("northEastDerbyshireDistrictCouncil")
    NORTH_EAST_DERBYSHIRE_DISTRICT_COUNCIL("North East Derbyshire District Council"),

    @JsonProperty("northHertfordshireDistrictCouncil")
    NORTH_HERTFORDSHIRE_DISTRICT_COUNCIL("North Hertfordshire District Council"),

    @JsonProperty("northKestevenDistrictCouncil")
    NORTH_KESTEVEN_DISTRICT_COUNCIL("North Kesteven District Council"),

    @JsonProperty("northNorfolkDistrictCouncil")
    NORTH_NORFOLK_DISTRICT_COUNCIL("North Norfolk District Council"),

    @JsonProperty("northWestLeicestershireDistrictCouncil")
    NORTH_WEST_LEICESTERSHIRE_DISTRICT_COUNCIL("North West Leicestershire District Council"),

    @JsonProperty("northWarwickshireBoroughCouncil")
    NORTH_WARWICKSHIRE_BOROUGH_COUNCIL("North Warwickshire Borough Council"),

    @JsonProperty("norwichCityCouncil")
    NORWICH_CITY_COUNCIL("Norwich City Council"),

    @JsonProperty("nuneatonAndBedworthBoroughCouncil")
    NUNEATON_AND_BEDWORTH_BOROUGH_COUNCIL("Nuneaton & Bedworth Borough Council"),

    @JsonProperty("oadbyAndWigstonBoroughCouncil")
    OADBY_AND_WIGSTON_BOROUGH_COUNCIL("Oadby & Wigston Borough Council"),

    @JsonProperty("oxfordCityCouncil")
    OXFORD_CITY_COUNCIL("Oxford City Council"),

    @JsonProperty("pendleBoroughCouncil")
    PENDLE_BOROUGH_COUNCIL("Pendle Borough Council"),

    @JsonProperty("prestonCityCouncil")
    PRESTON_CITY_COUNCIL("Preston City Council"),

    @JsonProperty("redditchBoroughCouncil")
    REDDITCH_BOROUGH_COUNCIL("Redditch Borough Council"),

    @JsonProperty("reigateAndBansteadBoroughCouncil")
    REIGATE_AND_BANSTEAD_BOROUGH_COUNCIL("Reigate & Banstead Borough Council"),

    @JsonProperty("ribbleValleyBoroughCouncil")
    RIBBLE_VALLEY_BOROUGH_COUNCIL("Ribble Valley Borough Council"),

    @JsonProperty("rochfordDistrictCouncil")
    ROCHFORD_DISTRICT_COUNCIL("Rochford District Council"),

    @JsonProperty("rossendaleBoroughCouncil")
    ROSSENDALE_BOROUGH_COUNCIL("Rossendale Borough Council"),

    @JsonProperty("rotherDistrictCouncil")
    ROTHER_DISTRICT_COUNCIL("Rother District Council"),

    @JsonProperty("rugbyBoroughCouncil")
    RUGBY_BOROUGH_COUNCIL("Rugby Borough Council"),

    @JsonProperty("runnymedeBoroughCouncil")
    RUNNYMEDE_BOROUGH_COUNCIL("Runnymede Borough Council"),

    @JsonProperty("rushcliffeBoroughCouncil")
    RUSHCLIFFE_BOROUGH_COUNCIL("Rushcliffe Borough Council"),

    @JsonProperty("rushmoorBoroughCouncil")
    RUSHMOOR_BOROUGH_COUNCIL("Rushmoor Borough Council"),

    @JsonProperty("sevenoaksDistrictCouncil")
    SEVENOAKS_DISTRICT_COUNCIL("Sevenoaks District Council"),

    @JsonProperty("southCambridgeshireDistrictCouncil")
    SOUTH_CAMBRIDGESHIRE_DISTRICT_COUNCIL("South Cambridgeshire District Council"),

    @JsonProperty("southDerbyshireDistrictCouncil")
    SOUTH_DERBYSHIRE_DISTRICT_COUNCIL("South Derbyshire District Council"),

    @JsonProperty("southHamsDistrictCouncil")
    SOUTH_HAMS_DISTRICT_COUNCIL("South Hams District Council"),

    @JsonProperty("southHollandDistrictCouncil")
    SOUTH_HOLLAND_DISTRICT_COUNCIL("South Holland District Council"),

    @JsonProperty("southKestevenDistrictCouncil")
    SOUTH_KESTEVEN_DISTRICT_COUNCIL("South Kesteven District Council"),

    @JsonProperty("southNorfolkDistrictCouncil")
    SOUTH_NORFOLK_DISTRICT_COUNCIL("South Norfolk District Council"),

    @JsonProperty("southOxfordshireDistrictCouncil")
    SOUTH_OXFORDSHIRE_DISTRICT_COUNCIL("South Oxfordshire District Council"),

    @JsonProperty("southRibbleBoroughCouncil")
    SOUTH_RIBBLE_BOROUGH_COUNCIL("South Ribble Borough Council"),

    @JsonProperty("southStaffordshireDistrictCouncil")
    SOUTH_STAFFORDSHIRE_DISTRICT_COUNCIL("South Staffordshire District Council"),

    @JsonProperty("spelthorneBoroughCouncil")
    SPELTHORNE_BOROUGH_COUNCIL("Spelthorne Borough Council"),

    @JsonProperty("stAlbansCityCouncil")
    ST_ALBANS_CITY_COUNCIL("St Albans City Council"),

    @JsonProperty("staffordBoroughCouncil")
    STAFFORD_BOROUGH_COUNCIL("Stafford Borough Council"),

    @JsonProperty("staffordshireMoorlandsDistrictCouncil")
    STAFFORDSHIRE_MOORLANDS_DISTRICT_COUNCIL("Staffordshire Moorlands District Council"),

    @JsonProperty("stevenageBoroughCouncil")
    STEVENAGE_BOROUGH_COUNCIL("Stevenage Borough Council"),

    @JsonProperty("stratfordOnAvonDistrictCouncil")
    STRATFORD_ON_AVON_DISTRICT_COUNCIL("Stratford on Avon District Council"),

    @JsonProperty("stroudDistrictCouncil")
    STROUD_DISTRICT_COUNCIL("Stroud District Council"),

    @JsonProperty("surreyHeathBoroughCouncil")
    SURREY_HEATH_BOROUGH_COUNCIL("Surrey Heath Borough Council"),

    @JsonProperty("swaleBoroughCouncil")
    SWALE_BOROUGH_COUNCIL("Swale Borough Council"),

    @JsonProperty("tamworthBoroughCouncil")
    TAMWORTH_BOROUGH_COUNCIL("Tamworth Borough Council"),

    @JsonProperty("tandridgeDistrictCouncil")
    TANDRIDGE_DISTRICT_COUNCIL("Tandridge District Council"),

    @JsonProperty("teignbridgeDistrictCouncil")
    TEIGNBRIDGE_DISTRICT_COUNCIL("Teignbridge District Council"),

    @JsonProperty("tendringDistrictCouncil")
    TENDRING_DISTRICT_COUNCIL("Tendring District Council"),

    @JsonProperty("testValleyBoroughCouncil")
    TEST_VALLEY_BOROUGH_COUNCIL("Test Valley Borough Council"),

    @JsonProperty("tewkesburyBoroughCouncil")
    TEWKESBURY_BOROUGH_COUNCIL("Tewkesbury Borough Council"),

    @JsonProperty("thanetDistrictCouncil")
    THANET_DISTRICT_COUNCIL("Thanet District Council"),

    @JsonProperty("threeRiversDistrictCouncil")
    THREE_RIVERS_DISTRICT_COUNCIL("Three Rivers District Council"),

    @JsonProperty("tonbridgeAndMallingBoroughCouncil")
    TONBRIDGE_AND_MALLING_BOROUGH_COUNCIL("Tonbridge & Malling Borough Council"),

    @JsonProperty("torridgeDistrictCouncil")
    TORRIDGE_DISTRICT_COUNCIL("Torridge District Council"),

    @JsonProperty("tunbridgeWellsBoroughCouncil")
    TUNBRIDGE_WELLS_BOROUGH_COUNCIL("Tunbridge Wells Borough Council"),

    @JsonProperty("uttlesfordDistrictCouncil")
    UTTLESFORD_DISTRICT_COUNCIL("Uttlesford District Council"),

    @JsonProperty("valeOfWhiteHorseDistrictCouncil")
    VALE_OF_WHITE_HORSE_DISTRICT_COUNCIL("Vale of White Horse District Council"),

    @JsonProperty("warwickDistrictCouncil")
    WARWICK_DISTRICT_COUNCIL("Warwick District Council"),

    @JsonProperty("watfordBoroughCouncil")
    WATFORD_BOROUGH_COUNCIL("Watford Borough Council"),

    @JsonProperty("waverleyBoroughCouncil")
    WAVERLEY_BOROUGH_COUNCIL("Waverley Borough Council"),

    @JsonProperty("wealdenDistrictCouncil")
    WEALDEN_DISTRICT_COUNCIL("Wealden District Council"),

    @JsonProperty("welwynHatfieldBoroughCouncil")
    WELWYN_HATFIELD_BOROUGH_COUNCIL("Welwyn Hatfield Borough Council"),

    @JsonProperty("westDevonDistrictCouncil")
    WEST_DEVON_DISTRICT_COUNCIL("West Devon District Council"),

    @JsonProperty("westLancashireDistrictCouncil")
    WEST_LANCASHIRE_DISTRICT_COUNCIL("West Lancashire District Council"),

    @JsonProperty("westLindseyDistrictCouncil")
    WEST_LINDSEY_DISTRICT_COUNCIL("West Lindsey District Council"),

    @JsonProperty("westOxfordshireDistrictCouncil")
    WEST_OXFORDSHIRE_DISTRICT_COUNCIL("West Oxfordshire District Council"),

    @JsonProperty("westSuffolkCouncil")
    WEST_SUFFOLK_COUNCIL("West Suffolk Council"),

    @JsonProperty("winchesterCityCouncil")
    WINCHESTER_CITY_COUNCIL("Winchester City Council"),

    @JsonProperty("wokingBoroughCouncil")
    WOKING_BOROUGH_COUNCIL("Woking Borough Council"),

    @JsonProperty("worcesterCityCouncil")
    WORCESTER_CITY_COUNCIL("Worcester City Council"),

    @JsonProperty("worthingBoroughCouncil")
    WORTHING_BOROUGH_COUNCIL("Worthing Borough Council"),

    @JsonProperty("wychavonDistrictCouncil")
    WYCHAVON_DISTRICT_COUNCIL("Wychavon District Council"),

    @JsonProperty("wyreBoroughCouncil")
    WYRE_BOROUGH_COUNCIL("Wyre Borough Council"),

    @JsonProperty("wyreForestDistrictCouncil")
    WYRE_FOREST_DISTRICT_COUNCIL("Wyre Forest District Council");

    private final String label;

    LocalAuthority(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
