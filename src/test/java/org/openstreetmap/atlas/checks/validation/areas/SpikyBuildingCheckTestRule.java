package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * SpikyBuildingCheck test atlases
 *
 * @author nachtm
 */
public class SpikyBuildingCheckTestRule extends CoreTestRule
{
    private static final String ONE = "47.616988, -122.356726";
    private static final String TWO = "47.617031, -122.353797";
    private static final String TWO_A = "47.617031, -122.350797";
    private static final String THREE = "47.615671, -122.356";
    private static final String FOUR = "47.615729, -122.353898";

    private static final String ROUND_ONE = "0.0000005,0.0000005";
    private static final String ROUND_TWO = "0.0000035,0.0000005";
    private static final String ROUND_THREE = "0.0000025,0.0000006";
    private static final String ROUND_FOUR = "0.0000025,0.0000020";
    private static final String ROUND_FIVE = "0.0000005,0.0000020";

    private static final String A = "22.5136768, 114.0802380";
    private static final String B = "22.5136402, 114.0802804";
    private static final String C = "22.5136130, 114.0802528";
    private static final String D = "22.5135873, 114.0802267";
    private static final String E = "22.5135594, 114.0801985";
    private static final String F = "22.5135961, 114.0801561";

    private static final String G = "50.4542345, -4.8259661";
    private static final String H = "50.4542210, -4.8257859";
    private static final String I = "50.4542209, -4.8257867";
    private static final String J = "50.4541504, -4.8257998";
    private static final String K = "50.4541645, -4.8259790";

    private static final String L = "1.2796474, 103.8383986";
    private static final String M = "1.2796485, 103.8382257";
    private static final String N = "1.2796800, 103.8382153";
    private static final String O = "1.2797157, 103.8382624";
    private static final String P = "1.2797573, 103.8382886";
    private static final String Q = "1.2798157, 103.8383006";
    private static final String R = "1.2798743, 103.8382891";
    private static final String S = "1.2799293, 103.8382500";
    private static final String T = "1.2799582, 103.8382024";
    private static final String U = "1.2799679, 103.8381499";
    private static final String U_PRIME = "1.27996945, 103.838543";
    private static final String V = "1.2799710, 103.8389356";
    private static final String V_PRIME = "1.27995015, 103.8385928";

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = ONE), @Loc(value = TWO_A),
            @Loc(value = TWO), @Loc(value = THREE) }, tags = "building=yes") })
    private Atlas spikyBuilding;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = ONE), @Loc(value = TWO),
            @Loc(value = FOUR), @Loc(value = THREE) }, tags = "building=yes") })
    private Atlas normalBuilding;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = ROUND_ONE), @Loc(value = ROUND_THREE),
            @Loc(value = ROUND_FOUR), @Loc(value = ROUND_FIVE) }, tags = "building=yes") })
    private Atlas normalRound;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = ROUND_ONE), @Loc(value = ROUND_TWO),
            @Loc(value = ROUND_THREE), @Loc(value = ROUND_FOUR),
            @Loc(value = ROUND_FIVE) }, tags = "building=yes") })
    private Atlas roundNumbersSpiky;

    @TestAtlas(areas = {
            @Area(id = "1", coordinates = { @Loc(value = A), @Loc(value = B), @Loc(value = C),
                    @Loc(value = D), @Loc(value = E), @Loc(value = F) }, tags = "building=yes"), })
    private Atlas badCase;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = G), @Loc(value = H), @Loc(value = I),
            @Loc(value = J), @Loc(value = K) }, tags = "building=yes") })
    private Atlas badCase2;

    @TestAtlas(areas = {
            @Area(id = "1", coordinates = { @Loc(value = L), @Loc(value = M), @Loc(value = N),
                    @Loc(value = O), @Loc(value = P), @Loc(value = Q), @Loc(value = R),
                    @Loc(value = S), @Loc(value = T), @Loc(value = U),
                    @Loc(value = V) }, tags = "building=yes"),
            @Area(id = "2", coordinates = { @Loc(value = P), @Loc(value = Q), @Loc(value = R),
                    @Loc(value = S), @Loc(value = T), @Loc(value = U), @Loc(value = V),
                    @Loc(value = L), @Loc(value = M), @Loc(value = N),
                    @Loc(value = O) }, tags = "building=yes"),
            @Area(id = "3", coordinates = { @Loc(value = Q), @Loc(value = R), @Loc(value = S),
                    @Loc(value = T), @Loc(value = U), @Loc(value = V),
                    @Loc(value = L) }, tags = "building=yes") })
    private Atlas circleBuilding;

    @TestAtlas(areas = { @Area(id = "1", coordinates = { @Loc(value = S), @Loc(value = T),
            @Loc(value = U), @Loc(value = U_PRIME), @Loc(value = V), @Loc(value = V_PRIME),
            @Loc(value = L), @Loc(value = N) }, tags = "building=yes") })
    private Atlas twoShortConsecutiveCurvesBuilding;

    public Atlas badCase()
    {
        return this.badCase;
    }

    public Atlas badCase2()
    {
        return this.badCase2;
    }

    public Atlas circleBuilding()
    {
        return this.circleBuilding;
    }

    public Atlas getNormalBuilding()
    {
        return this.normalBuilding;
    }

    public Atlas getNormalRound()
    {
        return this.normalRound;
    }

    public Atlas getRoundNumbersSpiky()
    {
        return this.roundNumbersSpiky;
    }

    public Atlas getSpikyBuilding()
    {
        return this.spikyBuilding;
    }

    public Atlas twoShortConsecutiveCurvesBuilding()
    {
        return this.twoShortConsecutiveCurvesBuilding;
    }
}
