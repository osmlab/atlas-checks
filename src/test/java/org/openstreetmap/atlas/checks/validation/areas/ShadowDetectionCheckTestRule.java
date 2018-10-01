package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * Unit test atlases for {@link ShadowDetectionCheck}.
 *
 * @author bbreithaupt
 */
public class ShadowDetectionCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2464747377508,-122.438262777482";
    private static final String TEST_2 = "47.2464428615188,-122.438065168917";
    private static final String TEST_3 = "47.2464096570902,-122.438284299207";
    private static final String TEST_4 = "47.2463698117484,-122.438047560233";
    private static final String TEST_5 = "47.246340591812,-122.438307777452";
    private static final String TEST_6 = "47.2462967618771,-122.438029951549";
    private static final String TEST_7 = "47.2462635573569,-122.438335168739";
    private static final String TEST_8 = "47.2462223837229,-122.438018212427";

    @TestAtlas(
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_4),
                    @Loc(value = TEST_3) }, tags = { "building=yes", "height=20" }) })
    private Atlas validBuildingAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = { "building=yes",
                            "height=20", "min_height=3" }) })
    private Atlas invalidFloatingHeightBuildingAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = { "building=yes",
                            "building:levels=5", "building:min_level=1" }) })
    private Atlas invalidFloatingLevelBuildingAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=1" }) })
    private Atlas validBuildingPartsTouchAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=0" }) })
    private Atlas validBuildingPartsTouchGroundAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_7), @Loc(value = TEST_8) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=1" }) })
    private Atlas validBuildingPartsIntersectAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4),
                            @Loc(value = TEST_7), @Loc(value = TEST_8) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=6" }) })
    private Atlas invalidBuildingPartsIntersectAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6),
                            @Loc(value = TEST_8), @Loc(value = TEST_7) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=4" }) })
    private Atlas invalidBuildingPartsDisparateAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "building:levels=8",
                                    "building:min_level=5" }) })
    private Atlas validBuildingPartsStackedAtlas;

    @TestAtlas(
            // areas
            areas = {
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_3) }, tags = {
                                    "building:part=yes", "building:levels=5" }),
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_6), @Loc(value = TEST_5) }, tags = {
                                    "building:part=yes", "height=30", "min_height=17.5" }) })
    private Atlas validBuildingPartsStackedMixedTagsAtlas;

    public Atlas validBuildingAtlas()
    {
        return this.validBuildingAtlas;
    }

    public Atlas invalidFloatingHeightBuildingAtlas()
    {
        return this.invalidFloatingHeightBuildingAtlas;
    }

    public Atlas invalidFloatingLevelBuildingAtlas()
    {
        return this.invalidFloatingLevelBuildingAtlas;
    }

    public Atlas validBuildingPartsTouchAtlas()
    {
        return this.validBuildingPartsTouchAtlas;
    }

    public Atlas validBuildingPartsTouchGroundAtlas()
    {
        return this.validBuildingPartsTouchGroundAtlas;
    }

    public Atlas validBuildingPartsIntersectAtlas()
    {
        return this.validBuildingPartsIntersectAtlas;
    }

    public Atlas invalidBuildingPartsIntersectAtlas()
    {
        return this.invalidBuildingPartsIntersectAtlas;
    }

    public Atlas invalidBuildingPartsDisparateAtlas()
    {
        return this.invalidBuildingPartsDisparateAtlas;
    }

    public Atlas validBuildingPartsStackedAtlas()
    {
        return this.validBuildingPartsStackedAtlas;
    }

    public Atlas validBuildingPartsStackedMixedTagsAtlas()
    {
        return this.validBuildingPartsStackedMixedTagsAtlas;
    }
}
