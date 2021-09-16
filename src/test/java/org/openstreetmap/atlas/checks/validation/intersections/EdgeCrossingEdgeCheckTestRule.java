package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link EdgeCrossingEdgeCheckTest} data generator
 *
 * @author mkalender, bbreithaupt
 */
public class EdgeCrossingEdgeCheckTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "47.576973, -122.304985";
    private static final String LOCATION_2 = "47.575661, -122.304222";
    private static final String LOCATION_3 = "47.574612, -122.305855";
    private static final String LOCATION_4 = "47.575371, -122.308121";
    private static final String LOCATION_5 = "47.576485, -122.307098";
    private static final String TEST1 = "47.2620768463041,-122.474848242369";
    private static final String TEST2 = "47.2618703753848,-122.474832590205";
    private static final String TEST3 = "47.26186971149,-122.474824764123";
    private static final String TEST4 = "47.2618624086451,-122.475082046561";
    private static final String TEST5 = "47.2618624086451,-122.474591938191";
    private static final String TEST6 = "47.2617588409197,-122.474843351067";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }) })
    private Atlas noCrossingItemsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_3) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=crossing" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "footway=crossing" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=footway" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=path" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=pedestrian",
                                    "area=yes" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_2) }, tags = { "railway=crossing" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_2) }, tags = { "highway=cycleway" }) })
    private Atlas validCrossingItemsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }) })
    private Atlas invalidCrossingItemsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway", "area=yes" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }) })
    private Atlas invalidCrossingItemsAtlasArea;

    @TestAtlas(
            /*
             * This Atlas contains Car and Pedestrian Navigable edges
             */
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=primary" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=footway" }) })
    private Atlas invalidCrossingItemsAtlasCarPedestrian;

    @TestAtlas(
            /*
             * This Atlas contains Car and Pedestrian Navigable edges
             */
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=steps" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=corridor" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=pedestrian",
                                    "indoor=yes" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=footway", "level=1" }) })
    private Atlas invalidCrossingItemsAtlasIndoorMapping;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_1) }, tags = { "highway=motorway" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_2) }, tags = { "highway=motorway" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_1) }, tags = { "highway=motorway" }),
                    @Edge(id = "-123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "-223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }),
                    @Edge(id = "-323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "-423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }) })
    private Atlas invalidCrossingNonMainItemsAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=service", "layer=1" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=road", "layer=1" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=residential",
                                    "layer=-1" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=track", "layer=-1" }) })
    private Atlas invalidCrossingItemsWithSameLayerTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway", "layer=-1" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway" }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway", "layer=1" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "highway=motorway", "layer=2" }) })
    private Atlas invalidCrossingItemsWithDifferentLayerTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway", "layer=x" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = { "highway=primary", }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_2) }, tags = { "highway=tertiary", "layer=-1" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_5) }, tags = { "highway=trunk", "layer=2" }) })
    private Atlas invalidCrossingItemsWithInvalidLayerTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_2), @Loc(value = LOCATION_3) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway" }) })
    private Atlas validIntersectionItemsAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(TEST1)),
                    @Node(id = "2000000", coordinates = @Loc(TEST2)),
                    @Node(id = "3000000", coordinates = @Loc(TEST3)),
                    @Node(id = "4000000", coordinates = @Loc(TEST4)),
                    @Node(id = "5000000", coordinates = @Loc(TEST5)),
                    @Node(id = "6000000", coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(id = "1234000000", coordinates = { @Loc(TEST1), @Loc(TEST3),
                                    @Loc(TEST6) }, tags = { "highway=secondary" }),
                            @Edge(id = "2345000000", coordinates = { @Loc(TEST4), @Loc(TEST2),
                                    @Loc(TEST5) }, tags = { "highway=secondary" }), })
    private Atlas invalidDisconnectedNodesCrossingAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(TEST1)),
                    @Node(id = "2000000", coordinates = @Loc(TEST3)),
                    @Node(id = "3000000", coordinates = @Loc(TEST4)),
                    @Node(id = "4000000", coordinates = @Loc(TEST5)),
                    @Node(id = "5000000", coordinates = @Loc(TEST6)) }, edges = {
                            @Edge(id = "1234000000", coordinates = { @Loc(TEST1), @Loc(TEST3),
                                    @Loc(TEST6) }, tags = { "highway=secondary" }),
                            @Edge(id = "2345000000", coordinates = { @Loc(TEST4),
                                    @Loc(TEST5) }, tags = { "highway=secondary" }) })
    private Atlas invalidDisconnectedEdgeCrossingAtlas;

    public Atlas invalidCrossingItemsAtlas()
    {
        return this.invalidCrossingItemsAtlas;
    }

    public Atlas invalidCrossingItemsAtlasArea()
    {
        return this.invalidCrossingItemsAtlasArea;
    }

    public Atlas invalidCrossingItemsAtlasCarPedestrian()
    {
        return this.invalidCrossingItemsAtlasCarPedestrian;
    }

    public Atlas invalidCrossingItemsAtlasIndoorMapping()
    {
        return this.invalidCrossingItemsAtlasIndoorMapping;
    }

    public Atlas invalidCrossingItemsWithDifferentLayerTagAtlas()
    {
        return this.invalidCrossingItemsWithDifferentLayerTagAtlas;
    }

    public Atlas invalidCrossingItemsWithInvalidLayerTagAtlas()
    {
        return this.invalidCrossingItemsWithInvalidLayerTagAtlas;
    }

    public Atlas invalidCrossingItemsWithSameLayerTagAtlas()
    {
        return this.invalidCrossingItemsWithSameLayerTagAtlas;
    }

    public Atlas invalidCrossingNonMainItemsAtlas()
    {
        return this.invalidCrossingNonMainItemsAtlas;
    }

    public Atlas invalidDisconnectedEdgeCrossingAtlas()
    {
        return this.invalidDisconnectedEdgeCrossingAtlas;
    }

    public Atlas invalidDisconnectedNodesCrossingAtlas()
    {
        return this.invalidDisconnectedNodesCrossingAtlas;
    }

    public Atlas noCrossingItemsAtlas()
    {
        return this.noCrossingItemsAtlas;
    }

    public Atlas validCrossingItemsAtlas()
    {
        return this.validCrossingItemsAtlas;
    }

    public Atlas validIntersectionItemsAtlas()
    {
        return this.validIntersectionItemsAtlas;
    }
}
