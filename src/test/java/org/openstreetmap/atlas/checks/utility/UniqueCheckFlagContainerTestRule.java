package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test Rule for {@link UniqueCheckFlagContainerTest}
 *
 * @author bbreithaupt
 */
public class UniqueCheckFlagContainerTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.8586236, -122.6417178";
    private static final String TEST_2 = "47.8598930, -122.6398508";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }) })
    private Atlas atlas;

    public Atlas atlas()
    {
        return this.atlas;
    }
}
