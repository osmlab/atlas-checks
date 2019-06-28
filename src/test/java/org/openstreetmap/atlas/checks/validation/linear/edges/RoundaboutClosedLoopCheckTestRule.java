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
    private Atlas masterRoundaboutEdgeWithValence1NodesAtlas;

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
    private Atlas masterRoundaboutEdgesWithValence2NodesAtlas;

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
    private Atlas masterRoundaboutEdgesWithMiniRoundaboutTagAtlas;

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
    private Atlas masterRoundaboutEdgesWithTurningCircleTagAtlas;

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
    private Atlas masterRoundaboutEdgesWithTurningLoopTagAtlas;

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
    private Atlas masterRoundaboutEdgesWithTrafficCalmingTagAtlas;

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
    private Atlas masterRoundaboutEdgesWithDeadEndNodesAtlas;

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
    private Atlas masterRoundaboutEdgesWithABidirectionalRoadAtlas;

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
    private Atlas masterRoundaboutEdgesWithOneMissingOneWayTagAtlas;

    public Atlas masterRoundaboutEdgeWithValence1NodesAtlas()
    {
        return this.masterRoundaboutEdgeWithValence1NodesAtlas;
    }

    public Atlas masterRoundaboutEdgesWithABidirectionalRoadAtlas()
    {
        return this.masterRoundaboutEdgesWithABidirectionalRoadAtlas;
    }

    public Atlas masterRoundaboutEdgesWithDeadEndNodesAtlas()
    {
        return this.masterRoundaboutEdgesWithDeadEndNodesAtlas;
    }

    public Atlas masterRoundaboutEdgesWithMiniRoundaboutTagAtlas()
    {
        return this.masterRoundaboutEdgesWithMiniRoundaboutTagAtlas;
    }

    public Atlas masterRoundaboutEdgesWithOneMissingOneWayTagAtlas()
    {
        return this.masterRoundaboutEdgesWithOneMissingOneWayTagAtlas;
    }

    public Atlas masterRoundaboutEdgesWithTrafficCalmingTagAtlas()
    {
        return this.masterRoundaboutEdgesWithTrafficCalmingTagAtlas;
    }

    public Atlas masterRoundaboutEdgesWithTurningCircleTagAtlas()
    {
        return this.masterRoundaboutEdgesWithTurningCircleTagAtlas;
    }

    public Atlas masterRoundaboutEdgesWithTurningLoopTagAtlas()
    {
        return this.masterRoundaboutEdgesWithTurningLoopTagAtlas;
    }

    public Atlas masterRoundaboutEdgesWithValence2NodesAtlas()
    {
        return this.masterRoundaboutEdgesWithValence2NodesAtlas;
    }

    public Atlas nonRoundaboutEdgeAtlas()
    {
        return this.nonRoundaboutEdgeAtlas;
    }
}
