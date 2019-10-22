package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;

/**
 * {@link InvalidPiersCheckTest} test data
 *
 * @author sayas01
 */
public class InvalidPiersCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "1.2042824, 103.7984000";
    private static final String TEST_2 = "1.2052837, 103.7992780";
    private static final String TEST_3 = "1.4216577, 103.9106552";
    private static final String TEST_4 = "1.4217830, 103.9106544";
    private static final String TEST_5 = "1.4217858, 103.9107547";
    private static final String TEST_6 = "1.4221270, 103.9107607";

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_2))},
            // Edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=service","man_made=pier" })})
    private Atlas linearPierWithHighwayTagAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @TestAtlas.Loc(value = TEST_3)),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_4)),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_5)),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_6))},
            // Edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4),   @Loc(value = TEST_5) }, tags = { "man_made=pier" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "route=ferry" }),
            })
    private Atlas linearPierConnectedToFerryAtlas;

    @TestAtlas(loadFromTextResource = "InvalidPiersCheck/polygonalPierOverlappingHighwayAtlas.txt")
    private Atlas polygonalPierOverlappingHighwayAtlas;

    public Atlas getPolygonalPierOverlappingHighwayAtlas()
    {
        return this.polygonalPierOverlappingHighwayAtlas;
    }

    @TestAtlas(loadFromTextResource = "InvalidPiersCheck/linearPierConnectedToBuildingAtlas.txt")
    private Atlas linearPierConnectedToBuildingAtlas;

    @TestAtlas(loadFromTextResource = "InvalidPiersCheck/polygonalPierConnectedToBuildingAtlas.txt")
    private Atlas polygonalPierConnectedToBuildingAtlas;

    @TestAtlas(loadFromTextResource = "InvalidPiersCheck/validPolygonalPier.txt")
    private Atlas validPierAtlas;

    public Atlas getLinearPierConnectedToBuildingAtlas()
    {
        return this.linearPierConnectedToBuildingAtlas;
    }

    public Atlas getLinearPierWithHighwayTag()
    {
        return this.linearPierWithHighwayTagAtlas;
    }

    public Atlas getPolygonalPierConnectedToBuildingAtlas()
    {
        return this.polygonalPierConnectedToBuildingAtlas;
    }

    public Atlas getValidPierAtlas()
    {
        return this.validPierAtlas;
    }

    public Atlas getlinearPierConnectedToFerryAtlas()
    {
        return this.linearPierConnectedToFerryAtlas;
    }
}
