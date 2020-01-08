package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link ShortSegmentCheck} test data generator
 *
 * @author mkalender
 */
public class ShortSegmentCheckTestRule extends CoreTestRule
{
    private static final String COMPANY_STORE = "37.3314171,-122.0304871";
    private static final String A_LOCATION_NEAR_COMPANY_STORE = "37.3314261,-122.0304871";
    private static final String A_LOCATION_CLOSE_TO_COMPANY_STORE_1 = "37.3314171,-122.0304758";
    private static final String A_LOCATION_CLOSE_TO_COMPANY_STORE_2 = "37.3314171,-122.0304645";
    private static final String A_LOCATION_CLOSE_TO_COMPANY_STORE_3 = "37.3314171,-122.0304532";
    private static final String A_LOCATION_CLOSE_TO_COMPANY_STORE_4 = "37.3314171,-122.0304419";
    private static final String ALVES_CAFFE = "37.32544,-122.033948";
    private static final String CAMPUS = "37.33531,-122.009566";

    private static final String HIGHWAY_RESIDENTIAL = "highway=residential";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_NEAR_COMPANY_STORE)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_NEAR_COMPANY_STORE) }, tags = {
                                    HIGHWAY_RESIDENTIAL }) })
    private Atlas almostShortSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = CAMPUS) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas longSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS)) }, edges = {
                    @Edge(id = "444342902000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = { @Loc(value = CAMPUS),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentsWithTwoValence2NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentWithTwoValence1NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    "highway=path" }) })
    private Atlas shortSegmentPathWithTwoValence1NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "-444342903000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortBidirectionalSegmentWithTwoValence2NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "-444342903000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortBidirectionalSegmentWithOneValence3NodeAndOneValence2NodeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = ALVES_CAFFE) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342905000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = CAMPUS) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342906000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = ALVES_CAFFE) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342907000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = CAMPUS) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentWithValence3NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentWithOneValence2NodeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)) }, edges = {
                    @Edge(id = "444342902000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342902000001", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342902000002", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentClosedWayWithTwoValence2NodeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS)) }, edges = {
                    @Edge(id = "444342902000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342902000001", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = ALVES_CAFFE) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342902000002", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = COMPANY_STORE) }, tags = { HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342903000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = CAMPUS) }, tags = { HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentClosedWayWithOneValence2NodeAtlas;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = COMPANY_STORE), tags = { "barrier=gate" }),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = ALVES_CAFFE)) }, edges = {
                    @Edge(id = "444342903000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }) })
    private Atlas shortSegmentWithOneValence2NodeOneBarrierNodeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_2)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_3)),
            @Node(coordinates = @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_4)) }, edges = {
                    @Edge(id = "444342902000000", coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342903000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_1),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_2) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342904000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_2),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_3) }, tags = {
                                    HIGHWAY_RESIDENTIAL }),
                    @Edge(id = "444342905000000", coordinates = {
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_3),
                            @Loc(value = A_LOCATION_CLOSE_TO_COMPANY_STORE_4) }, tags = {
                                    HIGHWAY_RESIDENTIAL })

    })
    private Atlas shortSegmentsWithValence1And2NodesAtlas;

    public Atlas almostShortSegmentAtlas()
    {
        return this.almostShortSegmentAtlas;
    }

    public Atlas longSegmentAtlas()
    {
        return this.longSegmentAtlas;
    }

    public Atlas shortBidirectionalSegmentWithOneValence3NodeAndOneValence2NodeAtlas()
    {
        return this.shortBidirectionalSegmentWithOneValence3NodeAndOneValence2NodeAtlas;
    }

    public Atlas shortBidirectionalSegmentWithTwoValence2NodesAtlas()
    {
        return this.shortBidirectionalSegmentWithTwoValence2NodesAtlas;
    }

    public Atlas shortSegmentClosedWayWithOneValence2NodeAtlas()
    {
        return this.shortSegmentClosedWayWithOneValence2NodeAtlas;
    }

    public Atlas shortSegmentClosedWayWithTwoValence2NodeAtlas()
    {
        return this.shortSegmentClosedWayWithTwoValence2NodeAtlas;
    }

    public Atlas shortSegmentPathWithTwoValence1NodesAtlas()
    {
        return this.shortSegmentPathWithTwoValence1NodesAtlas;
    }

    public Atlas shortSegmentWithOneValence2NodeAtlas()
    {
        return this.shortSegmentWithOneValence2NodeAtlas;
    }

    public Atlas shortSegmentWithOneValence2NodeOneBarrierNodeAtlas()
    {
        return this.shortSegmentWithOneValence2NodeOneBarrierNodeAtlas;
    }

    public Atlas shortSegmentWithTwoValence1NodesAtlas()
    {
        return this.shortSegmentWithTwoValence1NodesAtlas;
    }

    public Atlas shortSegmentWithValence3NodesAtlas()
    {
        return this.shortSegmentWithValence3NodesAtlas;
    }

    public Atlas shortSegmentsWithTwoValence2NodesAtlas()
    {
        return this.shortSegmentsWithTwoValence2NodesAtlas;
    }

    public Atlas shortSegmentsWithValence1And2NodesAtlas()
    {
        return this.shortSegmentsWithValence1And2NodesAtlas;
    }
}
