package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link HighwayIntersectionCheckTest} data generator
 *
 * @author pako.todea
 */
public class HighwayIntersectionTestCaseRule extends CoreTestRule
{

    private static final String LOCATION_1 = "47.576973, -122.304985";
    private static final String LOCATION_2 = "47.575661, -122.304222";
    private static final String LOCATION_3 = "47.574612, -122.305855";
    private static final String LOCATION_4 = "47.575371, -122.308121";
    private static final String LOCATION_5 = "47.576485, -122.307098";

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_3),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas noCrossingHighwayWaterEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas invalidCrossingHighwayWaterEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_4),
                            @TestAtlas.Loc(value = LOCATION_5) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "1236000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas invalidMultipleCrossingHighwayWaterEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_3),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "power=line" }) })
    private Atlas noCrossingHighwayPowerLineEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "power=line" }) })
    private Atlas invalidCrossingHighwayPowerLineEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=dam",
                                    "highway=service" }) })
    private Atlas validCrossingHighwayWaterwayDamEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=weir",
                                    "highway=service" }) })
    private Atlas validCrossingHighwayWaterwayWeirEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "ford=yes" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas validCrossingWaterwayFordYesEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "ford=yes" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "power=line" }) })
    private Atlas invalidCrossingPowerLineFordYesEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "leisure=slipway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas validCrossingWaterwayLeisureSlipwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "leisure=slipway" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "power=line" }) })
    private Atlas invalidCrossingPowerLineLeisureSlipwayEdges;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_1),
                            @TestAtlas.Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "leisure=dance" }),
                    @TestAtlas.Edge(id = "1235000000", coordinates = {
                            @TestAtlas.Loc(value = LOCATION_2),
                            @TestAtlas.Loc(value = LOCATION_4) }, tags = { "waterway=stream" }) })
    private Atlas invalidCrossingWaterwayLeisureEdges;

    public Atlas invalidCrossingHighwayPowerLineEdges()
    {
        return this.invalidCrossingHighwayPowerLineEdges;
    }

    public Atlas invalidCrossingHighwayWaterEdges()
    {
        return this.invalidCrossingHighwayWaterEdges;
    }

    public Atlas invalidCrossingPowerLineFordYesEdges()
    {
        return this.invalidCrossingPowerLineFordYesEdges;
    }

    public Atlas invalidCrossingPowerLineLeisureSlipwayEdges()
    {
        return this.invalidCrossingPowerLineLeisureSlipwayEdges;
    }

    public Atlas invalidCrossingWaterwayLeisureEdges()
    {
        return this.invalidCrossingWaterwayLeisureEdges;
    }

    public Atlas invalidMultipleCrossingHighwayWaterEdges()
    {
        return this.invalidMultipleCrossingHighwayWaterEdges;
    }

    public Atlas noCrossingHighwayPowerLineEdges()
    {
        return this.noCrossingHighwayPowerLineEdges;
    }

    public Atlas noCrossingHighwayWaterEdges()
    {
        return this.noCrossingHighwayWaterEdges;
    }

    public Atlas validCrossingHighwayWaterwayDamEdges()
    {
        return this.validCrossingHighwayWaterwayDamEdges;
    }

    public Atlas validCrossingHighwayWaterwayWeirEdges()
    {
        return this.validCrossingHighwayWaterwayWeirEdges;
    }

    public Atlas validCrossingWaterwayFordYesEdges()
    {
        return this.validCrossingWaterwayFordYesEdges;
    }

    public Atlas validCrossingWaterwayLeisureSlipwayEdges()
    {
        return this.validCrossingWaterwayLeisureSlipwayEdges;
    }
}
