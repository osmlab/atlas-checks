package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link StreetNameIntegersOnlyCheck}
 *
 * @author bbreithaupt
 */

public class StreetNameIntegersOnlyCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=\t20 19" }) })
    private Atlas motorwayWithIntegerNameTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=Way St" }) })
    private Atlas motorwayWithNonIntegerNameTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=1st St" }) })
    private Atlas motorwayWithMixedNameTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=1st St",
                            "name:left=1" }) })
    private Atlas motorwayWithMixedNameTagIntegerNameLeftTagAtlas;

    public Atlas motorwayWithIntegerNameTagAtlas()
    {
        return this.motorwayWithIntegerNameTagAtlas;
    }

    public Atlas motorwayWithNonIntegerNameTagAtlas()
    {
        return this.motorwayWithNonIntegerNameTagAtlas;
    }

    public Atlas motorwayWithMixedNameTagAtlas()
    {
        return this.motorwayWithMixedNameTagAtlas;
    }

    public Atlas motorwayWithMixedNameTagIntegerNameLeftTagAtlas()
    {
        return this.motorwayWithMixedNameTagIntegerNameLeftTagAtlas;
    }
}
