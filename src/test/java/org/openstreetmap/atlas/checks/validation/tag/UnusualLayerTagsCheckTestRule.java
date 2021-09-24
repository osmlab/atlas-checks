package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;

/**
 * {@link UnusualLayerTagsCheck} test data
 *
 * @author mkalender, bbreithaupt
 */
public class UnusualLayerTagsCheckTestRule extends CoreTestRule
{
    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=1", "bridge=yes" }) })
    private Atlas falsePositiveHighwayNotOnGroundWithBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=-1", "covered=yes" }) })
    private Atlas falsePositiveHighwayNotOnGroundWithCovered;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=steps",
                                    "layer=1" }) })
    private Atlas falsePositiveHighwayNotOnGroundWithHighwaySteps;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=service",
                                    "layer=1", "service=parking_aisle" }) })
    private Atlas falsePositiveHighwayNotOnGroundWithServiceParkingAisle;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=-1", "tunnel=yes" }) })
    private Atlas falsePositiveHighwayNotOnGroundWithTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=1", "bridge=yes" }) })
    private Atlas falsePositiveLandUseNotOnGroundWithBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=-1", "covered=yes" }) })
    private Atlas falsePositiveLandUseNotOnGroundWithCovered;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=-2", "tunnel=yes" }) })
    private Atlas falsePositiveLandUseNotOnGroundWithTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=1", "bridge=yes" }) })
    private Atlas falsePositiveNaturalNotOnGroundWithBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=-1", "covered=yes" }) })
    private Atlas falsePositiveNaturalNotOnGroundWithCovered;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=-1", "tunnel=yes" }) })
    private Atlas falsePositiveNaturalNotOnGroundWithTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=1", "bridge=yes" }) })
    private Atlas falsePositiveWaterwayNotOnGroundWithBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=-1", "covered=yes" }) })
    private Atlas falsePositiveWaterwayNotOnGroundWithCovered;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=-1", "location=underground" }) })
    private Atlas falsePositiveWaterwayNotOnGroundWithLocationUnderground;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=-1", "tunnel=yes" }) })
    private Atlas falsePositiveWaterwayNotOnGroundWithTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "bridge=yes" }) })
    private Atlas truePositiveBadLayerValueBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "bridge=yes",
                                    "layer=10" }) })
    private Atlas truePositiveBadLayerValueBridgeAboveRange;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "bridge=yes",
                                    "layer=-1" }) })
    private Atlas truePositiveBadLayerValueBridgeBelowRange;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "tunnel=yes", "layer=-1",
                                    "bridge=yes" }) })
    private Atlas truePositiveBadLayerValueBridgeWithTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "tunnel=yes" }) })
    private Atlas truePositiveBadLayerValueTunnel;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "tunnel=yes",
                                    "layer=1" }) })
    private Atlas truePositiveBadLayerValueTunnelAboveRange;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "tunnel=yes",
                                    "layer=-10" }) })
    private Atlas truePositiveBadLayerValueTunnelBelowRange;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "tunnel=yes", "layer=1",
                                    "bridge=yes" }) })
    private Atlas truePositiveBadLayerValueTunnelWithBridge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=-1" }) })
    private Atlas truePositiveHighwayNotOnGround;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=1", "bridge=no" }) })
    private Atlas truePositiveHighwayNotOnGroundWithBridgeNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=trunk",
                                    "layer=-1", "tunnel=no" }) })
    private Atlas truePositiveHighwayNotOnGroundWithTunnelNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=1" }) })
    private Atlas truePositiveLandUseNotOnGround;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=1", "bridge=no" }) })
    private Atlas truePositiveLandUseNotOnGroundWithBridgeNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "landuse=forest",
                                    "layer=-1", "tunnel=no" }) })
    private Atlas truePositiveLandUseNotOnGroundWithTunnelNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "layer=0" }) })
    private Atlas truePositiveLayerTagIsZero;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=1" }) })
    private Atlas truePositiveNaturalNotOnGround;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=1", "bridge=no" }) })
    private Atlas truePositiveNaturalNotOnGroundWithBridgeNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "natural=wood",
                                    "layer=-1", "tunnel=no" }) })
    private Atlas truePositiveNaturalNotOnGroundWithTunnelNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=1" }) })
    private Atlas truePositiveWaterwayNotOnGround;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=1", "bridge=no" }) })
    private Atlas truePositiveWaterwayNotOnGroundWithBridgeNo;

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "100003", coordinates = { @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "waterway=stream",
                                    "layer=-1", "tunnel=no" }) })
    private Atlas truePositiveWaterwayNotOnGroundWithTunnelNo;

    public Atlas getFalsePositiveHighwayNotOnGroundWithBridge()
    {
        return this.falsePositiveHighwayNotOnGroundWithBridge;
    }

    public Atlas getFalsePositiveHighwayNotOnGroundWithCovered()
    {
        return this.falsePositiveHighwayNotOnGroundWithCovered;
    }

    public Atlas getFalsePositiveHighwayNotOnGroundWithHighwaySteps()
    {
        return this.falsePositiveHighwayNotOnGroundWithHighwaySteps;
    }

    public Atlas getFalsePositiveHighwayNotOnGroundWithServiceParkingAisle()
    {
        return this.falsePositiveHighwayNotOnGroundWithServiceParkingAisle;
    }

    public Atlas getFalsePositiveHighwayNotOnGroundWithTunnel()
    {
        return this.falsePositiveHighwayNotOnGroundWithTunnel;
    }

    public Atlas getFalsePositiveLandUseNotOnGroundWithBridge()
    {
        return this.falsePositiveLandUseNotOnGroundWithBridge;
    }

    public Atlas getFalsePositiveLandUseNotOnGroundWithCovered()
    {
        return this.falsePositiveLandUseNotOnGroundWithCovered;
    }

    public Atlas getFalsePositiveLandUseNotOnGroundWithTunnel()
    {
        return this.falsePositiveLandUseNotOnGroundWithTunnel;
    }

    public Atlas getFalsePositiveNaturalNotOnGroundWithBridge()
    {
        return this.falsePositiveNaturalNotOnGroundWithBridge;
    }

    public Atlas getFalsePositiveNaturalNotOnGroundWithCovered()
    {
        return this.falsePositiveNaturalNotOnGroundWithCovered;
    }

    public Atlas getFalsePositiveNaturalNotOnGroundWithTunnel()
    {
        return this.falsePositiveNaturalNotOnGroundWithTunnel;
    }

    public Atlas getFalsePositiveWaterwayNotOnGroundWithBridge()
    {
        return this.falsePositiveWaterwayNotOnGroundWithBridge;
    }

    public Atlas getFalsePositiveWaterwayNotOnGroundWithCovered()
    {
        return this.falsePositiveWaterwayNotOnGroundWithCovered;
    }

    public Atlas getFalsePositiveWaterwayNotOnGroundWithLocationUnderground()
    {
        return this.falsePositiveWaterwayNotOnGroundWithLocationUnderground;
    }

    public Atlas getFalsePositiveWaterwayNotOnGroundWithTunnel()
    {
        return this.falsePositiveWaterwayNotOnGroundWithTunnel;
    }

    public Atlas getTruePositiveBadLayerValueBridge()
    {
        return this.truePositiveBadLayerValueBridge;
    }

    public Atlas getTruePositiveBadLayerValueBridgeAboveRange()
    {
        return this.truePositiveBadLayerValueBridgeAboveRange;
    }

    public Atlas getTruePositiveBadLayerValueBridgeBelowRange()
    {
        return this.truePositiveBadLayerValueBridgeBelowRange;
    }

    public Atlas getTruePositiveBadLayerValueBridgeWithTunnel()
    {
        return this.truePositiveBadLayerValueBridgeWithTunnel;
    }

    public Atlas getTruePositiveBadLayerValueTunnel()
    {
        return this.truePositiveBadLayerValueTunnel;
    }

    public Atlas getTruePositiveBadLayerValueTunnelAboveRange()
    {
        return this.truePositiveBadLayerValueTunnelAboveRange;
    }

    public Atlas getTruePositiveBadLayerValueTunnelBelowRange()
    {
        return this.truePositiveBadLayerValueTunnelBelowRange;
    }

    public Atlas getTruePositiveBadLayerValueTunnelWithBridge()
    {
        return this.truePositiveBadLayerValueTunnelWithBridge;
    }

    public Atlas getTruePositiveHighwayNotOnGround()
    {
        return this.truePositiveHighwayNotOnGround;
    }

    public Atlas getTruePositiveHighwayNotOnGroundWithBridgeNo()
    {
        return this.truePositiveHighwayNotOnGroundWithBridgeNo;
    }

    public Atlas getTruePositiveHighwayNotOnGroundWithTunnelNo()
    {
        return this.truePositiveHighwayNotOnGroundWithTunnelNo;
    }

    public Atlas getTruePositiveLandUseNotOnGround()
    {
        return this.truePositiveLandUseNotOnGround;
    }

    public Atlas getTruePositiveLandUseNotOnGroundWithBridgeNo()
    {
        return this.truePositiveLandUseNotOnGroundWithBridgeNo;
    }

    public Atlas getTruePositiveLandUseNotOnGroundWithTunnelNo()
    {
        return this.truePositiveLandUseNotOnGroundWithTunnelNo;
    }

    public Atlas getTruePositiveLayerTagIsZero()
    {
        return this.truePositiveLayerTagIsZero;
    }

    public Atlas getTruePositiveNaturalNotOnGround()
    {
        return this.truePositiveNaturalNotOnGround;
    }

    public Atlas getTruePositiveNaturalNotOnGroundWithBridgeNo()
    {
        return this.truePositiveNaturalNotOnGroundWithBridgeNo;
    }

    public Atlas getTruePositiveNaturalNotOnGroundWithTunnelNo()
    {
        return this.truePositiveNaturalNotOnGroundWithTunnelNo;
    }

    public Atlas getTruePositiveWaterwayNotOnGround()
    {
        return this.truePositiveWaterwayNotOnGround;
    }

    public Atlas getTruePositiveWaterwayNotOnGroundWithBridgeNo()
    {
        return this.truePositiveWaterwayNotOnGroundWithBridgeNo;
    }

    public Atlas getTruePositiveWaterwayNotOnGroundWithTunnelNo()
    {
        return this.truePositiveWaterwayNotOnGroundWithTunnelNo;
    }
}
