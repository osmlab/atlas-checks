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

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = ROUND_ONE), @Loc(value = ROUND_TWO),
            @Loc(value = ROUND_THREE) }, tags = "building=yes") })
    private Atlas spikyButSmall;

    @TestAtlas(areas = {
            @Area(id = "1", coordinates = { @Loc(value = A), @Loc(value = B), @Loc(value = C),
                    @Loc(value = D), @Loc(value = E), @Loc(value = F) }, tags = "building=yes"), })
    private Atlas badCase;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = G), @Loc(value = H), @Loc(value = I),
            @Loc(value = J), @Loc(value = K) }, tags = "building=yes") })
    private Atlas badCase2;

    public Atlas getSpikyBuilding()
    {
        return spikyBuilding;
    }

    public Atlas getNormalBuilding()
    {
        return normalBuilding;
    }

    public Atlas getRoundNumbersSpiky()
    {
        return roundNumbersSpiky;
    }

    public Atlas getNormalRound()
    {
        return normalRound;
    }

    public Atlas getSpikyButSmall()
    {
        return spikyButSmall;
    }

    public Atlas badCase()
    {
        return badCase;
    }

    public Atlas badCase2()
    {
        return badCase2;
    }
}
