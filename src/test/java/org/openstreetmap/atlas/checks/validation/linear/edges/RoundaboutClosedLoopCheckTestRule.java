package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RoundaboutClosedLoopCheckTest} data generator
 *
 * @author mkalender
 */
public class RoundaboutClosedLoopCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.32544,-122.033948";
    private static final String TEST_3 = "37.33531,-122.009566";
    private static final String TEST_4 = "37.390535,-122.031007";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }) })
    private Atlas nonRoundaboutEdgeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                    "junction=roundabout", "oneway=yes" }) })
    private Atlas mainRoundaboutEdgeWithValence1NodesAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }) })
    private Atlas mainRoundaboutEdgesWithValence2NodesAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=mini_roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=mini_roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "highway=mini_roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "highway=mini_roundabout" }) })
    private Atlas mainRoundaboutEdgesWithMiniRoundaboutTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=turning_circle" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=turning_circle" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "highway=turning_circle" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "highway=turning_circle" }) })
    private Atlas mainRoundaboutEdgesWithTurningCircleTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=turning_loop" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=turning_loop" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "highway=turning_loop" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "highway=turning_loop" }) })
    private Atlas mainRoundaboutEdgesWithTurningLoopTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "traffic_calming=island" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "traffic_calming=island" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "traffic_calming=island" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "traffic_calming=island" }) })
    private Atlas mainRoundaboutEdgesWithTrafficCalmingTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1234567891000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2234567891000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }) })
    private Atlas mainRoundaboutEdgesWithDeadEndNodesAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(id = "10", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "-10", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }) })
    private Atlas mainRoundaboutEdgesWithABidirectionalRoadAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }) })
    private Atlas mainRoundaboutEdgesWithOneMissingOneWayTagAtlas;

    public Atlas mainRoundaboutEdgeWithValence1NodesAtlas()
    {
        return this.mainRoundaboutEdgeWithValence1NodesAtlas;
    }

    public Atlas mainRoundaboutEdgesWithABidirectionalRoadAtlas()
    {
        return this.mainRoundaboutEdgesWithABidirectionalRoadAtlas;
    }

    public Atlas mainRoundaboutEdgesWithDeadEndNodesAtlas()
    {
        return this.mainRoundaboutEdgesWithDeadEndNodesAtlas;
    }

    public Atlas mainRoundaboutEdgesWithMiniRoundaboutTagAtlas()
    {
        return this.mainRoundaboutEdgesWithMiniRoundaboutTagAtlas;
    }

    public Atlas mainRoundaboutEdgesWithOneMissingOneWayTagAtlas()
    {
        return this.mainRoundaboutEdgesWithOneMissingOneWayTagAtlas;
    }

    public Atlas mainRoundaboutEdgesWithTrafficCalmingTagAtlas()
    {
        return this.mainRoundaboutEdgesWithTrafficCalmingTagAtlas;
    }

    public Atlas mainRoundaboutEdgesWithTurningCircleTagAtlas()
    {
        return this.mainRoundaboutEdgesWithTurningCircleTagAtlas;
    }

    public Atlas mainRoundaboutEdgesWithTurningLoopTagAtlas()
    {
        return this.mainRoundaboutEdgesWithTurningLoopTagAtlas;
    }

    public Atlas mainRoundaboutEdgesWithValence2NodesAtlas()
    {
        return this.mainRoundaboutEdgesWithValence2NodesAtlas;
    }

    public Atlas nonRoundaboutEdgeAtlas()
    {
        return this.nonRoundaboutEdgeAtlas;
    }
}
