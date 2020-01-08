package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link LongSegmentCheck} test data generator
 *
 * @author gpogulsky
 */
public class LongSegmentCheckTestRule extends CoreTestRule
{
    private static final String COMPANY_STORE = "37.3314171,-122.0304871";
    private static final String COMPANY_STORE_990M_EAST = "37.3314166,-122.01929";
    private static final String COMPANY_STORE_1KM_EAST = "37.3314166,-122.0191758";
    private static final String ALVES_CAFFE = "37.32544,-122.033948";
    private static final String CAMPUS_2 = "37.33531,-122.009566";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = COMPANY_STORE_990M_EAST)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = COMPANY_STORE_990M_EAST) }) })
    private Atlas shortSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COMPANY_STORE)),
            @Node(coordinates = @Loc(value = COMPANY_STORE_1KM_EAST)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COMPANY_STORE),
                            @Loc(value = COMPANY_STORE_1KM_EAST) }) })
    private Atlas minDistanceSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ALVES_CAFFE), @Loc(value = CAMPUS_2) }) })
    private Atlas longSegmentAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS_2)) }, edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = CAMPUS_2) }),
                    @Edge(id = "-1", coordinates = { @Loc(value = CAMPUS_2),
                            @Loc(value = ALVES_CAFFE) }) })
    private Atlas longBidirectionalSegment2NodesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ALVES_CAFFE)),
            @Node(coordinates = @Loc(value = CAMPUS_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ALVES_CAFFE),
                            @Loc(value = CAMPUS_2) }, tags = { "route=ferry" }) })
    private Atlas longSegmentFerryAtlas;

    public Atlas longBidirectionalSegment2NodesAtlas()
    {
        return this.longBidirectionalSegment2NodesAtlas;
    }

    public Atlas longSegmentAtlas()
    {
        return this.longSegmentAtlas;
    }

    public Atlas longSegmentFerryAtlas()
    {
        return this.longSegmentFerryAtlas;
    }

    public Atlas minDistanceSegmentAtlas()
    {
        return this.minDistanceSegmentAtlas;
    }

    public Atlas shortSegmentAtlas()
    {
        return this.shortSegmentAtlas;
    }
}
