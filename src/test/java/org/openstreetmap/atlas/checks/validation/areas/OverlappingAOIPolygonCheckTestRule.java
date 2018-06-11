package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * Tests for {@link OverlappingAOIPolygonCheck}
 *
 * @author bbreithaupt
 */
public class OverlappingAOIPolygonCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.244117672349,-122.396137421285";
    private static final String TEST_2 = "47.2434634265599,-122.396147058415";
    private static final String TEST_3 = "47.2434634265599,-122.395549556359";
    private static final String TEST_4 = "47.2441111299311,-122.395549556359";
    private static final String TEST_5 = "47.2449550951688,-122.396469902267";
    private static final String TEST_6 = "47.2442158085205,-122.396479539397";
    private static final String TEST_7 = "47.2440097223504,-122.395814577432";
    private static final String TEST_8 = "47.2449485528544,-122.395809758867";

    @TestAtlas(
            // areas
            areas = {
                    @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "amenity=GRAVE_YARD" }),
                    @Area(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_8) }, tags = { "amenity=GRAVE_YARD" }) })
    private Atlas sameAOIsNoOverlap;

    @TestAtlas(
            // areas
            areas = {
                    @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "amenity=GRAVE_YARD" }),
                    @Area(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6), @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "amenity=GRAVE_YARD" }) })
    private Atlas sameAOIs;

    @TestAtlas(
            // areas
            areas = {
                    @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "amenity=GRAVE_YARD" }),
                    @Area(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6), @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "landuse=CEMETERY" }) })
    private Atlas similarAOIs;

    @TestAtlas(
            // areas
            areas = {
                    @Area(id = "1000000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "amenity=GRAVE_YARD" }),
                    @Area(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6), @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "tourism=ZOO" }) })
    private Atlas differentAOIs;

    public Atlas sameAOIsNoOverlap()
    {
        return this.sameAOIsNoOverlap;
    }

    public Atlas sameAOIs()
    {
        return this.sameAOIs;
    }

    public Atlas similarAOIs()
    {
        return this.similarAOIs;
    }

    public Atlas differentAOIs()
    {
        return this.differentAOIs;
    }
}
