package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test rule for {@link GeometryChangeOperationTest}
 *
 * @author Taylor Smock
 */
public class GeometryChangeOperationTestRule extends CoreTestRule
{
    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_1_COORDINATES)),
            @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)) }, edges = @Edge(id = "1", tags = "highway=residential", coordinates = {
                    @Loc(Location.TEST_1_COORDINATES), @Loc(Location.TEST_2_COORDINATES) }))
    private Atlas edgeAtlas;

    public Atlas getEdgeAtlas()
    {
        return this.edgeAtlas;
    }
}
