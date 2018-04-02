package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * @author daniel-baah
 */
public class AreasWithHighwayTagCheckTestRule extends CoreTestRule
{

    public static final String INVALID_AREA_ID = "127005";
    private static final String AREA_LOCATION_ONE = "37.320524859664474, -122.03601479530336";
    private static final String AREA_LOCATION_TWO = "37.320524859664474, -122.03530669212341";
    private static final String AREA_LOCATION_THREE = "37.32097706357857, -122.03530669212341";
    private static final String AREA_LOCATION_FOUR = "37.32097706357857, -122.03601479530336";

    // Area with irrelevant tag
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "random=tag" }) })
    private Atlas areaNoHighwayTagAtlas;

    // Valid area with highway=pedestrian tag
    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE),
            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_FOUR),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=pedestrian", "area=yes" }) })
    private Atlas validHighwayPedestrianTagAtlas;

    // Area with invalid highway tag
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=primary" }) })
    private Atlas invalidAreaHighwayPrimaryTagAtlas;

    // Area with invalid highway=footway tag
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=footway" }) })
    private Atlas invalidAreaHighwayFootwayTagAtlas;

    // Pedestrian highway without area=yes tag
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidHighwayPedestrianNoAreaTagAtlas;

    public Atlas areaNoHighwayTagAtlas()
    {
        return this.areaNoHighwayTagAtlas;
    }

    public Atlas validHighwayPedestrianTagAtlas()
    {
        return this.validHighwayPedestrianTagAtlas;
    }

    public Atlas invalidAreaHighwayPrimaryTagAtlas()
    {
        return this.invalidAreaHighwayPrimaryTagAtlas;
    }

    public Atlas invalidAreaHighwayFootwayTagAtlas()
    {
        return this.invalidAreaHighwayFootwayTagAtlas;
    }

    public Atlas invalidHighwayPedestrianNoAreaTagAtlas()
    {
        return this.invalidHighwayPedestrianNoAreaTagAtlas;
    }

}
