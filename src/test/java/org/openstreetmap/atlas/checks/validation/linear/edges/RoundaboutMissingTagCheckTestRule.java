package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link RoundaboutMissingTagCheckTest} data generator
 *
 * @author vladlemberg
 */

public class RoundaboutMissingTagCheckTestRule extends CoreTestRule {

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
    private static final String TEST_NODE_5 = "37.3303971,-121.9139893";
    private static final String TEST_NODE_6 = "37.3281316,-121.9170493";

    @TestAtlas(
            // nodes
            nodes = {
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_6)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_7)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_8)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_9)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_10)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_11)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_SHP_12)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_6)),
            },
            // edges
            edges = {
                    //parts of roundabout
                    @TestAtlas.Edge(id = "12340001", coordinates = {
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_1),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_1),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_2),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_3),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_2) },
                            tags = {"highway=primary"}),
                    @TestAtlas.Edge(id = "12340002", coordinates = {
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_4),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_5),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_6),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_3) },
                            tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340003", coordinates = {
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_3),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_7),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_8),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_9),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) },
                            tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340004", coordinates = {
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_10),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_11),
                            @TestAtlas.Loc(value = TEST_NODE_SHP_12),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_1) },
                            tags = { "highway=primary" }),
                    //connected navigable edges
                    @TestAtlas.Edge(id = "32340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_6) }, tags = { "highway=tertiary" }),
                    @TestAtlas.Edge(id = "42340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_5),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas closedWayRoundShape;

    @TestAtlas(
            // nodes
            nodes = {
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_6)),
            },
            // edges
            edges = {
                    //parts of roundabout
                    @TestAtlas.Edge(id = "12340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_1),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_2) }, tags = {"highway=primary"}),
                    @TestAtlas.Edge(id = "12340002", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340003", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_3),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340004", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_4),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    //connected navigable edges
                    @TestAtlas.Edge(id = "32340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_6) }, tags = { "highway=tertiary" }),
                    @TestAtlas.Edge(id = "42340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_5),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=secondary" }),

            })
    private Atlas closedWayMalformedShape;

    @TestAtlas(
            // nodes
            nodes = {
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_4)),
            },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "2234", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_1),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_2) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "2235", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "2236", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_3),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
            })
    private Atlas unClosedWay;
    @TestAtlas(
            // nodes
            nodes = {
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_VERTEX_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_NODE_6)),
            },
            // edges
            edges = {
                    //parts of roundabout
                    @TestAtlas.Edge(id = "12340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_1),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_2) }, tags = {"highway=primary"}),
                    @TestAtlas.Edge(id = "12340002", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_3) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340003", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_3),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "12340004", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_4),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_1) }, tags = { "highway=primary" }),
                    //connected not navigable edges
                    @TestAtlas.Edge(id = "32340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_VERTEX_2),
                            @TestAtlas.Loc(value = TEST_NODE_6) }, tags = { "highway=footway" }),
                    @TestAtlas.Edge(id = "42340001", coordinates = { @TestAtlas.Loc(value = TEST_NODE_5),
                            @TestAtlas.Loc(value = TEST_NODE_VERTEX_4) }, tags = { "highway=path" }),

            })
    private Atlas closedWayNoIntersectionsWithNavigableEdges;


    public Atlas closedWayRoundShape() {
        return this.closedWayRoundShape;
    }

    public Atlas unClosedWay() {
        return this.unClosedWay;
    }

    public Atlas closedWayNoIntersectionsWithNavigableEdges() {
        return this.closedWayNoIntersectionsWithNavigableEdges;
    }

    public Atlas ClosedWayMalformedShape() {
        return this.closedWayMalformedShape;
    }
}
