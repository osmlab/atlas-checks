package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link WrongWayRoundaboutCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class WrongWayRoundaboutCheckTestRule extends CoreTestRule
{
    // Roundabout edges
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.32544,-122.033948";
    private static final String TEST_3 = "37.33531,-122.009566";
    private static final String TEST_4 = "37.390535,-122.031007";
    private static final String TEST_5 = "37.331460, -122.032579";
    private static final String TEST_6 = "37.322020, -122.038963";

    // Clockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}) })
    private Atlas clockwiseRoundaboutLeftDrivingAtlas;

    // Clockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}) })
    private Atlas clockwiseRoundaboutRightDrivingAtlas;

    // Clockwise roundabout with valence of 2 (connected to nodes in roundabout)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(id = "3456", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "3457", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "3458", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "3459", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "3460", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway",
                            "iso_country_code=USA"}),
                    @Edge(id = "3461", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway",
                            "iso_country_code=USA"}) })
    private Atlas clockwiseRoundaboutWithConnectionsRightDriving;

    // Counterclockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}),
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP"}) })
    private Atlas counterClockwiseRoundaboutLeftDrivingAtlas;

    // Counterclockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                            "iso_country_code=USA"}),
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                            "iso_country_code=USA" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingAtlas;


    public Atlas clockwiseRoundaboutLeftDrivingAtlas() {
        return this.clockwiseRoundaboutLeftDrivingAtlas;
    }
    public Atlas clockwiseRoundaboutRightDrivingAtlas() {
        return this.clockwiseRoundaboutRightDrivingAtlas;
    }
    public Atlas clockwiseRoundaboutWithConnectionsRightDriving() {
        return this.clockwiseRoundaboutWithConnectionsRightDriving;
    }
    public Atlas counterClockwiseRoundaboutLeftDrivingAtlas() {
        return this.counterClockwiseRoundaboutLeftDrivingAtlas;
    }
    public Atlas counterClockwiseRoundaboutRightDrivingAtlas() {
        return this.counterClockwiseRoundaboutRightDrivingAtlas;
    }



}
