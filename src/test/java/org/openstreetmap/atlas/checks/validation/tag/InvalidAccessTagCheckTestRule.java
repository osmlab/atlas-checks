package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Tests for {@link InvalidAccessTagCheck}
 *
 * @author bbreithaupt
 */

public class InvalidAccessTagCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";
    private static final String TEST_4 = "47.2138114677627,-122.440990166979";
    private static final String TEST_5 = "47.2136200921786,-122.44001973284";
    private static final String TEST_6 = "47.2135137721113,-122.439127559518";
    private static final String TEST_7 = "47.2136200921786,-122.438157125378";
    private static final String TEST_8 = "47.2136413561665,-122.437468430183";
    private static final String TEST_9 = "47.2137689399148,-122.436717126333";
    private static final String TEST_10 = "47.2136413561665,-122.436028431137";
    private static final String TEST_11 = "47.2141623212065,-122.443729295599";
    private static final String TEST_12 = "47.2132054427106,-122.44382320858";
    private static final String TEST_13 = "47.2132267068647,-122.435339735941";
    private static final String TEST_14 = "47.2142154806167,-122.435355388105";

    // In Highway Tests
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
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
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
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
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
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
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
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
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
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
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
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)) },
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
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_11), @Loc(value = TEST_12),
                    @Loc(value = TEST_13),
                    @Loc(value = TEST_14) }, tags = { "landuse=military" }) })
    private Atlas accessNoInHighwayEdgesLanduseMilitary;

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
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // areas
            lines = {
                    @Line(id = "1000", coordinates = { @Loc(value = TEST_11), @Loc(value = TEST_12),
                            @Loc(value = TEST_13), @Loc(value = TEST_5), @Loc(value = TEST_11) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "1000", type = "line", role = "outer") }, tags = {
                            "landuse=military", "type=multipolygon" }) })
    private Atlas accessNoInHighwayEdgesLanduseMilitaryRelation;

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
                            @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "access=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // areas
            areas = { @Area(id = "1000", coordinates = { @Loc(value = TEST_11),
                    @Loc(value = TEST_12), @Loc(value = TEST_13), @Loc(value = TEST_14),
                    @Loc(value = TEST_11) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "1000", type = "area", role = "na") }) })
    private Atlas accessNoInHighwayEdgesInRelation;

    // Config Tests
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
                                    "highway=motorway", "access=no", "vehicle=no" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesVehicleNo;

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
                                    "highway=motorway", "access=no", "public_transport=yes" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas accessNoInHighwayEdgesPublicTransportYes;

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

    public Atlas accessNoInHighwayEdgesSameFeature()
    {
        return this.accessNoInHighwayEdgesSameFeature;
    }

    public Atlas accessNoInHighwayEdgesSameFeatureSquare()
    {
        return this.accessNoInHighwayEdgesSameFeatureSquare;
    }

    public Atlas getAccessNoInHighwayEdgesLanduseMilitary()
    {
        return this.accessNoInHighwayEdgesLanduseMilitary;
    }

    public Atlas getAccessNoInHighwayEdgesLanduseMilitaryRelation()
    {
        return this.accessNoInHighwayEdgesLanduseMilitaryRelation;
    }

    public Atlas getAccessNoInHighwayEdgesInRelation()
    {
        return this.accessNoInHighwayEdgesInRelation;
    }

    // Config Tests
    public Atlas accessNoInHighwayEdgesVehicleNo()
    {
        return this.accessNoInHighwayEdgesVehicleNo;
    }

    public Atlas accessNoInHighwayEdgesPublicTransportYes()
    {
        return this.accessNoInHighwayEdgesPublicTransportYes;
    }

}
