package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Unit test rule for {@link RoundaboutConnectorCheck}.
 *
 * @author bbreithaupt
 */
public class RoundaboutConnectorCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "48.8734501809537,2.29620290325";
    private static final String TEST_2 = "48.8732729738021,2.29564420261152";
    private static final String TEST_3 = "48.8734016822167,2.29584839776873";
    private static final String TEST_4 = "48.8735136023081,2.29596751161044";
    private static final String TEST_5 = "48.8736329834629,2.29601572435589";
    private static final String TEST_6 = "48.8738176506256,2.29606677314519";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=primary" }) })
    private Atlas validOneWayConnectorsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3),
                    @Loc(value = TEST_4) }, tags = { "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_4) }, tags = { "highway=primary" }) })
    private Atlas validTwoWayConnectorsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }) })
    private Atlas invalidOneWayConnectorsReversedAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=primary" }) })
    private Atlas invalidTwoWayConnectorAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=primary" }) })
    private Atlas invalidTwoWayConnectorReversedAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_2) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_4),
                            @Loc(value = TEST_3) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }) })
    private Atlas validOneWayConnectorsLeftAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_3),
                    @Loc(value = TEST_2) }, tags = { "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_6), @Loc(value = TEST_5),
                            @Loc(value = TEST_4) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_4) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }) })
    private Atlas validTwoWayConnectorsLeftAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_2) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_4),
                            @Loc(value = TEST_3) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=primary" }) })
    private Atlas invalidOneWayConnectorsReversedLeftAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_2) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_4),
                            @Loc(value = TEST_3) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }) })
    private Atlas invalidTwoWayConnectorLeftAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_2) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_4),
                            @Loc(value = TEST_3) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                            "highway=primary", "junction=roundabout" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }, tags = { "highway=primary" }) })
    private Atlas invalidTwoWayConnectorReversedLeftAtlas;

    public Atlas invalidOneWayConnectorsReversedAtlas()
    {
        return this.invalidOneWayConnectorsReversedAtlas;
    }

    public Atlas invalidOneWayConnectorsReversedLeftAtlas()
    {
        return this.invalidOneWayConnectorsReversedLeftAtlas;
    }

    public Atlas invalidTwoWayConnectorAtlas()
    {
        return this.invalidTwoWayConnectorAtlas;
    }

    public Atlas invalidTwoWayConnectorLeftAtlas()
    {
        return this.invalidTwoWayConnectorLeftAtlas;
    }

    public Atlas invalidTwoWayConnectorReversedAtlas()
    {
        return this.invalidTwoWayConnectorReversedAtlas;
    }

    public Atlas invalidTwoWayConnectorReversedLeftAtlas()
    {
        return this.invalidTwoWayConnectorReversedLeftAtlas;
    }

    public Atlas validOneWayConnectorsAtlas()
    {
        return this.validOneWayConnectorsAtlas;
    }

    public Atlas validOneWayConnectorsLeftAtlas()
    {
        return this.validOneWayConnectorsLeftAtlas;
    }

    public Atlas validTwoWayConnectorsAtlas()
    {
        return this.validTwoWayConnectorsAtlas;
    }

    public Atlas validTwoWayConnectorsLeftAtlas()
    {
        return this.validTwoWayConnectorsLeftAtlas;
    }
}
