package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Create test atlases for use with {@link ElevationUtilitiesTest}.
 *
 * @author Taylor Smock
 */
public class ElevationUtilitiesTestRule extends CoreTestRule
{
    @TestAtlas(nodes = {
            @Node(id = "-101752", coordinates = @Loc(value = "28.92414725033,-89.42596868149")),
            @Node(id = "-101754", coordinates = @Loc(value = "28.93350332367,-89.41806350699")),
            @Node(id = "-101755", coordinates = @Loc(value = "28.92996541203,-89.40144467423")),
            @Node(id = "-101756", coordinates = @Loc(value = "28.91942958225,-89.39012590165")),
            @Node(id = "-101757", coordinates = @Loc(value = "28.91345356126,-89.40234298952")),
            @Node(id = "-101758", coordinates = @Loc(value = "28.91376809726,-89.43081958402")) }, lines = {
                    @Line(id = "-101782", coordinates = {
                            @Loc(value = "28.92414725033,-89.42596868149"),
                            @Loc(value = "28.93350332367,-89.41806350699"),
                            @Loc(value = "28.92996541203,-89.40144467423"),
                            @Loc(value = "28.91942958225,-89.39012590165"),
                            @Loc(value = "28.91345356126,-89.40234298952"),
                            @Loc(value = "28.91376809726,-89.43081958402"),
                            @Loc(value = "28.92414725033,-89.42596868149") }, tags = {
                                    "waterway=river" }) })
    private Atlas circularWaterway;

    /**
     * @return A circular waterway (reused from WaterWayCheckTestRule)
     */
    public Atlas getCircularWaterway()
    {
        return this.circularWaterway;
    }
}
