package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link ApproximateWayCheck}
 *
 * @author v-brjor
 */
public class ApproximateWayCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "1, 0";
    private static final String TEST_2 = "0, 0";
    private static final String TEST_3 = "0, 1";

    private static final String TEST_4 = "-46.1827, 168.47972";
    private static final String TEST_5 = "-46.181988, 168.4833022";
    private static final String TEST_6 = "-46.17616, 168.49038";

    private static final String TEST_7 = "-45.927904, 168.2816058";
    private static final String TEST_8 = "-45.927667, 168.2806711";
    private static final String TEST_9 = "-45.9276148, 168.280596";
    private static final String TEST_10 = "-45.9275439, 168.2805558";
    private static final String TEST_11 = "-45.9272734, 168.2805397";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_4)),
            @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = "highway=SECONDARY") })
    private Atlas invalidApproximateWayAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }) })
    private Atlas singleSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)),
            @Node(coordinates = @Loc(value = TEST_11)), }, edges = { @Edge(coordinates = {
                    @Loc(value = TEST_7), @Loc(value = TEST_8), @Loc(value = TEST_9),
                    @Loc(value = TEST_10), @Loc(value = TEST_11) }, tags = "highway=SECONDARY") })
    private Atlas validApproximateWayAtlas;

    public Atlas invalidApproximateWayAtlas()
    {
        return this.invalidApproximateWayAtlas;
    }

    public Atlas singleSegmentAtlas()
    {
        return this.singleSegmentAtlas;
    }

    public Atlas validApproximateWayAtlas()
    {
        return this.validApproximateWayAtlas;
    }
}
