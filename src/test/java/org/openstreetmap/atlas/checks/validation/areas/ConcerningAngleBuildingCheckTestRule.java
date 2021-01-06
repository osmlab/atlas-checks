package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * Tests for {@link ConcerningAngleBuildingCheck}
 *
 * @author v-garei
 */
public class ConcerningAngleBuildingCheckTestRule extends CoreTestRule
{

    private static final String POLYGON_NODE_1 = "47.8037110, -122.2112443";
    private static final String POLYGON_NODE_2 = "47.8037110, -122.2111731";
    private static final String POLYGON_NODE_3 = "47.8036180, -122.2111731";
    private static final String POLYGON_NODE_4 = "47.8036180, -122.2112443";

    private static final String POLYGON_2_NODE_1 = "-27.5859601, 151.9420256";
    private static final String POLYGON_2_NODE_2 = "-27.5859912, 151.9422828";
    private static final String POLYGON_2_NODE_3 = "-27.5861153, 151.9422659";
    private static final String POLYGON_2_NODE_4 = "-27.5860886, 151.9420075";

    private static final String POLYGON_3_NODE_1 = "47.8036993, -122.2116721";
    private static final String POLYGON_3_NODE_2 = "47.8037000, -122.2116279";
    private static final String POLYGON_3_NODE_3 = "47.8037049, -122.2116281";
    private static final String POLYGON_3_NODE_4 = "47.8037057, -122.2115764";
    private static final String POLYGON_3_NODE_5 = "47.8037142, -122.2115767";
    private static final String POLYGON_3_NODE_6 = "47.8037161, -122.2114610";
    private static final String POLYGON_3_NODE_7 = "47.8037069, -122.2114607";
    private static final String POLYGON_3_NODE_8 = "47.8037078, -122.2114072";
    private static final String POLYGON_3_NODE_9 = "47.8037023, -122.2114070";
    private static final String POLYGON_3_NODE_10 = "47.8037030, -122.2113641";
    private static final String POLYGON_3_NODE_11 = "47.8036220, -122.2113612";
    private static final String POLYGON_3_NODE_12 = "47.8036215, -122.2113906";
    private static final String POLYGON_3_NODE_13 = "47.8036088, -122.2113901";
    private static final String POLYGON_3_NODE_14 = "47.8036094, -122.2113492";
    private static final String POLYGON_3_NODE_15 = "47.8035703, -122.2113478";
    private static final String POLYGON_3_NODE_16 = "47.8035701, -122.2113626";
    private static final String POLYGON_3_NODE_17 = "47.8035304, -122.2113612";
    private static final String POLYGON_3_NODE_18 = "47.8035297, -122.2114031";
    private static final String POLYGON_3_NODE_19 = "47.8034664, -122.2114008";
    private static final String POLYGON_3_NODE_20 = "47.8034631, -122.2116262";
    private static final String POLYGON_3_NODE_21 = "47.8036192, -122.2116387";
    private static final String POLYGON_3_NODE_22 = "47.8036187, -122.2116692";

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_2_NODE_1),
            @Loc(value = POLYGON_2_NODE_2), @Loc(value = POLYGON_2_NODE_3),
            @Loc(value = POLYGON_2_NODE_4) }, tags = "building=yes") })

    private Atlas needsSquaredAngleTruePositive;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_3_NODE_1),
            @Loc(value = POLYGON_3_NODE_2), @Loc(value = POLYGON_3_NODE_3),
            @Loc(value = POLYGON_3_NODE_4), @Loc(value = POLYGON_3_NODE_5),
            @Loc(value = POLYGON_3_NODE_6), @Loc(value = POLYGON_3_NODE_7),
            @Loc(value = POLYGON_3_NODE_8), @Loc(value = POLYGON_3_NODE_9),
            @Loc(value = POLYGON_3_NODE_10), @Loc(value = POLYGON_3_NODE_11),
            @Loc(value = POLYGON_3_NODE_12), @Loc(value = POLYGON_3_NODE_13),
            @Loc(value = POLYGON_3_NODE_14), @Loc(value = POLYGON_3_NODE_15),
            @Loc(value = POLYGON_3_NODE_16), @Loc(value = POLYGON_3_NODE_17),
            @Loc(value = POLYGON_3_NODE_18), @Loc(value = POLYGON_3_NODE_19),
            @Loc(value = POLYGON_3_NODE_20), @Loc(value = POLYGON_3_NODE_21),
            @Loc(value = POLYGON_3_NODE_22) }, tags = "building=yes") })

    private Atlas overSixteenAnglesFalsePositive;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_NODE_1),
            @Loc(value = POLYGON_NODE_2), @Loc(value = POLYGON_NODE_3),
            @Loc(value = POLYGON_NODE_4) }, tags = "building=yes") })

    private Atlas squaredAngleFalsePositive;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_NODE_1),
            @Loc(value = POLYGON_NODE_2), @Loc(value = POLYGON_NODE_3) }, tags = "building=yes") })

    private Atlas underFourAnglesFalsePositive;

    public Atlas needsSquaredAngleTruePositive()
    {
        return this.needsSquaredAngleTruePositive;
    }

    public Atlas overSixteenAnglesFalsePositive()
    {
        return this.overSixteenAnglesFalsePositive;
    }

    public Atlas squaredAngleFalsePositive()
    {
        return this.squaredAngleFalsePositive;
    }

    public Atlas underFourAnglesFalsePositive()
    {
        return this.underFourAnglesFalsePositive;
    }
}
