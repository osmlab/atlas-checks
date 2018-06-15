package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link SharpAngleCheck}
 *
 * @author bbreithaupt
 */
public class SharpAngleCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "48.040836812424,-122.947197210666";
    private static final String TEST_2 = "48.0404742717597,-122.946636785158";
    private static final String TEST_3 = "48.0405764317775,-122.947086805021";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })
    private Atlas sharpeAngle;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_1)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                    @Loc(value = TEST_3), @Loc(value = TEST_1) }, tags = { "highway=motorway" }) })
    private Atlas notSharpeAngle;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_2), @Loc(value = TEST_1),
                    @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })
    private Atlas sharpeAngles;

    public Atlas sharpeAngle()
    {
        return this.sharpeAngle;
    }

    public Atlas notSharpeAngle()
    {
        return this.notSharpeAngle;
    }

    public Atlas sharpeAngles()
    {
        return this.sharpeAngles;
    }
}
