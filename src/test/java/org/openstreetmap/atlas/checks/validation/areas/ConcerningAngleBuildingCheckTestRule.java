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

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_2_NODE_1),
            @Loc(value = POLYGON_2_NODE_2), @Loc(value = POLYGON_2_NODE_3),
            @Loc(value = POLYGON_2_NODE_4) }, tags = "building=yes") })

    private Atlas needsSquaredAngleTruePositive;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = POLYGON_NODE_1),
            @Loc(value = POLYGON_NODE_2), @Loc(value = POLYGON_NODE_3),
            @Loc(value = POLYGON_NODE_4) }, tags = "building=yes") })

    private Atlas squaredAngleFalsePositive;

    public Atlas needsSquaredAngleTruePositive()
    {
        return this.needsSquaredAngleTruePositive;
    }

    public Atlas squaredAngleFalsePositive()
    {
        return this.squaredAngleFalsePositive;
    }
}
