package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link ConnectivityCheck}
 *
 * @author bbreithaupt
 */
public class ConnectivityCheckTestRule extends CoreTestRule
{
    private static final String TEST1 = "47.2620768463041,-122.474848242369";
    private static final String TEST2 = "47.2618703753848,-122.474832590205";
    private static final String TEST3 = "47.26186971149,-122.474824764123";
    private static final String TEST4 = "47.2620795018734,-122.474795416317";
    private static final String TEST5 = "47.2618624086451,-122.475082046561";
    private static final String TEST6 = "47.2618624086451,-122.474827698904";
    private static final String TEST7 = "47.2618624086451,-122.474591938191";
    private static final String TEST8 = "47.2617588409197,-122.474843351067";

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas invalidDisconnectedNodesAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(TEST1)),
                    @Node(id = "2000000", coordinates = @Loc(TEST2)),
                    @Node(id = "3000000", coordinates = @Loc(TEST3)),
                    @Node(id = "4000000", coordinates = @Loc(TEST4)),
                    @Node(id = "6000000", coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(id = "1234000000", coordinates = { @Loc(TEST1),
                                    @Loc(TEST2) }, tags = { "highway=secondary" }),
                            @Edge(id = "2345000000", coordinates = { @Loc(TEST3),
                                    @Loc(TEST4) }, tags = { "highway=secondary" }),
                            @Edge(id = "3456000000", coordinates = { @Loc(TEST2),
                                    @Loc(TEST6) }, tags = { "boundary=administrative" }) })
    private Atlas invalidDisconnectedNodesAtlasNavigableDeadEnd;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(TEST1)),
                    @Node(id = "2000000", coordinates = @Loc(TEST2)),
                    @Node(id = "3000000", coordinates = @Loc(TEST3)),
                    @Node(id = "4000000", coordinates = @Loc(TEST4)),
                    @Node(id = "6000000", coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(id = "1234000000", coordinates = { @Loc(TEST1),
                                    @Loc(TEST2) }, tags = { "highway=secondary" }),
                            @Edge(id = "-1234000000", coordinates = { @Loc(TEST1),
                                    @Loc(TEST2) }, tags = { "highway=secondary" }),
                            @Edge(id = "2345000000", coordinates = { @Loc(TEST3),
                                    @Loc(TEST4) }, tags = { "highway=secondary" }),
                            @Edge(id = "3456000000", coordinates = { @Loc(TEST2),
                                    @Loc(TEST6) }, tags = { "boundary=administrative" }) })
    private Atlas invalidDisconnectedNodesAtlasNavigableDeadEndReversible;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST4) }, tags = {
                                    "highway=secondary", "layer=1" }) })
    private Atlas validDisconnectedNodesLayerAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST4) }, tags = {
                                    "highway=secondary", "level=1" }) })
    private Atlas validDisconnectedNodesLevelAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }) })
    private Atlas invalidDisconnectedNodesOppositeAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)),
                    @Node(coordinates = @Loc(TEST2), tags = {
                            "synthetic_boundary_node=yes" }) }, edges = {
                                    @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                            "highway=secondary" }) })
    private Atlas validationForSyntheticNode;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validConnectedNodesAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST6)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST6) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST6), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validConnectedNodesAndSnapLocationsAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas invalidConnectedNodesAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST4)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST4), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validConnectedEdgesOppositeAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST2)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST7)), @Node(coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST7) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST7), @Loc(TEST6) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validConnectedEdges3Atlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST8)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST8), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas invalidDisconnectedEdgesAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST6) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validConnectedNodesLinearAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)),
                    @Node(coordinates = @Loc(TEST2), tags = { "barrier=toll_booth" }),
                    @Node(coordinates = @Loc(TEST8)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST8), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validDisconnectedEdgesBarrierAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)),
                    @Node(coordinates = @Loc(TEST2), tags = { "noexit=yes" }),
                    @Node(coordinates = @Loc(TEST8)), @Node(coordinates = @Loc(TEST4)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST2) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST8), @Loc(TEST4) }, tags = {
                                    "highway=secondary" }) })
    private Atlas validDisconnectedEdgesNoExitAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST5)),
                    @Node(coordinates = @Loc(TEST7)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST2) }, tags = {
                                    "highway=secondary", "layer=1" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "layer=1" }) })
    private Atlas validDisconnectedNodesCrossingLayerAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST5)),
                    @Node(coordinates = @Loc(TEST7)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST2) }, tags = {
                                    "highway=secondary", "tunnel=yes" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "tunnel=yes" }) })
    private Atlas validDisconnectedNodesCrossingTunnelAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST5)),
                    @Node(coordinates = @Loc(TEST7)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST2) }, tags = {
                                    "highway=secondary", "bridge=yes" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "bridge=yes" }) })
    private Atlas validDisconnectedNodesCrossingBridgeAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST2)),
                    @Node(coordinates = @Loc(TEST3)), @Node(coordinates = @Loc(TEST5)),
                    @Node(coordinates = @Loc(TEST7)), @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST2) }, tags = {
                                    "highway=pedestrian" }),
                            @Edge(coordinates = { @Loc(TEST2), @Loc(TEST7) }, tags = {
                                    "highway=pedestrian" }) })
    private Atlas validDisconnectedNodesCrossingPedestrianAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)),
                    @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "barrier=city_wall" }) })
    private Atlas validDisconnectedEdgeCrossingBarrierAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)),
                    @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "layer=1" }) })
    private Atlas validDisconnectedEdgeCrossingLayerAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)),
                    @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "tunnel=yes" }) })
    private Atlas validDisconnectedEdgeCrossingTunnelAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)),
                    @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=secondary", "bridge=yes" }) })
    private Atlas validDisconnectedEdgeCrossingBridgeAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST3)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)),
                    @Node(coordinates = @Loc(TEST8)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST3) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST3), @Loc(TEST8) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=pedestrian" }) })
    private Atlas validDisconnectedEdgeCrossingPedestrianAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST1)), @Node(coordinates = @Loc(TEST6)),
                    @Node(coordinates = @Loc(TEST5)), @Node(coordinates = @Loc(TEST7)) }, edges = {
                            @Edge(coordinates = { @Loc(TEST1), @Loc(TEST6) }, tags = {
                                    "highway=secondary" }),
                            @Edge(coordinates = { @Loc(TEST5), @Loc(TEST7) }, tags = {
                                    "highway=secondary" }) })
    private Atlas invalidDisconnectedNodeOnEdgeAtlas;

    public Atlas invalidConnectedNodesAtlas()
    {
        return this.invalidConnectedNodesAtlas;
    }

    public Atlas invalidConnectedNodesAtlasNavigableDeadEnd()
    {
        return this.invalidDisconnectedNodesAtlasNavigableDeadEnd;
    }

    public Atlas invalidConnectedNodesAtlasNavigableDeadEndReversible()
    {
        return this.invalidDisconnectedNodesAtlasNavigableDeadEndReversible;
    }

    public Atlas invalidDisconnectedEdgesAtlas()
    {
        return this.invalidDisconnectedEdgesAtlas;
    }

    public Atlas invalidDisconnectedNodeOnEdgeAtlas()
    {
        return this.invalidDisconnectedNodeOnEdgeAtlas;
    }

    public Atlas invalidDisconnectedNodesAtlas()
    {
        return this.invalidDisconnectedNodesOppositeAtlas;
    }

    public Atlas invalidDisconnectedNodesOppositeAtlas()
    {
        return this.invalidDisconnectedNodesAtlas;
    }

    public Atlas validConnectedEdges3Atlas()
    {
        return this.validConnectedEdges3Atlas;
    }

    public Atlas validConnectedEdgesOppositeAtlas()
    {
        return this.validConnectedEdgesOppositeAtlas;
    }

    public Atlas validConnectedNodesAndSnapLocationsAtlas()
    {
        return this.validConnectedNodesAndSnapLocationsAtlas;
    }

    public Atlas validConnectedNodesAtlas()
    {
        return this.validConnectedNodesAtlas;
    }

    public Atlas validConnectedNodesLinearAtlas()
    {
        return this.validConnectedNodesLinearAtlas;
    }

    public Atlas validDisconnectedEdgeCrossingBarrierAtlas()
    {
        return this.validDisconnectedEdgeCrossingBarrierAtlas;
    }

    public Atlas validDisconnectedEdgeCrossingBridgeAtlas()
    {
        return this.validDisconnectedEdgeCrossingBridgeAtlas;
    }

    public Atlas validDisconnectedEdgeCrossingLayerAtlas()
    {
        return this.validDisconnectedEdgeCrossingLayerAtlas;
    }

    public Atlas validDisconnectedEdgeCrossingPedestrianAtlas()
    {
        return this.validDisconnectedEdgeCrossingPedestrianAtlas;
    }

    public Atlas validDisconnectedEdgeCrossingTunnelAtlas()
    {
        return this.validDisconnectedEdgeCrossingTunnelAtlas;
    }

    public Atlas validDisconnectedEdgesBarrierAtlas()
    {
        return this.validDisconnectedEdgesBarrierAtlas;
    }

    public Atlas validDisconnectedEdgesNoExitAtlas()
    {
        return this.validDisconnectedEdgesNoExitAtlas;
    }

    public Atlas validDisconnectedNodesCrossingBridgeAtlas()
    {
        return this.validDisconnectedNodesCrossingBridgeAtlas;
    }

    public Atlas validDisconnectedNodesCrossingLayerAtlas()
    {
        return this.validDisconnectedNodesCrossingLayerAtlas;
    }

    public Atlas validDisconnectedNodesCrossingPedestrianAtlas()
    {
        return this.validDisconnectedNodesCrossingPedestrianAtlas;
    }

    public Atlas validDisconnectedNodesCrossingTunnelAtlas()
    {
        return this.validDisconnectedNodesCrossingTunnelAtlas;
    }

    public Atlas validDisconnectedNodesLayerAtlas()
    {
        return this.validDisconnectedNodesLayerAtlas;
    }

    public Atlas validDisconnectedNodesLevelAtlas()
    {
        return this.validDisconnectedNodesLevelAtlas;
    }

    public Atlas validSyntheticNodeCheck()
    {
        return this.validationForSyntheticNode;
    }
}
