package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link SeparateSidewalkTagCheck} data generator
 *
 * @author vladlemberg
 */
public class SeparateSidewalkTagCheckTestRule extends CoreTestRule
{
    private static final String HIGHWAY_1 = "37.3485228,-121.9449555";
    private static final String HIGHWAY_2 = "37.3490966,-121.9433543";
    private static final String SIDEWALK_RIGHT_1 = "37.34839,-121.94506";
    private static final String SIDEWALK_RIGHT_2 = "37.34874,-121.94408";
    private static final String SIDEWALK_LEFT_1 = "37.34858,-121.94512";
    private static final String SIDEWALK_LEFT_2 = "37.34886,-121.94427";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkRightSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=left" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkLeftSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=both" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkBothSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayRightSidewalkLeftSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=left" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayLeftSidewalkRightSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=both" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }), })
    private Atlas invalidSidewalkBothSide;

    public Atlas getInvalidHighwayLeftSidewalkRight()
    {
        return this.invalidHighwayLeftSidewalkRightSide;
    }

    public Atlas getInvalidHighwayRightSidewalkLeft()
    {
        return this.invalidHighwayRightSidewalkLeftSide;
    }

    public Atlas getInvalidSidewalkBothSide()
    {
        return this.invalidSidewalkBothSide;
    }

    public Atlas getValidSidewalkBothSide()
    {
        return this.validSidewalkBothSide;
    }

    public Atlas getValidSidewalkLeftSide()
    {
        return this.validSidewalkLeftSide;
    }

    public Atlas getValidSidewalkRightSide()
    {
        return this.validSidewalkRightSide;
    }
}
