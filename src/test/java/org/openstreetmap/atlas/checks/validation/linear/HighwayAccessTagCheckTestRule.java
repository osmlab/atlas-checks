package org.openstreetmap.atlas.checks.validation.linear;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link HighwayAccessTagCheck}
 *
 * @author bbreithaupt
 */

public class HighwayAccessTagCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "0.0,0.0";
    private static final String TEST_2 = "0.1,0.1";
    private static final String TEST_3 = "0.0,0.2";
    private static final String TEST_4 = "0.1,0.3";
    private static final String TEST_5 = "0.0,0.4";
    private static final String TEST_6 = "-0.1,0.5";
    private static final String TEST_7 = "0.0,0.6";
    private static final String TEST_8 = "0.0,0.7";
    private static final String TEST_9 = "0.1,0.8";
    private static final String TEST_10 = "0.0,0.9";

    // In Highway Tests
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
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = { @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayLines;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // lines
            lines = { @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4),
                    @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }) })
    private Atlas accessNoInHighwayEdgeLineEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = { @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // edges
            edges = { @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4),
                    @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }) })
    private Atlas accessNoInHighwayLineEdgeLine;

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
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=track" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=track", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=track" }) })
    private Atlas accessNoInHighwayEdgesTrack;

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
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "access=no", "public_transport=yes" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesPublicTransportYes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
                    @Node(coordinates = @Loc(value = TEST_9)),
                    @Node(coordinates = @Loc(value = TEST_10)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1001000002", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_9),
                            @Loc(value = TEST_10) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesSameFeature;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
                    @Node(coordinates = @Loc(value = TEST_9)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1001000002", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_9),
                            @Loc(value = TEST_1) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesSameFeatureSquare;

    // Start Highway Tests
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
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoStartHighwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = {
                    @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "access=no" }),
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoStartHighwayLines;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // lines
            lines = { @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = { "highway=motorway" }) })
    private Atlas accessNoStartHighwayEdgeLineEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = {
                    @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "access=no" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // edges
            edges = { @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = { "highway=motorway" }) })
    private Atlas accessNoStartHighwayLineEdgeLine;

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
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3) }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesNonHighwayConnected;

    // Private in Highway Tests
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
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "access=private" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessPrivateInHighwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = { @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "access=private" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessPrivateInHighwayLines;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
                    @Node(coordinates = @Loc(value = TEST_9)),
                    @Node(coordinates = @Loc(value = TEST_10)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_9),
                            @Loc(value = TEST_10) }, tags = { "highway=motorway" }) })
    private Atlas accessNoStartHighwayEdgesSameFeature;

    // Private in Highway With Gates Tests
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4), tags = { "barrier=gate" }),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "access=private" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessPrivateGateInHighwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4), tags = { "barrier=gate" }),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // lines
            lines = { @Line(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                                    "highway=motorway", "access=private" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessPrivateGateInHighwayLines;

    // In Highway Tests
    public Atlas accessNoInHighwayEdges()
    {
        return this.accessNoInHighwayEdges;
    }

    public Atlas accessNoInHighwayLines()
    {
        return this.accessNoInHighwayLines;
    }

    public Atlas accessNoInHighwayEdgeLineEdge()
    {
        return this.accessNoInHighwayEdgeLineEdge;
    }

    public Atlas accessNoInHighwayLineEdgeLine()
    {
        return this.accessNoInHighwayLineEdgeLine;
    }

    public Atlas accessNoInHighwayEdgesTrack()
    {
        return this.accessNoInHighwayEdgesTrack;
    }

    public Atlas accessNoInHighwayEdgesPublicTransportYes()
    {
        return this.accessNoInHighwayEdgesPublicTransportYes;
    }

    public Atlas accessNoInHighwayEdgesSameFeature()
    {
        return this.accessNoInHighwayEdgesSameFeature;
    }

    public Atlas accessNoInHighwayEdgesSameFeatureSquare()
    {
        return this.accessNoInHighwayEdgesSameFeatureSquare;
    }

    // Start Highway Tests
    public Atlas accessNoStartHighwayEdges()
    {
        return this.accessNoStartHighwayEdges;
    }

    public Atlas accessNoStartHighwayLines()
    {
        return this.accessNoStartHighwayLines;
    }

    public Atlas accessNoStartHighwayEdgeLineEdge()
    {
        return this.accessNoStartHighwayEdgeLineEdge;
    }

    public Atlas accessNoStartHighwayLineEdgeLine()
    {
        return this.accessNoStartHighwayLineEdgeLine;
    }

    public Atlas accessNoInHighwayEdgesNonHighwayConnected()
    {
        return this.accessNoInHighwayEdgesNonHighwayConnected;
    }

    public Atlas accessNoStartHighwayEdgesSameFeature()
    {
        return this.accessNoStartHighwayEdgesSameFeature;
    }

    // Private in Highway Tests
    public Atlas accessPrivateInHighwayEdges()
    {
        return this.accessPrivateInHighwayEdges;
    }

    public Atlas accessPrivateInHighwayLines()
    {
        return this.accessPrivateInHighwayLines;
    }

    // Private in Highway With Gates Tests
    public Atlas accessPrivateGateInHighwayEdges()
    {
        return this.accessPrivateGateInHighwayEdges;
    }

    public Atlas accessPrivateGateInHighwayLines()
    {
        return this.accessPrivateGateInHighwayLines;
    }

}
