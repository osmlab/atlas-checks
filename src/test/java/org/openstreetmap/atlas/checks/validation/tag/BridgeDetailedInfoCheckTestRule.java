package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import static org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test atlases for {@link BridgeDetailedInfoCheckTest}
 *
 * @author ladwlo
 */
public class BridgeDetailedInfoCheckTestRule extends CoreTestRule
{

    // short bridge
    private static final String LOC_1 = "47.222,-122.444";
    private static final String LOC_2 = "47.225,-122.441";
    // long bridge
    private static final String LOC_3 = "47.111,-122.666";
    private static final String LOC_4 = "47.115,-122.661";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "railway=rail" }) })
    private Atlas longEdgeThatIsNotABridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "bridge=yes" }) })
    private Atlas longGenericBridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "highway=motorway", "bridge=yes" }) })
    private Atlas longGenericMajorHighwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "highway=tertiary", "bridge=yes" }) })
    private Atlas longGenericMinorHighwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "railway=rail", "bridge=yes" }) })
    private Atlas longGenericRailwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "highway=motorway", "bridge=yes",
                            "bridge:structure=arch" }) })
    private Atlas longRailwayBridgeWithStructure;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                    @Loc(value = LOC_4) }, tags = { "railway=rail", "bridge=cantilever" }) })
    private Atlas longRailwayBridgeWithType;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_3)),
                    @Node(coordinates = @Loc(value = LOC_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_3),
                            @Loc(value = LOC_4) }, tags = { "railway=rail", "bridge=yes" }),
                    @Edge(id = "-1000000001", coordinates = { @Loc(value = LOC_4),
                            @Loc(value = LOC_3) }, tags = { "railway=rail", "bridge=yes" }) })
    private Atlas mainAndReversedEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOC_1)),
                    @Node(coordinates = @Loc(value = LOC_2)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = LOC_1),
                    @Loc(value = LOC_2) }, tags = { "highway=motorway", "bridge=yes" }) })
    private Atlas shortGenericHighwayBridge;

    public Atlas getLongEdgeThatIsNotABridge()
    {
        return this.longEdgeThatIsNotABridge;
    }

    public Atlas longGenericBridge()
    {
        return this.longGenericBridge;
    }

    public Atlas longGenericMajorHighwayBridge()
    {
        return this.longGenericMajorHighwayBridge;
    }

    public Atlas longGenericMinorHighwayBridge()
    {
        return this.longGenericMinorHighwayBridge;
    }

    public Atlas longGenericRailwayBridge()
    {
        return this.longGenericRailwayBridge;
    }

    public Atlas longRailwayBridgeWithStructure()
    {
        return this.longRailwayBridgeWithStructure;
    }

    public Atlas longRailwayBridgeWithType()
    {
        return this.longRailwayBridgeWithType;
    }

    public Atlas mainAndReversedEdges()
    {
        return this.mainAndReversedEdges;
    }

    public Atlas shortGenericHighwayBridge()
    {
        return this.shortGenericHighwayBridge;
    }
}
