package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link UnusualLayerTagsCheck} test data
 *
 * @author mkalender
 */
public class UnusualLayerTagsCheckTestRule extends CoreTestRule
{
    private static final String COMPANY_STORE = "37.3314171,-122.0304871";
    private static final String APPLE_CAMPUS_2 = "37.33531,-122.009566";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=iaminvalid", "highway=service" }) })
    private Atlas invalidLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=iaminvalid", "bridge=yes", "highway=secondary" }) })
    private Atlas invalidLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=iaminvalid", "junction=roundabout", "highway=secondary" }) })
    private Atlas invalidLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=iaminvalid", "tunnel=yes", "highway=secondary" }) })
    private Atlas invalidLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }) })
    private Atlas missingLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "bridge=yes" }) })
    private Atlas missingLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "junction=roundabout" }) })
    private Atlas missingLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "tunnel=yes", "highway=residential" }) })
    private Atlas missingLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) })
    private Atlas noEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=null", "highway=primary" }) })
    private Atlas nullLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=null", "bridge=yes", "highway=primary" }) })
    private Atlas nullLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=null", "junction=roundabout" }) })
    private Atlas nullLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=null", "tunnel=yes", "highway=primary" }) })
    private Atlas nullLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer= ",
                            "highway=primary" }) })
    private Atlas whitespaceLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer= ",
                            "bridge=yes", "highway=service" }) })
    private Atlas whitespaceLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer= ",
                            "junction=roundabout" }) })
    private Atlas whitespaceLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer= ",
                            "tunnel=yes", "highway=service" }) })
    private Atlas whitespaceLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=0",
                            "bridge=yes", "highway=secondary" }) })
    private Atlas zeroLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=0",
                            "junction=roundabout", "highway=service" }) })
    private Atlas zeroLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=0",
                            "tunnel=yes", "highway=service" }) })
    private Atlas zeroLayerTagTunnelEdgeAtlas;

    // More atlases
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-2147483648", "highway=service" }) })
    private Atlas minusInfinityLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-1" }) })
    private Atlas minusOneLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=0",
                            "highway=motorway" }) })
    private Atlas zeroLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=1" }) })
    private Atlas plusOneLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=2147483647", "highway=service" }) })
    private Atlas plusInfinityLayerTagEdgeAtlas;

    // More junction atlases
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-2147483648", "junction=roundabout" }) })
    private Atlas minusInfinityLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-1", "junction=roundabout" }) })
    private Atlas minusOneLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=1",
                            "junction=roundabout" }) })
    private Atlas plusOneLayerTagJunctionEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=2147483647", "junction=roundabout" }) })
    private Atlas plusInfinityLayerTagJunctionEdgeAtlas;

    // More bridge atlases
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-2147483648", "bridge=yes", "highway=secondary" }) })
    private Atlas minusInfinityLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-1", "bridge=yes", "highway=service" }) })
    private Atlas minusOneLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=1", "bridge=yes" }) })
    private Atlas plusOneLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=2", "bridge=yes" }) })
    private Atlas plusTwoLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=3", "bridge=yes" }) })
    private Atlas plusThreeLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=4", "bridge=yes" }) })
    private Atlas plusFourLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=5", "bridge=yes" }) })
    private Atlas plusFiveLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=6",
                            "bridge=yes", "highway=service" }) })
    private Atlas plusSixLayerTagBridgeEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=2147483647", "bridge=yes", "highway=secondary" }) })
    private Atlas plusInfinityLayerTagBridgeEdgeAtlas;

    // More tunnel atlases
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-2147483648", "tunnel=yes", "highway=primary" }) })
    private Atlas minusInfinityLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=-6", "tunnel=yes", "highway=secondary" }) })
    private Atlas minusSixLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-5", "tunnel=yes" }) })
    private Atlas minusFiveLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-4", "tunnel=yes" }) })
    private Atlas minusFourLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-3", "tunnel=yes" }) })
    private Atlas minusThreeLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-2", "tunnel=yes" }) })
    private Atlas minusTwoLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=-1", "tunnel=yes" }) })
    private Atlas minusOneLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=1",
                            "tunnel=yes", "highway=service" }) })
    private Atlas plusOneLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "layer=2147483647", "tunnel=yes", "highway=service" }) })
    private Atlas plusInfinityLayerTagTunnelEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=4",
                            "junction=roundabout", "highway=service", "bridge=yes" }) })
    private Atlas validLayerTagRoundaboutEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=4",
                            "highway=service", "junction=roundabout" }) })
    private Atlas missingBridgeTagJunctionLayerEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = { "layer=4",
                            "highway=service", "bridge= ", "junction=roundabout" }) })
    private Atlas whitespaceBridgeRoundaboutLayerTagEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = APPLE_CAMPUS_2)) }, edges = { @Edge(coordinates = {
                    @Loc(value = COMPANY_STORE), @Loc(value = APPLE_CAMPUS_2) }, tags = {
                            "tunnel=building_passage", "highway=service" }) })
    private Atlas validBuildingPassageTunnelLayerEdgeAtlas;

    public Atlas invalidLayerTagBridgeEdgeAtlas()
    {
        return this.invalidLayerTagBridgeEdgeAtlas;
    }

    public Atlas invalidLayerTagEdgeAtlas()
    {
        return this.invalidLayerTagEdgeAtlas;
    }

    public Atlas invalidLayerTagJunctionEdgeAtlas()
    {
        return this.invalidLayerTagJunctionEdgeAtlas;
    }

    public Atlas invalidLayerTagTunnelEdgeAtlas()
    {
        return this.invalidLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusFiveLayerTagTunnelEdgeAtlas()
    {
        return this.minusFiveLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusFourLayerTagTunnelEdgeAtlas()
    {
        return this.minusFourLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusInfinityLayerTagBridgeEdgeAtlas()
    {
        return this.minusInfinityLayerTagBridgeEdgeAtlas;
    }

    public Atlas minusInfinityLayerTagEdgeAtlas()
    {
        return this.minusInfinityLayerTagEdgeAtlas;
    }

    public Atlas minusInfinityLayerTagJunctionEdgeAtlas()
    {
        return this.minusInfinityLayerTagJunctionEdgeAtlas;
    }

    public Atlas minusInfinityLayerTagTunnelEdgeAtlas()
    {
        return this.minusInfinityLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusOneLayerTagBridgeEdgeAtlas()
    {
        return this.minusOneLayerTagBridgeEdgeAtlas;
    }

    public Atlas minusOneLayerTagEdgeAtlas()
    {
        return this.minusOneLayerTagEdgeAtlas;
    }

    public Atlas minusOneLayerTagJunctionEdgeAtlas()
    {
        return this.minusOneLayerTagJunctionEdgeAtlas;
    }

    public Atlas minusOneLayerTagTunnelEdgeAtlas()
    {
        return this.minusOneLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusSixLayerTagTunnelEdgeAtlas()
    {
        return this.minusSixLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusThreeLayerTagTunnelEdgeAtlas()
    {
        return this.minusThreeLayerTagTunnelEdgeAtlas;
    }

    public Atlas minusTwoLayerTagTunnelEdgeAtlas()
    {
        return this.minusTwoLayerTagTunnelEdgeAtlas;
    }

    public Atlas missingLayerTagBridgeEdgeAtlas()
    {
        return this.missingLayerTagBridgeEdgeAtlas;
    }

    public Atlas missingLayerTagEdgeAtlas()
    {
        return this.missingLayerTagEdgeAtlas;
    }

    public Atlas missingLayerTagJunctionEdgeAtlas()
    {
        return this.missingLayerTagJunctionEdgeAtlas;
    }

    public Atlas missingLayerTagTunnelEdgeAtlas()
    {
        return this.missingLayerTagTunnelEdgeAtlas;
    }

    public Atlas noEdgeAtlas()
    {
        return this.noEdgeAtlas;
    }

    public Atlas nullLayerTagBridgeEdgeAtlas()
    {
        return this.nullLayerTagBridgeEdgeAtlas;
    }

    public Atlas nullLayerTagEdgeAtlas()
    {
        return this.nullLayerTagEdgeAtlas;
    }

    public Atlas nullLayerTagJunctionEdgeAtlas()
    {
        return this.nullLayerTagJunctionEdgeAtlas;
    }

    public Atlas nullLayerTagTunnelEdgeAtlas()
    {
        return this.nullLayerTagTunnelEdgeAtlas;
    }

    public Atlas plusFiveLayerTagBridgeEdgeAtlas()
    {
        return this.plusFiveLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusFourLayerTagBridgeEdgeAtlas()
    {
        return this.plusFourLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusInfinityLayerTagBridgeEdgeAtlas()
    {
        return this.plusInfinityLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusInfinityLayerTagEdgeAtlas()
    {
        return this.plusInfinityLayerTagEdgeAtlas;
    }

    public Atlas plusInfinityLayerTagJunctionEdgeAtlas()
    {
        return this.plusInfinityLayerTagJunctionEdgeAtlas;
    }

    public Atlas plusInfinityLayerTagTunnelEdgeAtlas()
    {
        return this.plusInfinityLayerTagTunnelEdgeAtlas;
    }

    public Atlas plusOneLayerTagBridgeEdgeAtlas()
    {
        return this.plusOneLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusOneLayerTagEdgeAtlas()
    {
        return this.plusOneLayerTagEdgeAtlas;
    }

    public Atlas plusOneLayerTagJunctionEdgeAtlas()
    {
        return this.plusOneLayerTagJunctionEdgeAtlas;
    }

    public Atlas plusOneLayerTagTunnelEdgeAtlas()
    {
        return this.plusOneLayerTagTunnelEdgeAtlas;
    }

    public Atlas plusSixLayerTagBridgeEdgeAtlas()
    {
        return this.plusSixLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusThreeLayerTagBridgeEdgeAtlas()
    {
        return this.plusThreeLayerTagBridgeEdgeAtlas;
    }

    public Atlas plusTwoLayerTagBridgeEdgeAtlas()
    {
        return this.plusTwoLayerTagBridgeEdgeAtlas;
    }

    public Atlas whitespaceLayerTagBridgeEdgeAtlas()
    {
        return this.whitespaceLayerTagBridgeEdgeAtlas;
    }

    public Atlas whitespaceLayerTagEdgeAtlas()
    {
        return this.whitespaceLayerTagEdgeAtlas;
    }

    public Atlas whitespaceLayerTagJunctionEdgeAtlas()
    {
        return this.whitespaceLayerTagJunctionEdgeAtlas;
    }

    public Atlas whitespaceLayerTagTunnelEdgeAtlas()
    {
        return this.whitespaceLayerTagTunnelEdgeAtlas;
    }

    public Atlas zeroLayerTagBridgeEdgeAtlas()
    {
        return this.zeroLayerTagBridgeEdgeAtlas;
    }

    public Atlas zeroLayerTagEdgeAtlas()
    {
        return this.zeroLayerTagEdgeAtlas;
    }

    public Atlas zeroLayerTagJunctionEdgeAtlas()
    {
        return this.zeroLayerTagJunctionEdgeAtlas;
    }

    public Atlas zeroLayerTagTunnelEdgeAtlas()
    {
        return this.zeroLayerTagTunnelEdgeAtlas;
    }

    public Atlas validLayerTagRoundaboutEdgeAtlas()
    {
        return this.validLayerTagRoundaboutEdgeAtlas;
    }

    public Atlas missingBridgeTagJunctionLayerEdgeAtlas()
    {
        return this.missingBridgeTagJunctionLayerEdgeAtlas;
    }

    public Atlas whitespaceBridgeRoundaboutLayerTagEdgeAtlas()
    {
        return whitespaceBridgeRoundaboutLayerTagEdgeAtlas;
    }

    public Atlas validBuildingPassageTunnelLayerEdgeAtlas()
    {
        return validBuildingPassageTunnelLayerEdgeAtlas;
    }
}
