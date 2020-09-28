package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link SuddenHighwayChangeCheck}
 *
 * @author v-garei
 */
public class SuddenHighwayChangeCheckTestRule extends CoreTestRule
{
    private static final String WAY1_LOC1 = "37.4060235, 41.3680762";
    private static final String WAY1_WAY2_INTERSECTION = "37.4055918, 41.3698508";
    private static final String WAY1_LOC3 = "37.4054417, 41.3705483";

    private static final String WAY2_LOC1 = "37.4054258, 41.3702144";
    private static final String WAY2_LOC2 = "37.4053224, 41.3703283";
    private static final String WAY2_LOC3 = "37.4052126, 41.3703811";
    private static final String WAY2_LOC4 = "37.4050835, 41.3704055";
    private static final String WAY2_WAY3_INTERSECTION = "37.4049012, 41.3704203";

    private static final String WAY3_LOC1 = "37.4054417, 41.3705483";
    private static final String WAY3_LOC3 = "37.4043780, 41.3701141";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_LOC1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY1_LOC3)),
            @Node(coordinates = @Loc(value = WAY2_LOC1)),
            @Node(coordinates = @Loc(value = WAY2_LOC2)),
            @Node(coordinates = @Loc(value = WAY2_LOC3)),
            @Node(coordinates = @Loc(value = WAY2_LOC4)),
            @Node(coordinates = @Loc(value = WAY2_WAY3_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY3_LOC3)),
            @Node(coordinates = @Loc(value = WAY3_LOC1)) },

            edges = {
                    @Edge(id = "1000001", coordinates = { @Loc(value = WAY1_LOC1),
                            @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = { "highway=secondary" }),
                    @Edge(id = "1000002", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY1_LOC3) }, tags = { "highway=secondary" }),
                    @Edge(id = "2000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY2_LOC1), @Loc(value = WAY2_LOC2),
                            @Loc(value = WAY2_LOC3), @Loc(value = WAY2_LOC4),
                            @Loc(value = WAY2_WAY3_INTERSECTION) }, tags = { "highway=tertiary" }),
                    @Edge(id = "3000002", coordinates = { @Loc(value = WAY2_WAY3_INTERSECTION),
                            @Loc(value = WAY3_LOC3) }, tags = { "highway=tertiary" }),
                    @Edge(id = "3000001", coordinates = { @Loc(value = WAY3_LOC1),
                            @Loc(value = WAY2_WAY3_INTERSECTION) }, tags = {
                                    "highway=tertiary" }) })

    private Atlas truePositiveSuddenHighwayChangeCheck;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_LOC1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY1_LOC3)),
            @Node(coordinates = @Loc(value = WAY2_LOC1)),
            @Node(coordinates = @Loc(value = WAY2_LOC2)),
            @Node(coordinates = @Loc(value = WAY2_LOC3)),
            @Node(coordinates = @Loc(value = WAY2_LOC4)),
            @Node(coordinates = @Loc(value = WAY2_WAY3_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY3_LOC3)),
            @Node(coordinates = @Loc(value = WAY3_LOC1)) },

            edges = {
                    @Edge(id = "4000001", coordinates = { @Loc(value = WAY1_LOC1),
                            @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = { "highway=secondary" }),
                    @Edge(id = "4000002", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY1_LOC3) }, tags = { "highway=secondary" }),
                    @Edge(id = "5000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY2_LOC1), @Loc(value = WAY2_LOC2),
                            @Loc(value = WAY2_LOC3), @Loc(value = WAY2_LOC4),
                            @Loc(value = WAY2_WAY3_INTERSECTION) }, tags = {
                                    "highway=secondary_link" }),
                    @Edge(id = "6000002", coordinates = { @Loc(value = WAY2_WAY3_INTERSECTION),
                            @Loc(value = WAY3_LOC3) }, tags = { "highway=tertiary" }),
                    @Edge(id = "6000001", coordinates = { @Loc(value = WAY3_LOC1),
                            @Loc(value = WAY2_WAY3_INTERSECTION) }, tags = {
                                    "highway=tertiary" }) })
    private Atlas falsePositiveSuddenHighwayChangeCheck;

    public Atlas falsePositiveSuddenHighwayChangeCheck()
    {
        return this.falsePositiveSuddenHighwayChangeCheck;
    }

    public Atlas truePositiveSuddenHighwayChangeCheck()
    {
        return this.truePositiveSuddenHighwayChangeCheck;
    }
}
