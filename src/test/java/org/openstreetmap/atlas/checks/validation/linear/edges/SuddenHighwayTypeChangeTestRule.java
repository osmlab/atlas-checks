package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

public class SuddenHighwayTypeChangeTestRule extends CoreTestRule {

    private static final String WAY1_LOC1 = "37.4060235, 41.3680762";
    private static final String WAY1_WAY2_INTERSECTION = "37.4055918, 41.3698508";
    private static final String WAY1_LOC3 = "37.4054417, 41.3705483";

    private static final String WAY2_LOC1  = "37.4054258, 41.3702144";
    private static final String WAY2_LOC2 = "37.4053224, 41.3703283";
    private static final String WAY2_LOC3 = "37.4052126, 41.3703811";
    private static final String WAY2_LOC4 = "37.4050835, 41.3704055";
    private static final String WAY2_WAY3_INTERSECTION = "37.4049012, 41.3704203";

    private static final String WAY3_LOC2 = "37.4043780, 41.3701141";


    @TestAtlas(
            nodes = {
                @Node(coordinates = @Loc(value = WAY1_LOC1)),
                @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
                @Node(coordinates = @Loc(value = WAY1_LOC3)),
                @Node(coordinates = @Loc(value = WAY2_LOC1)),
                @Node(coordinates = @Loc(value = WAY2_LOC2)),
                @Node(coordinates = @Loc(value = WAY2_LOC3)),
                @Node(coordinates = @Loc(value = WAY2_LOC4)),
                @Node(coordinates = @Loc(value = WAY2_WAY3_INTERSECTION)),
                @Node(coordinates = @Loc(value = WAY3_LOC2))},

            edges = {
                @Edge(coordinates = {@Loc(value = WAY1_LOC1), @Loc(value = WAY1_WAY2_INTERSECTION)}, tags = {"highway=secondary"}),
                @Edge(coordinates = {@Loc(value = WAY1_WAY2_INTERSECTION), @Loc(value = WAY1_LOC3)}, tags = {"highway=secondary"}),
                @Edge(coordinates = {@Loc(value = WAY1_WAY2_INTERSECTION), @Loc(value = WAY2_LOC1),
                    @Loc(value = WAY2_LOC2), @Loc(value = WAY2_LOC3), @Loc(value = WAY2_LOC4), @Loc(value = WAY2_WAY3_INTERSECTION)}, tags = {"highway=tertiary"}),
                @Edge(coordinates = {@Loc(value = WAY2_WAY3_INTERSECTION), @Loc(value = WAY3_LOC2)}, tags = {"highway=tertiary"})
                    }
    )
    private Atlas truePositiveSuddenHighwayChange;

    @TestAtlas(
            nodes = {
                @Node(coordinates = @Loc(value = WAY1_LOC1)),
                @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
                @Node(coordinates = @Loc(value = WAY1_LOC3)),
                @Node(coordinates = @Loc(value = WAY2_LOC1)),
                @Node(coordinates = @Loc(value = WAY2_LOC2)),
                @Node(coordinates = @Loc(value = WAY2_LOC3)),
                @Node(coordinates = @Loc(value = WAY2_LOC4)),
                @Node(coordinates = @Loc(value = WAY2_WAY3_INTERSECTION)),
                @Node(coordinates = @Loc(value = WAY3_LOC2))},

            edges = {
                @Edge(coordinates = {@Loc(value = WAY1_LOC1), @Loc(value = WAY1_WAY2_INTERSECTION)}, tags = {"highway=secondary"}),
                @Edge(coordinates = {@Loc(value = WAY1_WAY2_INTERSECTION), @Loc(value = WAY1_LOC3)}, tags = {"highway=secondary"}),
                @Edge(coordinates = {@Loc(value = WAY1_WAY2_INTERSECTION), @Loc(value = WAY2_LOC1),
                        @Loc(value = WAY2_LOC2), @Loc(value = WAY2_LOC3), @Loc(value = WAY2_LOC4), @Loc(value = WAY2_WAY3_INTERSECTION)}, tags = {"highway=secondary_link"}),
                @Edge(coordinates = {@Loc(value = WAY2_WAY3_INTERSECTION), @Loc(value = WAY3_LOC2)}, tags = {"highway=tertiary"})
            }
    )
    private Atlas falsePositiveSuddenHighwayChange;

    public Atlas falsePositiveSuddenHighwayChange()
    {
        return this.falsePositiveSuddenHighwayChange;
    }

    public Atlas truePositiveSuddenHighwayChange()
    {
        return this.truePositiveSuddenHighwayChange;
    }
}
