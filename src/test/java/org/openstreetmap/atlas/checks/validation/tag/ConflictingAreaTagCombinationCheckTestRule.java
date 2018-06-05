package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * ConflictingAreaTagCombination test Atlas.
 *
 * @author danielbaah
 */
public class ConflictingAreaTagCombinationCheckTestRule extends CoreTestRule
{
    public static final String INVALID_AREA_ID = "127005";
    private static final String AREA_LOCATION_ONE = "37.320524859664474, -122.03601479530336";
    private static final String AREA_LOCATION_TWO = "37.320524859664474, -122.03530669212341";
    private static final String AREA_LOCATION_THREE = "37.32097706357857, -122.03530669212341";

    // building=* and natural=*
    @TestAtlas(areas = {
            @Area(id = INVALID_AREA_ID, coordinates = { @Loc(value = AREA_LOCATION_ONE),
                    @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "building=yes", "natural=rock" }) })
    private Atlas buildingNaturalTagAtlas;

    // building=* and highway=*
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "building=yes", "highway=pedestrian" }) })
    private Atlas buildingHighwayTagAtlas;

    // building=* and highway=*
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "building=yes", "highway=services" }) })
    private Atlas buildingHighwayServicesAtlas;

    // natural=* and man_made=* valid tag combinations
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_ONE) }, tags = {
                    "natural=water", "man_made=reservoir_covered" }) })
    private Atlas naturalManMadeTagAtlas;

    // natural=* and highway=*
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "natural=water", "highway=primary" }) })
    private Atlas naturalHighwayTagAtlas;

    // natural=* and leisure=* valid tag combinations
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "natural=grassland", "leisure=park" }) })
    private Atlas naturalLeisureTagAtlas;

    // natural=water and landuse=*
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "natural=water", "landuse=port" }) })
    private Atlas waterLandUseTagAtlas;

    // natural=water and landuse=aquaculture
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "natural=water", "landuse=aquaculture" }) })
    private Atlas waterLandUseAquacultureAtlas;

    // natural=water and man_made=
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "natural=water", "man_made=chimney" }) })
    private Atlas waterManMadeChimneyAtlas;

    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "landuse=disused", "highway=primary" }) })
    private Atlas landUseHighwayAtlas;

    public Atlas getBuildingNaturalTagAtlas()
    {
        return buildingNaturalTagAtlas;
    }

    public Atlas getBuildingHighwayTagAtlas()
    {
        return buildingHighwayTagAtlas;
    }

    public Atlas getBuildingHighwayServicesAtlas()
    {
        return buildingHighwayServicesAtlas;
    }

    public Atlas getNaturalManMadeTagAtlas()
    {
        return naturalManMadeTagAtlas;
    }

    public Atlas getNaturalHighwayTagAtlas()
    {
        return naturalHighwayTagAtlas;
    }

    public Atlas getNaturalLeisureTagAtlas()
    {
        return naturalLeisureTagAtlas;
    }

    public Atlas getWaterLandUseTagAtlas()
    {
        return waterLandUseTagAtlas;
    }

    public Atlas getWaterLandUseAquacultureAtlas()
    {
        return waterLandUseAquacultureAtlas;
    }

    public Atlas getWaterManMadeChimneyAtlas()
    {
        return waterManMadeChimneyAtlas;
    }

    public Atlas getLandUseHighwayAtlas()
    {
        return landUseHighwayAtlas;
    }
}
