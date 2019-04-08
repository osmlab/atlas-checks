package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Test rule for {@link AddressStreetNameCheckTest}
 *
 * @author bbreithaupt
 */
public class AddressStreetNameCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "48.1780944662566,-122.645324334797";
    private static final String TEST_2 = "48.1784193930508,-122.644707774486";
    private static final String TEST_3 = "48.1789233570657,-122.645035943684";
    private static final String TEST_4 = "48.1785221755876,-122.645165222459";

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_4), tags = { "addr:street=1st st" }) },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=residential", "name=1st st" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=residential", "name=2nd st" }) })
    private Atlas validAddressStreetTagAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_4), tags = {
                    "addr:street=Rue de adresse" }) },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                    "highway=residential", "name=1st st", "name:fr=Rue de adresse" }) })
    private Atlas validAddressStreetLocalizedTagAtlas;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_4), tags = { "addr:street=3rd st" }) },
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=residential", "name=1st st", "name:fr=Rue de adresse" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=residential", "name=2nd st" }) })
    private Atlas invalidAddressStreetTagAtlas;

    public Atlas validAddressStreetTagAtlas()
    {
        return this.validAddressStreetTagAtlas;
    }

    public Atlas validAddressStreetLocalizedTagAtlas()
    {
        return this.validAddressStreetLocalizedTagAtlas;
    }

    public Atlas invalidAddressStreetTagAtlas()
    {
        return this.invalidAddressStreetTagAtlas;
    }
}
