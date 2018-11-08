package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.*;

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

    @TestAtlas(
            buildings = @Building(id = "1", outer = @Area(coordinates = {@Loc(value=ONE), @Loc(value=TWO_A), @Loc(value=TWO), @Loc(value=THREE)}), tags={"building=yes"})
    )
    private Atlas spikyBuilding;

    @TestAtlas(
            buildings = @Building(outer = @Area(coordinates = {@Loc(value=ONE), @Loc(value=TWO), @Loc(value=FOUR), @Loc(value=THREE)}))
    )
    private Atlas normalBuilding;

    @TestAtlas(
            buildings = @Building(outer = @Area(coordinates = {@Loc(value=ROUND_ONE), @Loc(value = ROUND_THREE), @Loc(value = ROUND_FOUR), @Loc(value = ROUND_FIVE)}))
    )
    private Atlas normalRound;

    @TestAtlas(
            buildings = @Building(outer = @Area(coordinates = {@Loc(value=ROUND_ONE), @Loc(value=ROUND_TWO), @Loc(value=ROUND_THREE), @Loc(value=ROUND_FOUR), @Loc(value=ROUND_FIVE)}))
    )
    private Atlas roundNumbersSpiky;

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
}
