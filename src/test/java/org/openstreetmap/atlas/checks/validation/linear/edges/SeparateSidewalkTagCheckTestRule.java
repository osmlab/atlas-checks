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
    private static final String HIGHWAY_3 = "37.34855, -121.94487";
    private static final String SIDEWALK_RIGHT_1 = "37.34839,-121.94506";
    private static final String SIDEWALK_RIGHT_2 = "37.34874,-121.94408";
    private static final String SIDEWALK_LEFT_1 = "37.34858,-121.94512";
    private static final String SIDEWALK_LEFT_2 = "37.34886,-121.94427";
    private static final String SIDEWALK_LEFT_3 = "37.34874, -121.94524";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    @Edge(id = "-1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk=right" }) })
    private Atlas noSidewalk;

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
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:right=separate", "sidewalk:left=no" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkRightSideAlternativeMapping;

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
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:left=separate", "sidewalk:right=no" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkLeftSideAlternativeMapping;

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
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:right=separate", "sidewalk:left=separate" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas validSidewalkBothSideAlternativeMapping;

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
    private Atlas invalidHighwayRightSidewalkLeft;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:right=separate", "sidewalk:left=no" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayRightSidewalkLeftAlternativeMapping;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:right=separate" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayRightSidewalkLeftAlternativeMapping2;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_2),
                            @Loc(value = SIDEWALK_LEFT_1) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayRightSidewalkLeftReverseHeading;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_1) }, tags = {
                                    "highway=residential", "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayClosedWaySidewalkLeftSide;

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
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "foot=yes" }) })
    private Atlas invalidHighwayRightFootwayLeftSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=trunk", "oneway=yes",
                                    "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidSidewalkLHighwayDualCarriageWay;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_3) }, tags = { "highway=residential", "sidewalk=right" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidSidewalkLHighwayShotEdge;

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
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:left=separate", "sidewalk:right=no" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayLeftSidewalkRightSideAlternativeMapping;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:left=separate" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas invalidHighwayLeftSidewalkRightSideAlternativeMapping2;

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
    private Atlas invalidSidewalkBothSideLeftMissing;

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
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }), })
    private Atlas invalidSidewalkBothSideRightMissing;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=residential",
                                    "sidewalk:right=separate", "sidewalk:left=separate" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }), })
    private Atlas invalidSidewalkBothSideAlternativeMapping;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=both" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }),
                    @Edge(id = "-1000000002", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = SIDEWALK_RIGHT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "foot=designated" }) })
    private Atlas invalidSidewalkBothSameSide;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    // sidewalk sharing location with highway
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas sidewalkCrossing;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    // sidewalk sharing location with highway
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = SIDEWALK_LEFT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk", "layer=1" }) })
    private Atlas sidewalkDifferentLayer;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_3)),
                    @Node(coordinates = @Loc(value = SIDEWALK_RIGHT_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    // sidewalk sharing location with highway
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_3),
                            @Loc(value = SIDEWALK_RIGHT_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas sidewalkHeadingOutsideDegreeRange;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = SIDEWALK_LEFT_1)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2) }, tags = { "highway=residential", "sidewalk=right" }),
                    // sidewalk sharing location with highway
                    @Edge(id = "1000000002", coordinates = { @Loc(value = SIDEWALK_LEFT_1),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=footway",
                                    "footway=sidewalk" }) })
    private Atlas sidewalkSharingLocation;

    public Atlas getInvalidHighwayClosedWaySidewalkLeftSide()
    {
        return this.invalidHighwayClosedWaySidewalkLeftSide;
    }

    public Atlas getInvalidHighwayLeftSidewalkRight()
    {
        return this.invalidHighwayLeftSidewalkRightSide;
    }

    public Atlas getInvalidHighwayLeftSidewalkRightSideAlternativeMapping()
    {
        return this.invalidHighwayLeftSidewalkRightSideAlternativeMapping;
    }

    public Atlas getInvalidHighwayLeftSidewalkRightSideAlternativeMapping2()
    {
        return this.invalidHighwayLeftSidewalkRightSideAlternativeMapping2;
    }

    public Atlas getInvalidHighwayRightFootwayLeft()
    {
        return this.invalidHighwayRightFootwayLeftSide;
    }

    public Atlas getInvalidHighwayRightSidewalkLeft()
    {
        return this.invalidHighwayRightSidewalkLeft;
    }

    public Atlas getInvalidHighwayRightSidewalkLeftAlternativeMapping()
    {
        return this.invalidHighwayRightSidewalkLeftAlternativeMapping;
    }

    public Atlas getInvalidHighwayRightSidewalkLeftAlternativeMapping2()
    {
        return this.invalidHighwayRightSidewalkLeftAlternativeMapping2;
    }

    public Atlas getInvalidHighwayRightSidewalkLeftReverseHeading()
    {
        return this.invalidHighwayRightSidewalkLeftReverseHeading;
    }

    public Atlas getInvalidSidewalkBothSameSide()
    {
        return this.invalidSidewalkBothSameSide;
    }

    public Atlas getInvalidSidewalkBothSideAlternativeMapping()
    {
        return this.invalidSidewalkBothSideAlternativeMapping;
    }

    public Atlas getInvalidSidewalkBothSideLeftMissing()
    {
        return this.invalidSidewalkBothSideLeftMissing;
    }

    public Atlas getInvalidSidewalkBothSideRightMissing()
    {
        return this.invalidSidewalkBothSideRightMissing;
    }

    public Atlas getInvalidSidewalkLHighwayDualCarriageWay()
    {
        return this.invalidSidewalkLHighwayDualCarriageWay;
    }

    public Atlas getInvalidSidewalkLHighwayShotEdge()
    {
        return this.invalidSidewalkLHighwayShotEdge;
    }

    public Atlas getNoSidewalk()
    {
        return this.noSidewalk;
    }

    public Atlas getSidewalkCrossing()
    {
        return this.sidewalkCrossing;
    }

    public Atlas getSidewalkDifferentLayer()
    {
        return this.sidewalkDifferentLayer;
    }

    public Atlas getSidewalkHeadingOutsideDegreeRange()
    {
        return this.sidewalkHeadingOutsideDegreeRange;
    }

    public Atlas getSidewalkSharingLocation()
    {
        return this.sidewalkSharingLocation;
    }

    public Atlas getValidSidewalkBothSide()
    {
        return this.validSidewalkBothSide;
    }

    public Atlas getValidSidewalkBothSideAlternativeMapping()
    {
        return this.validSidewalkBothSideAlternativeMapping;
    }

    public Atlas getValidSidewalkLeftSide()
    {
        return this.validSidewalkLeftSide;
    }

    public Atlas getValidSidewalkLeftSideAlternativeMapping()
    {
        return this.validSidewalkLeftSideAlternativeMapping;
    }

    public Atlas getValidSidewalkRightSide()
    {
        return this.validSidewalkRightSide;
    }

    public Atlas getValidSidewalkRightSideAlternativeMapping()
    {
        return this.validSidewalkRightSideAlternativeMapping;
    }
}
