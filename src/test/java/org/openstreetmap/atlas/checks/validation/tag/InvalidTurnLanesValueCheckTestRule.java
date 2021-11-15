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
 * @author mselaineleong
 */

public class InvalidTurnLanesValueCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";
    private static final String TEST_4 = "47.2138114677627,-122.440990166979";
    private static final String TEST_5 = "47.2136200921786,-122.44001973284";
    private static final String TEST_6 = "47.2135137721113,-122.439127559518";
    private static final String TEST_7 = "47.2136200921786,-122.438157125378";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "lanes=1.5", "turn:lanes=through|right" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas validTurnLanesValue;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_7), tags = { "barrier=toll_booth" }) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "lanes=11",
                                    "turn:lanes=throug|throug|slight_right" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas invalidTurnLanesValue;

    public Atlas invalidTurnLanesValue()
    {
        return this.invalidTurnLanesValue;
    }

    public Atlas validTurnLanesValue()
    {
        return this.validTurnLanesValue;
    }
}
