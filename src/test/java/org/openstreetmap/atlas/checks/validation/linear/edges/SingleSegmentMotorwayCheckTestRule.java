package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Unit test rules for {@link SingleSegmentMotorwayCheck}.
 *
 * @author bbreithaupt
 */
public class SingleSegmentMotorwayCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "48.1033298347647,-122.794643805939";
    private static final String TEST_2 = "48.1034825739679,-122.796334374533";
    private static final String TEST_3 = "48.1033862819144,-122.798144277382";
    private static final String TEST_4 = "48.1032534649929,-122.79873100413";
    private static final String TEST_5 = "48.1034327677559,-122.798775754475";
    private static final String TEST_6 = "48.1032899896805,-122.79916856306";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "highway=motorway" }) })
    private Atlas validMotorwaySegmentsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway" }),
                    @Edge(id = "-2000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "highway=primary" }) })
    private Atlas invalidPrimaryMotorwayPrimarySegmentAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=motorway" }) })
    private Atlas invalidMotorwaySegmentOneConnectionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2), tags = {
                            "synthetic_boundary_node=yes" }),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=motorway" }) })
    private Atlas validMotorwaySegmentOneConnectionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_5) }, tags = {
                            "highway=motorway", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                            "highway=primary", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_6), @Loc(value = TEST_4) }, tags = {
                                    "highway=motorway", "oneway=yes", "junction=roundabout" }), })
    private Atlas invalidMotorwaySegmentOneConnectionRoundaboutAtlas;

    public Atlas invalidMotorwaySegmentOneConnectionAtlas()
    {
        return this.invalidMotorwaySegmentOneConnectionAtlas;
    }

    public Atlas invalidMotorwaySegmentOneConnectionRoundaboutAtlas()
    {
        return this.invalidMotorwaySegmentOneConnectionRoundaboutAtlas;
    }

    public Atlas invalidPrimaryMotorwayPrimarySegmentAtlas()
    {
        return this.invalidPrimaryMotorwayPrimarySegmentAtlas;
    }

    public Atlas validMotorwaySegmentOneConnectionAtlas()
    {
        return this.validMotorwaySegmentOneConnectionAtlas;
    }

    public Atlas validMotorwaySegmentsAtlas()
    {
        return this.validMotorwaySegmentsAtlas;
    }
}
