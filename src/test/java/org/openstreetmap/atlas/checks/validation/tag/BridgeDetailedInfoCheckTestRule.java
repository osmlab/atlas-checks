package org.openstreetmap.atlas.checks.validation.tag;

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
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3),
                    @TestAtlas.Loc(value = LOC_4) }, tags = { "railway=yes" }) })
    private Atlas longEdgeThatIsNotABridge;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3),
                    @TestAtlas.Loc(value = LOC_4) }, tags = { "bridge=yes" }) })
    private Atlas longGenericBridge;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3),
                    @TestAtlas.Loc(value = LOC_4) }, tags = { "highway=motorway", "bridge=yes" }) })
    private Atlas longGenericMajorHighwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3),
                    @TestAtlas.Loc(value = LOC_4) }, tags = { "highway=tertiary", "bridge=yes" }) })
    private Atlas longGenericMinorHighwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3),
                    @TestAtlas.Loc(value = LOC_4) }, tags = { "railway=yes", "bridge=yes" }) })
    private Atlas longGenericRailwayBridge;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3), @TestAtlas.Loc(value = LOC_4) }, tags = {
                            "highway=motorway", "bridge=yes", "bridge:structure=arch" }) })
    private Atlas longRailwayBridgeWithStructure;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_3), @TestAtlas.Loc(value = LOC_4) }, tags = {
                            "railway=yes", "bridge=cantilever" }) })
    private Atlas longRailwayBridgeWithType;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOC_2)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = LOC_1),
                    @TestAtlas.Loc(value = LOC_2) }, tags = { "highway=motorway", "bridge=yes" }) })
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

    public Atlas shortGenericHighwayBridge()
    {
        return this.shortGenericHighwayBridge;
    }
}
