package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

import org.openstreetmap.atlas.checks.base.checks.BaseTestRule;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test Atlases for {@link TunnelBridgeHeightLimitCheckTest}
 *
 * @author ladwlo
 */
public class TunnelBridgeHeightLimitCheckTestRule extends BaseTestRule
{

    // bridge or standalone road/tunnel
    private static final String LOC_1 = "47.0,-122.1";
    private static final String LOC_2 = "47.2,-122.1";
    // crossing edge
    private static final String LOC_3 = "47.1,-122.0";
    private static final String LOC_4 = "47.1,-122.2";
    // edge with multiple crossings; way split into two edges
    private static final String LOC_5 = "47.0,-122.0";
    private static final String LOC_6 = "47.1,-122.2";
    private static final String LOC_7 = "47.2,-122.0";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_1)),
                    @Node(coordinates = @Loc(value = LOC_2)) },
            // edges
            edges = { @Edge(
                    // master
                    id = "1000000001", coordinates = { @Loc(value = LOC_1),
                            @Loc(value = LOC_2) }, tags = { "highway=primary", "tunnel=yes" }),
                    @Edge(
                            // reversed
                            id = "-1000000001", coordinates = { @Loc(value = LOC_2),
                                    @Loc(value = LOC_1) }, tags = { "highway=primary",
                                            "tunnel=yes" }) })
    // positive case; should only flag master edge
    private Atlas bidirectionalTunnelWithoutMaxHeight;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_1)),
                    @Node(coordinates = @Loc(value = LOC_2)),
                    @Node(coordinates = @Loc(value = LOC_5)),
                    @Node(coordinates = @Loc(value = LOC_7)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_1),
                            @Loc(value = LOC_2) }, tags = { "highway=primary", "bridge=yes" }),
                    @Edge(id = "2000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_7) }, tags = { "highway=primary" }) })
    // negative case: no edges with violations
    private Atlas bridgeWithNoCrossingRoads;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_5)),
                    @Node(coordinates = @Loc(value = LOC_6)),
                    @Node(coordinates = @Loc(value = LOC_7)) },
            // edges
            edges = { @Edge(
                    // first part of way #1
                    id = "1000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_6) }, tags = { "highway=primary", "covered=yes" }),
                    @Edge(
                            // second part of way #1
                            id = "1000000002", coordinates = { @Loc(value = LOC_6),
                                    @Loc(value = LOC_7) }, tags = { "highway=primary",
                                            "covered=yes" }) })
    // positive case; should only flag given OSM ID once
    private Atlas coveredRoadWithoutMaxHeightSplitIntoTwoEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_1)),
                    @Node(coordinates = @Loc(value = LOC_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_1),
                    @Loc(value = LOC_2) }, tags = { "highway=tertiary", "tunnel=yes" }) })
    // negative case: highway class does not match
    private Atlas lowClassTunnelWithoutMaxHeight;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_1)),
                    @Node(coordinates = @Loc(value = LOC_2)),
                    @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)),
                    @Node(coordinates = @Loc(value = LOC_5)),
                    @Node(coordinates = @Loc(value = LOC_6)),
                    @Node(coordinates = @Loc(value = LOC_7)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_1),
                            @Loc(value = LOC_2) }, tags = { "highway=primary", "bridge=yes" }),
                    // negative case: edge only touches the bridge (at one end)
                    @Edge(id = "2000000001", coordinates = { @Loc(value = LOC_1),
                            @Loc(value = LOC_5) }, tags = { "highway=primary" }),
                    // negative case: not a master edge
                    @Edge(id = "-2000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_1) }, tags = { "highway=primary" }),
                    // negative case: edge only touches the bridge (at the other end)
                    @Edge(id = "3000000001", coordinates = { @Loc(value = LOC_2),
                            @Loc(value = LOC_7) }, tags = { "highway=primary" }),
                    // positive case: edge crosses the bridge
                    @Edge(id = "4000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_6),
                            @Loc(value = LOC_7) }, tags = { "highway=primary" }),
                    // negative case: edge with the same OSM ID already flagged
                    @Edge(id = "4000000002", coordinates = { @Loc(value = LOC_7),
                            @Loc(value = LOC_6),
                            @Loc(value = LOC_5) }, tags = { "highway=primary" }),
                    // negative case: edge crossed the bridge but has a low class
                    @Edge(id = "5000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_6) }, tags = { "highway=tertiary" }),
                    // negative case: edge crosses the bridge but maxheight tag is present
                    @Edge(id = "6000000001", coordinates = { @Loc(value = LOC_6),
                            @Loc(value = LOC_7) }, tags = { "highway=primary", "maxheight=3.5" }) })
    // positive, should only flag one edge that matches all conditions
    private Atlas roadsPassingUnderBridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_5)),
                    @Node(coordinates = @Loc(value = LOC_6)),
                    @Node(coordinates = @Loc(value = LOC_7)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_6) }, tags = { "highway=primary", "tunnel=yes",
                                    "maxheight=4.0" }),
                    @Edge(id = "2000000001", coordinates = { @Loc(value = LOC_6),
                            @Loc(value = LOC_7) }, tags = { "highway=primary", "covered=yes",
                                    "maxheight:physical=3.8" }) })
    // negative case: has maxheight
    private Atlas tunnelAndCoveredRoadWithMaxHeight;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_5)),
                    @Node(coordinates = @Loc(value = LOC_6)),
                    @Node(coordinates = @Loc(value = LOC_7)) },
            // edges - two cases: one with covered=no and another without any covered tag
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_5),
                            @Loc(value = LOC_6) }, tags = { "highway=primary", "covered=no" }),
                    @Edge(id = "2000000001", coordinates = { @Loc(value = LOC_6),
                            @Loc(value = LOC_7) }, tags = { "highway=primary" }) })
    // negative case: is not covered
    private Atlas uncoveredRoadWithoutMaxHeight;

    public Atlas getBidirectionalTunnelWithoutMaxHeight()
    {
        return this.bidirectionalTunnelWithoutMaxHeight;
    }

    public Atlas getBridgeWithNoCrossingRoads()
    {
        return this.bridgeWithNoCrossingRoads;
    }

    public Atlas getCoveredRoadWithoutMaxHeightSplitIntoTwoEdges()
    {
        return this.coveredRoadWithoutMaxHeightSplitIntoTwoEdges;
    }

    public Atlas getLowClassTunnelWithoutMaxHeight()
    {
        return this.lowClassTunnelWithoutMaxHeight;
    }

    public Atlas getRoadsPassingUnderBridge()
    {
        return this.roadsPassingUnderBridge;
    }

    public Atlas getTunnelAndCoveredRoadWithMaxHeight()
    {
        return this.tunnelAndCoveredRoadWithMaxHeight;
    }

    public Atlas getUncoveredRoadWithoutMaxHeight()
    {
        return this.uncoveredRoadWithoutMaxHeight;
    }
}
