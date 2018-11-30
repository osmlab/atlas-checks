package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

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

    // Don't flag areas with area=yes tag and highway tag in validHighwayTags.
    // This shouldn't ever happen in the wild, since the default atlas filter only allows areas
    // without highway tags or highway=platform.
    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE),
            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
            @Loc(value = AREA_LOCATION_FOUR),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=pedestrian", "area=yes" }) })
    private Atlas validHighwayPedestrianTagAtlas;

    // Flag area with area=yes tag and highway tag not in validHighwayTags
    // Same as validHighwayPedestrianTagAtlas, this should not appear in the wild.
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=primary", "area=yes" }) })
    private Atlas invalidAreaHighwayPrimaryTagAtlas;

    // Area with invalid highway=footway tag
    // As above, this should not appear in the wild.
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=footway", "area=yes" }) })
    private Atlas invalidAreaHighwayFootwayTagAtlas;

    // Pedestrian highway without area=yes tag
    // As above, this should not appear in the wild.
    @TestAtlas(areas = { @Area(id = INVALID_AREA_ID, coordinates = {
            @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
            @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
            @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidHighwayPedestrianNoAreaTagAtlas;

    // Area with highway=platform tag, which is legitimate.
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=platform", "area=yes" }) })
    private Atlas validAreaHighwayPlatform;

    // Flag edge with area=yes tag and highway tag not in validHighwayTags
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = AREA_LOCATION_ONE)),
            @Node(coordinates = @Loc(value = AREA_LOCATION_TWO)) }, edges = { @Edge(coordinates = {
                    @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO) }, tags = {
                            "area=yes", "highway=secondary" }) })
    private Atlas invalidEdgeHighwaySecondary;

    // Don't flag edge with area=yes tag and highway tag in validHighwayTags
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = AREA_LOCATION_ONE)),
            @Node(coordinates = @Loc(value = AREA_LOCATION_TWO)) }, edges = { @Edge(coordinates = {
                    @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO) }, tags = {
                            "area=yes", "highway=service" }) })
    private Atlas validEdgeHighwayService;

    // Don't flag edge without area=yes tag
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = AREA_LOCATION_ONE)),
            @Node(coordinates = @Loc(value = AREA_LOCATION_TWO)) }, edges = {
                    @Edge(coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO) }, tags = { "highway=secondary" }) })
    private Atlas validEdgeNoAreaTag;

    // Don't flag areas without area=yes tag, regardless of highway tags.
    // The way that would generate this area would actually be ingested as an edge in a real-world
    // scenario.
    @TestAtlas(areas = {
            @Area(coordinates = { @Loc(value = AREA_LOCATION_ONE), @Loc(value = AREA_LOCATION_TWO),
                    @Loc(value = AREA_LOCATION_THREE), @Loc(value = AREA_LOCATION_FOUR),
                    @Loc(value = AREA_LOCATION_ONE) }, tags = { "highway=primary" }) })
    private Atlas validAreaHighwayPrimaryNoAreaTag;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = AREA_LOCATION_ONE)),
            @Node(coordinates = @Loc(value = AREA_LOCATION_TWO)),
            @Node(coordinates = @Loc(value = AREA_LOCATION_THREE)) }, edges = {
                    @Edge(coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO) }, tags = { "area=yes",
                                    "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = AREA_LOCATION_TWO),
                            @Loc(value = AREA_LOCATION_THREE) }, tags = { "area=yes",
                                    "highway=primary" }) })
    private Atlas connectedEdgesBadTags;

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

    public Atlas validAreaHighwayPlatform()
    {
        return this.validAreaHighwayPlatform;
    }

    public Atlas invalidEdgeHighwaySecondary()
    {
        return invalidEdgeHighwaySecondary;
    }

    public Atlas validEdgeHighwayService()
    {
        return validEdgeHighwayService;
    }

    public Atlas validEdgeNoAreaTag()
    {
        return validEdgeNoAreaTag;
    }

    public Atlas validAreaHighwayPrimaryNoAreaTag()
    {
        return validAreaHighwayPrimaryNoAreaTag;
    }

    public Atlas connectedEdgesBadTags()
    {
        return connectedEdgesBadTags;
    }

}
