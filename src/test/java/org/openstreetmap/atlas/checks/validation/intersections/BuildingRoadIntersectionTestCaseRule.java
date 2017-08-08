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
    private static final String TEST_3 = "37.325440,-122.033948";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.317585,-122.052138";
    private static final String TEST_6 = "37.390535,-122.031007";

    @TestAtlas(

            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            edges = {

                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }) },

            areas = {
                    // Regular building - flagged
                    @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
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
                            @Loc(value = TEST_6) }, tags = { "highway=pedestrian", "area=yes" }) })
    private Atlas atlas;

    @TestAtlas(loadFromTextResource = "covered.atlas")
    private Atlas coveredAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getCoveredAtlas()
    {
        return this.coveredAtlas;
    }
}
