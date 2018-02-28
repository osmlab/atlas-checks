package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;

/**
 * {@link RoundaboutValenceCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class RoundaboutValenceTestCaseRule extends CoreTestRule
{
    // Roundabout edges
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.32544,-122.033948";
    private static final String TEST_3 = "37.33531,-122.009566";
    private static final String TEST_4 = "37.390535,-122.031007";

    // Non-roundabout edges

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }) })
    private Atlas roundaboutWithValenceZero;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1234567891000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2234567891000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }) })
    private Atlas masterRoundaboutEdgesWithDeadEndNodesAtlas;
}

