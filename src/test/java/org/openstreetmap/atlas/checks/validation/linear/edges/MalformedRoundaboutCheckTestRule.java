package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link MalformedRoundaboutCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class MalformedRoundaboutCheckTestRule extends CoreTestRule
{
    private static final String CLOCKWISE_1 = "38.905336130818505,-77.03197002410889";
    private static final String CLOCKWISE_2 = "38.90558660084624,-77.03158378601074";
    private static final String CLOCKWISE_3 = "38.905995699991145,-77.0318305492401";
    private static final String CLOCKWISE_4 = "38.90582872103308,-77.03239917755127";
    private static final String CLOCKWISE_5 = "38.905494761938684,-77.03236699104309";

    private static final String COUNTER_CLOCKWISE_1 = "38.905361177861046,-77.03205585479736";
    private static final String COUNTER_CLOCKWISE_2 = "38.905528157918816,-77.03158378601074";
    private static final String COUNTER_CLOCKWISE_3 = "38.905937257400495,-77.031809091568";
    private static final String COUNTER_CLOCKWISE_4 = "38.90588716371307,-77.03230261802673";
    private static final String COUNTER_CLOCKWISE_5 = "38.90551980892527,-77.03236699104309";

    // Clockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }) })
    private Atlas clockwiseRoundaboutLeftDrivingAtlas;

    // Clockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }) })
    private Atlas clockwiseRoundaboutRightDrivingAtlas;

    // Counterclockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP" }) })
    private Atlas counterClockwiseRoundaboutLeftDrivingAtlas;

    // Counterclockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingAtlas;

    // Multi-directional Atlas
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA" }) })
    private Atlas multiDirectionalRoundaboutAtlas;

    public Atlas clockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas clockwiseRoundaboutRightDrivingAtlas()
    {
        return this.clockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas multiDirectionalRoundaboutAtlas()
    {
        return this.multiDirectionalRoundaboutAtlas;
    }

}
