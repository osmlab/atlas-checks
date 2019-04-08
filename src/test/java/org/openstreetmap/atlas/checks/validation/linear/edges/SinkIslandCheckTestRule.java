package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link SinkIslandCheck} test data
 *
 * @author gpogulsky
 * @author nachtm
 * @author sayas01
 */
public class SinkIslandCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.335310,-122.009566";
    private static final String TEST_2 = "37.321628,-122.028464";
    private static final String TEST_3 = "37.317585,-122.052138";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.390535,-122.031007";
    private static final String TEST_6 = "37.325440,-122.033948";
    private static final String TEST_7 = "37.3314171,-122.0304871";
    private static final String TEST_8 = "37.324233,-122.003467";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_6)),
            @Node(coordinates = @Loc(value = TEST_7)), @Node(coordinates = @Loc(value = TEST_4)),
            @Node(coordinates = @Loc(value = TEST_1)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_8)) },

            edges = {
                    @Edge(id = "160978519000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_2) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "260978519000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "360978519000001", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "460978519000001", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_4) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "560978519000001", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "660978519000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "760978519000001", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "860978519000001", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "960978519000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_8) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "-960978519000001", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_5) }, tags = { "highway=primary", "oneway=yes" }) })
    private Atlas testAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_6)),
            @Node(coordinates = @Loc(value = TEST_7)) },

            edges = { @Edge(id = "360978519000001", coordinates = { @Loc(value = TEST_6),
                    @Loc(value = TEST_7) }, tags = { "highway=primary", "oneway=yes" }) })
    private Atlas singleEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_6)),
            @Node(coordinates = @Loc(value = TEST_7), tags = { "amenity=parking" }) },

            edges = { @Edge(id = "360978519000003", coordinates = { @Loc(value = TEST_6),
                    @Loc(value = TEST_7) }, tags = { "highway=primary", "oneway=yes" }) })
    private Atlas singleEdgeWithAmenityAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3), tags = {
                    "amenity=parking_space" }) }, edges = {
                            @Edge(coordinates = { @Loc(value = TEST_1),
                                    @Loc(value = TEST_2) }, tags = { "highway=primary" }),
                            @Edge(coordinates = { @Loc(value = TEST_2),
                                    @Loc(value = TEST_3) }, tags = { "highway=primary" }) })
    private Atlas twoEdgesWithAmenityAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=track" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=track" }) })
    private Atlas trackSinkIsland;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=track" }) })
    private Atlas trackAndHighwaySinkIsland;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=service" }) })
    private Atlas serviceSinkIsland;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=service", "aeroway=taxiway" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=service", "route=ferry" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=footpath" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=service", "area=yes" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=service" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_1) }, tags = {
                            "highway=service" }), })
    private Atlas invalidEdges;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=service", "service=driveway" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=pedestrian" }) })
    private Atlas pedestrianNetwork;

    public Atlas getSingleEdgeAtlas()
    {
        return this.singleEdgeAtlas;
    }

    public Atlas getTestAtlas()
    {
        return this.testAtlas;
    }

    public Atlas getSingleEdgeWithAmenityAtlas()
    {
        return this.singleEdgeWithAmenityAtlas;
    }

    public Atlas getTwoEdgesWithAmenityAtlas()
    {
        return this.twoEdgesWithAmenityAtlas;
    }

    public Atlas getTrackSinkIsland()
    {
        return this.trackSinkIsland;
    }

    public Atlas getTrackAndPrimarySinkIsland()
    {
        return this.trackAndHighwaySinkIsland;
    }

    public Atlas getServiceSinkIsland()
    {
        return this.serviceSinkIsland;
    }

    public Atlas getInvalidEdges()
    {
        return this.invalidEdges;
    }

    public Atlas getEdgeConnectedToPedestrianNetwork()
    {
        return this.pedestrianNetwork;
    }
}
