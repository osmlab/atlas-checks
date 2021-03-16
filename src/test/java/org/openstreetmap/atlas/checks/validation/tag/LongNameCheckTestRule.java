package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Unit test atlases for {@link LongNameCheckTest}.
 *
 * @author bbreithaupt
 */
public class LongNameCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "53.2226484, -4.2036036";
    private static final String TEST_2 = "53.2229552, -4.2036429";
    private static final String TEST_3 = "53.2232145, -4.2034552";

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_3), tags = "name=short") })
    private Atlas validNameAtlas;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_1), tags = "name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch") })
    private Atlas invalidNameAtlas;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_1), tags = "name:en=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch") })
    private Atlas invalidNameEnAtlas;

    @TestAtlas(
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_1), tags = "alt_name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch") })
    private Atlas invalidAltNameAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = {
                    "reg_name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch",
                    "loc_name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch" }) })
    private Atlas invalidRegionalLocalNameAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                            @Edge(id = "1000001", coordinates = { @Loc(value = TEST_1),
                                    @Loc(value = TEST_1) }, tags = "name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch street"),
                            @Edge(id = "1000002", coordinates = { @Loc(value = TEST_1),
                                    @Loc(value = TEST_1) }, tags = "name=Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch street") })
    private Atlas invalidEdgeNamesAtlas;

    public Atlas invalidAltNameAtlas()
    {
        return this.invalidAltNameAtlas;
    }

    public Atlas invalidEdgeNamesAtlas()
    {
        return this.invalidEdgeNamesAtlas;
    }

    public Atlas invalidNameAtlas()
    {
        return this.invalidNameAtlas;
    }

    public Atlas invalidNameEnAtlas()
    {
        return this.invalidNameEnAtlas;
    }

    public Atlas invalidRegionalLocalNameAtlas()
    {
        return this.invalidRegionalLocalNameAtlas;
    }

    public Atlas validNameAtlas()
    {
        return this.validNameAtlas;
    }
}
