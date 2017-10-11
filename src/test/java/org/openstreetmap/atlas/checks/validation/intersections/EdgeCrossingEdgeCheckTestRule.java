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
 * @author mkalender
 */
public class EdgeCrossingEdgeCheckTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "47.576973, -122.304985";
    private static final String LOCATION_2 = "47.575661, -122.304222";
    private static final String LOCATION_3 = "47.574612, -122.305855";
    private static final String LOCATION_4 = "47.575371, -122.308121";
    private static final String LOCATION_5 = "47.576485, -122.307098";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
                    @Node(coordinates = @Loc(value = LOCATION_2)),
                    @Node(coordinates = @Loc(value = LOCATION_3)),
                    @Node(coordinates = @Loc(value = LOCATION_4)),
                    @Node(coordinates = @Loc(value = LOCATION_5)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_2) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3), @Loc(value = LOCATION_4) }) })
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
                    @Edge(id = "123456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_1) }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_2) }),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_3) }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_1) }),
                    @Edge(id = "-123456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_3) }),
                    @Edge(id = "-223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }),
                    @Edge(id = "-323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }),
                    @Edge(id = "-423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }) })
    private Atlas invalidCrossingNonMasterItemsAtlas;

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
                            @Loc(value = LOCATION_3) }, tags = { "layer=-1" }),
                    @Edge(id = "223456789000000", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_4) }, tags = {}),
                    @Edge(id = "323456789000000", coordinates = { @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_5) }, tags = { "layer=1" }),
                    @Edge(id = "423456789000000", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_4) }, tags = { "layer=2" }) })
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
                            @Loc(value = LOCATION_3) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_2), @Loc(value = LOCATION_3) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }),
                    @Edge(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }) })
    private Atlas validIntersectionItemsAtlas;

    public Atlas invalidCrossingItemsAtlas()
    {
        return this.invalidCrossingItemsAtlas;
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

    public Atlas invalidCrossingNonMasterItemsAtlas()
    {
        return this.invalidCrossingNonMasterItemsAtlas;
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
