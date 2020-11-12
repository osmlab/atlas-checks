package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RoundaboutMissingTagCheckTest} data generator
 *
 * @author vladlemberg
 */

public class RoundaboutMissingTagCheckTestRule extends CoreTestRule
{
    // roundabout nodes
    private static final String TEST_NODE_VERTEX_1 = "37.3293541,-121.9156701";
    private static final String TEST_NODE_SHP_1 = "37.3293187,-121.9157123";
    private static final String TEST_NODE_SHP_2 = "37.3292841,-121.9157281";
    private static final String TEST_NODE_SHP_3 = "37.3292474,-121.9157280";
    private static final String TEST_NODE_VERTEX_2 = "37.3292026,-121.9157035";
    private static final String TEST_NODE_SHP_4 = "37.3291717,-121.9156569";
    private static final String TEST_NODE_SHP_5 = "37.3291612,-121.9156132";
    private static final String TEST_NODE_SHP_6 = "37.3291632,-121.9155676";
    private static final String TEST_NODE_VERTEX_3 = "37.3291845,-121.9155131";
    private static final String TEST_NODE_SHP_7 = "37.3292123,-121.9154833";
    private static final String TEST_NODE_SHP_8 = "37.3292464,-121.9154672";
    private static final String TEST_NODE_SHP_9 = "37.3292949,-121.9154700";
    private static final String TEST_NODE_VERTEX_4 = "37.3293277,-121.9154900";
    private static final String TEST_NODE_SHP_10 = "37.3293534,-121.9155234";
    private static final String TEST_NODE_SHP_11 = "37.3293683,-121.9155658";
    private static final String TEST_NODE_SHP_12 = "37.3293686,-121.9156274";
    private static final String TEST_NODE_VERTEX_5 = "37.329273,-121.915736";
    // connected way nodes
    private static final String TEST_NODE_1 = "37.3303971,-121.9139893";
    private static final String TEST_NODE_2 = "37.3281316,-121.9170493";
    private static final String TEST_NODE_3 = "37.3299230,-121.9163520";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas closedWayRoundShape;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas closedWayMalformedShape;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)), },
            // edges
            edges = { @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                    @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary",
                            "junction=roundabout" }), })
    private Atlas edgeWithRoundaboutTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)), },
            // edges
            edges = { @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                    @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary",
                            "area=yes" }), })
    private Atlas edgeWithAreaTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)), },
            // edges
            edges = {
                    @Edge(id = "-12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }), })
    private Atlas reversedEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)), },
            // edges
            edges = {
                    @Edge(id = "2234", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }),
                    @Edge(id = "2235", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2236", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }), })
    private Atlas unClosedWay;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    // connected not navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=footway" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=path" }),

            })
    private Atlas closedWayNoIntersectionsWithNavigableEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_3)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_VERTEX_5) }, tags = { "highway=primary" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_5),
                            @Loc(value = TEST_NODE_SHP_3), @Loc(value = TEST_NODE_SHP_4),
                            @Loc(value = TEST_NODE_SHP_5), @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3), @Loc(value = TEST_NODE_SHP_7),
                            @Loc(value = TEST_NODE_SHP_8), @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4), @Loc(value = TEST_NODE_SHP_10),
                            @Loc(value = TEST_NODE_SHP_11), @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    // connected navigable edges
                    @Edge(id = "52340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_5),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "62340001", coordinates = { @Loc(value = TEST_NODE_3),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=secondary" }),

            })
    private Atlas turnLoop;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary",
                                    "foot=yes" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary",
                                    "foot=yes" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary",
                                    "foot=yes" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary",
                                    "foot=yes" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas tagFilterTestFootYes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary",
                                    "footway=sidewalk" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary",
                                    "footway=sidewalk" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary",
                                    "footway=sidewalk" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary",
                                    "footway=sidewalk" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas tagFilterTestFootwayTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=residential",
                                    "motor_vehicle=no" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=residential",
                                    "motor_vehicle=no" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=residential",
                                    "motor_vehicle=no" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=residential",
                                    "motor_vehicle=no" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas tagFilterTestMotorVehicleNo;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=residential",
                                    "access=private" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=residential",
                                    "access=private" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=residential",
                                    "access=private" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=residential",
                                    "access=private" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas tagFilterTestAccessPrivate;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_VERTEX_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_2)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_3)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_4)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_5)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_6)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_7)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_8)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_9)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_10)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_11)),
                    @Node(coordinates = @Loc(value = TEST_NODE_SHP_12)),
                    @Node(coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(coordinates = @Loc(value = TEST_NODE_2)), },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "12340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_1),
                            @Loc(value = TEST_NODE_SHP_1), @Loc(value = TEST_NODE_SHP_2),
                            @Loc(value = TEST_NODE_SHP_3),
                            @Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=residential",
                                    "construction=any" }),
                    @Edge(id = "12340002", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_SHP_4), @Loc(value = TEST_NODE_SHP_5),
                            @Loc(value = TEST_NODE_SHP_6),
                            @Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=residential",
                                    "construction=any" }),
                    @Edge(id = "12340003", coordinates = { @Loc(value = TEST_NODE_VERTEX_3),
                            @Loc(value = TEST_NODE_SHP_7), @Loc(value = TEST_NODE_SHP_8),
                            @Loc(value = TEST_NODE_SHP_9),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=residential",
                                    "construction=any" }),
                    @Edge(id = "12340004", coordinates = { @Loc(value = TEST_NODE_VERTEX_4),
                            @Loc(value = TEST_NODE_SHP_10), @Loc(value = TEST_NODE_SHP_11),
                            @Loc(value = TEST_NODE_SHP_12),
                            @Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=residential",
                                    "construction=any" }),
                    // connected navigable edges
                    @Edge(id = "32340001", coordinates = { @Loc(value = TEST_NODE_VERTEX_2),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "42340001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas tagFilterTestConstruction;

    public Atlas closedWayMalformedShape()
    {
        return this.closedWayMalformedShape;
    }

    public Atlas closedWayNoIntersectionsWithNavigableEdges()
    {
        return this.closedWayNoIntersectionsWithNavigableEdges;
    }

    public Atlas closedWayRoundShape()
    {
        return this.closedWayRoundShape;
    }

    public Atlas edgeWithAreaTag()
    {
        return this.edgeWithAreaTag;
    }

    public Atlas edgeWithRoundaboutTag()
    {
        return this.edgeWithRoundaboutTag;
    }

    public Atlas reversedEdge()
    {
        return this.reversedEdge;
    }

    public Atlas tagFilterTestAccessPrivate()
    {
        return this.tagFilterTestAccessPrivate;
    }

    public Atlas tagFilterTestConstruction()
    {
        return this.tagFilterTestConstruction;
    }

    public Atlas tagFilterTestFootYes()
    {
        return this.tagFilterTestFootYes;
    }

    public Atlas tagFilterTestFootwayTag()
    {
        return this.tagFilterTestFootwayTag;
    }

    public Atlas tagFilterTestMotorVehicleNo()
    {
        return this.tagFilterTestMotorVehicleNo;
    }

    public Atlas turnLoop()
    {
        return this.turnLoop;
    }

    public Atlas unClosedWay()
    {
        return this.unClosedWay;
    }
}
