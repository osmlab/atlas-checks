package org.openstreetmap.atlas.checks.maproulette.data;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Atlases for {@link TaskTest}
 *
 * @author Taylor Smock
 */
public class TaskTestRule extends CoreTestRule
{
    @TestAtlas(points = { @Point(id = "1000000", coordinates = @Loc(Location.TEST_1_COORDINATES)),
            @Point(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)) }, lines = @Line(id = "1000000", coordinates = {
                    @Loc(Location.TEST_1_COORDINATES), @Loc(Location.TEST_2_COORDINATES) }))
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
