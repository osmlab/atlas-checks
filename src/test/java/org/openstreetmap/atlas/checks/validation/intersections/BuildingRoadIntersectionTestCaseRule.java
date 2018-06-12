package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link BuildingRoadIntersectionCheckTest} test data
 *
 * @author mgostintsev
 */
public class BuildingRoadIntersectionTestCaseRule extends CoreTestRule
{
    private static final String TEST_1 = "37.335310,-122.009566";
    private static final String TEST_2 = "37.3314171,-122.0304871";
    private static final String TEST_2_0 = "10.12352901009, -80.76485349564";
    private static final String TEST_2_1 = "10.12309410805, -80.7054341158";
    private static final String TEST_2_2 = "10.04451299889, -80.70602752863";
    private static final String TEST_2_3 = "10.04494800701, -80.76544690847";
    private static final String TEST_2_4 = "10.09373688925, -80.76507849106";
    private static final String TEST_2_5 = "10.0750348474, -80.70579705713";
    private static final String TEST_2_6 = "10.10700231854, -80.82073863728";
    private static final String TEST_2_7 = "10.05567718163, -80.65485033146";
    private static final String TEST_3 = "37.325440,-122.033948";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.317585,-122.052138";
    private static final String TEST_6 = "37.390535,-122.031007";
    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(id = "292929292929", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }) },

            areas = {
                    // Regular building - flagged
                    @Area(id = "323232323232", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_2), @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }),
                    // Non-Pedestrian Area - flagged
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "highway=primary", "building=house" }),
                    // Pedestrian Area - not flagged
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "highway=pedestrian",
                                    "building=house" }),
                    // Another variation of a Pedestrian Area - not flagged
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "highway=pedestrian", "area=yes" }),
                    // Parking area - not flagged
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "parking=yes" }) })
    private Atlas atlas;
    @TestAtlas(loadFromTextResource = "covered.atlas")
    private Atlas coveredAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_2_5)),
            @Node(coordinates = @Loc(value = TEST_2_4)),
            @Node(coordinates = @Loc(value = TEST_2_3)),
            @Node(coordinates = @Loc(value = TEST_2_2)),
            @Node(coordinates = @Loc(value = TEST_2_1)),
            @Node(coordinates = @Loc(value = TEST_2_0)),
            @Node(coordinates = @Loc(value = TEST_2_7)),
            @Node(coordinates = @Loc(value = TEST_2_6)) },

            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2_4), @Loc(value = TEST_2_5) }, tags = {
                            "tunnel=building_passage", "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2_5), @Loc(value = TEST_2_4) }, tags = {
                            "tunnel=building_passage", "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2_6), @Loc(value = TEST_2_4) }, tags = {
                            "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2_4), @Loc(value = TEST_2_6) }, tags = {
                            "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2_7), @Loc(value = TEST_2_5) }, tags = {
                            "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2_5), @Loc(value = TEST_2_7) }, tags = {
                            "highway=service" }) },

            areas = { @Area(coordinates = { @Loc(value = TEST_2_0), @Loc(value = TEST_2_1),
                    @Loc(value = TEST_2_5), @Loc(value = TEST_2_2), @Loc(value = TEST_2_3),
                    @Loc(value = TEST_2_4), @Loc(value = TEST_2_0) }, tags = { "building=yes" }) })
    private Atlas tunnelBuildingIntersect;
    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=primary", "layer=-1" }) },

            areas = {
                    @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }),
                    @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "highway=primary",
                                    "building=house" }) })
    private Atlas layered;

    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(id = "292929292929", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "area=yes" }) },

            areas = {
                    // Regular building - flagged
                    @Area(id = "323232323232", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_2), @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas edgeAreaYesAtlas;

    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(id = "292929292929", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "indoor=yes" }) },

            areas = {
                    // Regular building - flagged
                    @Area(id = "323232323232", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_2), @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas edgeIndoorYesAtlas;

    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(id = "292929292929", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                                    "highway=service", "service=driveway" }) },

            areas = {
                    // Regular building - flagged
                    @Area(id = "323232323232", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_2), @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas edgeHighWayServiceAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getCoveredAtlas()
    {
        return this.coveredAtlas;
    }

    public Atlas getTunnelBuildingIntersect()
    {
        return this.tunnelBuildingIntersect;
    }

    public Atlas getLayeredAtlas()
    {
        return this.layered;
    }

    public Atlas getEdgeAreaYesAtlas()
    {
        return this.edgeAreaYesAtlas;
    }

    public Atlas getEdgeIndoorYesAtlas()
    {
        return this.edgeIndoorYesAtlas;
    }

    public Atlas getEdgeHighWayServiceAtlas()
    {
        return this.edgeHighWayServiceAtlas;
    }
}
