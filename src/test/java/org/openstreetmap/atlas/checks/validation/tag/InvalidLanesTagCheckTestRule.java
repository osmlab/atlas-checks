package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link InvalidLanesTagCheck}
 *
 * @author bbreithaupt
 */

public class InvalidLanesTagCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "0.0,0.0";
    private static final String TEST_2 = "0.1,0.1";
    private static final String TEST_3 = "0.0,0.2";
    private static final String TEST_4 = "0.1,0.3";
    private static final String TEST_5 = "0.0,0.4";
    private static final String TEST_6 = "-0.1,0.5";
    private static final String TEST_7 = "0.0,0.6";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "lanes=1.5" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas validLanesTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "lanes=11" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas invalidLanesTag;

    public Atlas validLanesTag()
    {
        return this.validLanesTag;
    }

    public Atlas invalidLanesTag()
    {
        return this.invalidLanesTag;
    }
}
