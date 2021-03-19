package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Tests for {@link TallBuildingCheck}
 *
 * @author v-garei
 */
public class TallBuildingCheckTestRule extends CoreTestRule
{

    // building nodes
    public static final String NODE_1 = "-36.8520257, 174.7684098";
    public static final String NODE_2 = "-36.8518690, 174.7685705";
    public static final String NODE_3 = "-36.8520447, 174.7688274";
    public static final String NODE_4 = "-36.8521979, 174.7686716";

    @TestAtlas(areas = { @TestAtlas.Area(coordinates = { @TestAtlas.Loc(value = NODE_1),
            @TestAtlas.Loc(value = NODE_2), @TestAtlas.Loc(value = NODE_3),
            @TestAtlas.Loc(value = NODE_4) }, tags = { "building=yes", "height=m o _" }) })

    private Atlas heightTagDoesNotContainNumericalCharacter;

    @TestAtlas(areas = { @TestAtlas.Area(coordinates = { @TestAtlas.Loc(value = NODE_1),
            @TestAtlas.Loc(value = NODE_2), @TestAtlas.Loc(value = NODE_3),
            @TestAtlas.Loc(value = NODE_4) }, tags = { "building=yes", "height=3m" }) })

    private Atlas heightTagNeedsSpace;

    @TestAtlas(areas = { @TestAtlas.Area(coordinates = { @TestAtlas.Loc(value = NODE_1),
            @TestAtlas.Loc(value = NODE_2), @TestAtlas.Loc(value = NODE_3),
            @TestAtlas.Loc(value = NODE_4) }, tags = { "building=yes", "height=3)" }) })

    private Atlas invalidHeightTagCharacter;

    @TestAtlas(areas = { @TestAtlas.Area(coordinates = { @TestAtlas.Loc(value = NODE_1),
            @TestAtlas.Loc(value = NODE_2), @TestAtlas.Loc(value = NODE_3),
            @TestAtlas.Loc(value = NODE_4) }, tags = { "building=yes", "building:levels=1-m" }) })

    private Atlas invalidLevelsTag;

    @TestAtlas(areas = { @TestAtlas.Area(coordinates = { @TestAtlas.Loc(value = NODE_1),
            @TestAtlas.Loc(value = NODE_2), @TestAtlas.Loc(value = NODE_3),
            @TestAtlas.Loc(value = NODE_4) }, tags = { "building=yes", "building:levels=110" }) })

    private Atlas levelsTagOver100;

    public Atlas heightTagDoesNotContainNumericalCharacter()
    {
        return this.heightTagDoesNotContainNumericalCharacter;
    }

    public Atlas heightTagNeedsSpace()
    {
        return this.heightTagNeedsSpace;
    }

    public Atlas invalidHeightTagCharacter()
    {
        return this.invalidHeightTagCharacter;
    }

    public Atlas invalidLevelsTag()
    {
        return this.invalidLevelsTag;
    }

    public Atlas levelsTagOver100()
    {
        return this.levelsTagOver100;
    }
}
