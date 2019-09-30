/**
 *
 */
package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author gpogulsky
 */
public class FloatingEdgeCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.32544,-122.033948";
    private static final String TEST_2 = "37.33531,-122.009566";
    private static final String TEST_3 = "37.3314171,-122.0304871";

    private static final String LOCATION_1 = "1.11,1.11";
    private static final String LOCATION_2 = "1.12,1.12";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }) }, areas = {
                                    @Area(coordinates = { @Loc(value = TEST_1),
                                            @Loc(value = TEST_2) }, tags = { "aeroway=taxiway" }) })
    private Atlas airportAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=SECONDARY" }) })
    private Atlas connectedEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas floatingEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_1) }, tags = { "highway=SECONDARY" }) })
    private Atlas floatingBidirectionalEdgeAtlas;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = LOCATION_1), tags = { "synthetic_boundary_node=YES" }),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas syntheticBorderAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas mixedAtlas;

    public Atlas airportAtlas()
    {
        return this.airportAtlas;
    }

    public Atlas connectedEdgeAtlas()
    {
        return this.connectedEdgeAtlas;
    }

    public Atlas floatingBidirectionalEdgeAtlas()
    {
        return this.floatingBidirectionalEdgeAtlas;
    }

    public Atlas floatingEdgeAtlas()
    {
        return this.floatingEdgeAtlas;
    }

    public Atlas mixedAtlas()
    {
        return this.mixedAtlas;
    }

    public Atlas syntheticBorderAtlas()
    {
        return this.syntheticBorderAtlas;
    }

}
