package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Unit test rule for {@link OceanBleedingCheck}
 *
 * @author seancoulter
 */
public class OceanBleedingCheckTestRule extends CoreTestRule
{
    private static final String AREA_LOCATION_1 = "47.6312, -122.3406";
    private static final String AREA_LOCATION_2 = "47.6263, -122.3370";
    private static final String AREA_LOCATION_3 = "47.6328, -122.3271";

    private static final String LOCATION_INSIDE_OCEAN = "47.6348, -122.3352";
    private static final String LOCATION_OUTSIDE_AREA_1 = "47.6182, -122.3366";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(AREA_LOCATION_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)), @Node(coordinates = @Loc(AREA_LOCATION_3)),
            @Node(coordinates = @Loc(LOCATION_OUTSIDE_AREA_1)),
            @Node(coordinates = @Loc(LOCATION_INSIDE_OCEAN)) }, areas = {
                    @Area(tags = { "natural=bay" }, coordinates = { @Loc(AREA_LOCATION_1),
                            @Loc(AREA_LOCATION_2), @Loc(AREA_LOCATION_3) }) }, lines = {
                                    @Line(tags = { "railway=rail" }, coordinates = {
                                            @Loc(LOCATION_OUTSIDE_AREA_1),
                                            @Loc(LOCATION_INSIDE_OCEAN) }) })
    private Atlas invalidRailwayBleedingIntoOcean;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(AREA_LOCATION_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)), @Node(coordinates = @Loc(AREA_LOCATION_3)),
            @Node(coordinates = @Loc(LOCATION_OUTSIDE_AREA_1)),
            @Node(coordinates = @Loc(LOCATION_INSIDE_OCEAN)) }, areas = {
                    @Area(tags = { "place=sea" }, coordinates = { @Loc(AREA_LOCATION_1),
                            @Loc(AREA_LOCATION_2), @Loc(AREA_LOCATION_3) }),
                    @Area(tags = { "building=greenhouse" }, coordinates = {
                            @Loc(LOCATION_OUTSIDE_AREA_1), @Loc(LOCATION_INSIDE_OCEAN),
                            @Loc(AREA_LOCATION_1) }) })
    private Atlas invalidBuildingBleedingIntoOcean;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(AREA_LOCATION_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)), @Node(coordinates = @Loc(AREA_LOCATION_3)),
            @Node(coordinates = @Loc(LOCATION_OUTSIDE_AREA_1)),
            @Node(coordinates = @Loc(LOCATION_INSIDE_OCEAN)) }, areas = {
                    @Area(tags = { "seamark=sea_area" }, coordinates = { @Loc(AREA_LOCATION_1),
                            @Loc(AREA_LOCATION_2), @Loc(AREA_LOCATION_3) }) }, edges = {
                                    @Edge(tags = { "highway=tertiary" }, coordinates = {
                                            @Loc(LOCATION_OUTSIDE_AREA_1),
                                            @Loc(LOCATION_INSIDE_OCEAN) }) })
    private Atlas invalidStreetBleedingIntoOcean;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(AREA_LOCATION_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)), @Node(coordinates = @Loc(AREA_LOCATION_3)),
            @Node(coordinates = @Loc(LOCATION_OUTSIDE_AREA_1)),
            @Node(coordinates = @Loc(LOCATION_INSIDE_OCEAN)) }, areas = {
                    @Area(tags = { "place=ocean" }, coordinates = { @Loc(AREA_LOCATION_1),
                            @Loc(AREA_LOCATION_2), @Loc(AREA_LOCATION_3) }) }, lines = {
                                    @Line(tags = { "railway=rail" }, coordinates = {
                                            @Loc(LOCATION_OUTSIDE_AREA_1),
                                            @Loc(LOCATION_INSIDE_OCEAN) }) })
    private Atlas validRailwayBleedingIntoWaterbodyNonOcean;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(AREA_LOCATION_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)), @Node(coordinates = @Loc(AREA_LOCATION_3)),
            @Node(coordinates = @Loc(LOCATION_OUTSIDE_AREA_1)),
            @Node(coordinates = @Loc(AREA_LOCATION_2)) }, areas = {
                    @Area(tags = { "natural=fjord" }, coordinates = { @Loc(AREA_LOCATION_1),
                            @Loc(AREA_LOCATION_2), @Loc(AREA_LOCATION_3) }) }, lines = {
                                    @Line(tags = { "bridge=yes" }, coordinates = {
                                            @Loc(LOCATION_OUTSIDE_AREA_1),
                                            @Loc(LOCATION_INSIDE_OCEAN) }) })
    private Atlas validBridgeOverOceanBoundary;

    public Atlas getInvalidBuildingBleedingIntoOcean()
    {
        return this.invalidBuildingBleedingIntoOcean;
    }

    public Atlas getInvalidRailwayBleedingIntoOcean()
    {
        return this.invalidRailwayBleedingIntoOcean;
    }

    public Atlas getInvalidStreetBleedingIntoOcean()
    {
        return this.invalidStreetBleedingIntoOcean;
    }

    public Atlas getValidBridgeOverOceanBoundary()
    {
        return this.validBridgeOverOceanBoundary;
    }

    public Atlas getValidRailwayBleedingIntoWaterbodyNonOcean()
    {
        return this.validRailwayBleedingIntoWaterbodyNonOcean;
    }

}
