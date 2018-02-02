package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * {@link IntersectingBuildingsCheckTest} data generator
 *
 * @author mkalender
 */
public class IntersectingBuildingsTestCaseRule extends CoreTestRule
{
    private static final String TEST_1 = "47.620079, -122.206879";
    private static final String TEST_2 = "47.619576, -122.206917";
    private static final String TEST_3 = "47.620094, -122.205856";
    private static final String TEST_4 = "47.619587, -122.205833";
    private static final String TEST_5 = "47.620087, -122.204948";
    private static final String TEST_6 = "47.619522, -122.204926";
    private static final String TEST_7 = "47.620102, -122.206024";
    private static final String TEST_8 = "47.619583, -122.20594";

    @TestAtlas(areas = {
            // a building
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_7), @Loc(value = TEST_8),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another building, but no intersection
            @Area(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_4) }, tags = { "building=yes" }) })
    private Atlas noIntersectingBuildingAtlas;

    @TestAtlas(areas = {
            // a building
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_7), @Loc(value = TEST_8),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another neighbor building, but no intersection
            @Area(coordinates = { @Loc(value = TEST_7), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_8) }, tags = { "building=yes" }) })
    private Atlas neighborBuildingsAtlas;

    @TestAtlas(areas = {
            // a building
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another neighbor building intersecting with the first one
            @Area(coordinates = { @Loc(value = TEST_7), @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_8) }, tags = { "building=yes" }) })
    private Atlas smallIntersectionBuildingsAtlas;

    @TestAtlas(areas = {
            // a building
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another area intersecting, but not a building
            @Area(coordinates = { @Loc(value = TEST_7), @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_8) }, tags = { "building=no" }) })
    private Atlas smallIntersectionAreasAtlas;

    @TestAtlas(areas = {
            // a building
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another building with same footprint
            @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_2) }, tags = { "building=yes" }) })
    private Atlas duplicateBuildingsAtlas;

    @TestAtlas(areas = {
            // a building
            @Area(id = "1234567000000", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another building with same footprint
            @Area(id = "2234567000000", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_2) }, tags = { "building=yes" }),
            // another building with different footprint but overlapping
            @Area(id = "3234567000000", coordinates = { @Loc(value = TEST_7), @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_8) }, tags = { "building=yes" }),
            // another building with different footprint but overlapping
            @Area(id = "4234567000000", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5),
                    @Loc(value = TEST_6), @Loc(value = TEST_2) }, tags = { "building=yes" }) })
    private Atlas severalDuplicateBuildingsAtlas;

    public Atlas duplicateBuildingsAtlas()
    {
        return this.duplicateBuildingsAtlas;
    }

    public Atlas neighborBuildingsAtlas()
    {
        return this.neighborBuildingsAtlas;
    }

    public Atlas noIntersectingBuildingAtlas()
    {
        return this.noIntersectingBuildingAtlas;
    }

    public Atlas severalDuplicateBuildingsAtlas()
    {
        return this.severalDuplicateBuildingsAtlas;
    }

    public Atlas smallIntersectionAreasAtlas()
    {
        return this.smallIntersectionAreasAtlas;
    }

    public Atlas smallIntersectionBuildingsAtlas()
    {
        return this.smallIntersectionBuildingsAtlas;
    }
}
