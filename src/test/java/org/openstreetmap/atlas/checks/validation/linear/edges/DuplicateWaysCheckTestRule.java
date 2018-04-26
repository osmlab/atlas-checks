package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link DuplicateWaysCheck}
 *
 * @author savannahostrowski
 */

public class DuplicateWaysCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.32544,-122.033948";
    private static final String TEST_2 = "37.33531,-122.009566";
    private static final String TEST_3 = "37.3314171,-122.0304871";
    private static final String TEST_4 = "37.337742, -122.032223";
    private static final String TEST_5 = "37.323511, -122.015186";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })

    private Atlas duplicateEdgeCompleteCoverageTwoEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "area=yes" }) })

    private Atlas duplicateEdgeCompleteCoverageTwoEdgesArea;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "3", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })

    private Atlas duplicateEdgeCompleteCoverageThreeEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "3", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "area=yes" }) })

    private Atlas duplicateEdgeCompleteCoverageThreeEdgesAreaOneTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "area=yes" }),
                    @Edge(id = "3", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "area=yes" }) })

    private Atlas duplicateEdgeCompleteCoverageThreeEdgesAreaTwoTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges

            edges = {
                    @Edge(id = "4", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "5", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })

    private Atlas duplicateEdgePartialCoverageTwoEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges

            edges = {
                    @Edge(id = "34", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "35", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway" }),
                    @Edge(id = "36", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }) })

    private Atlas duplicateEdgePartialCoverageThreeEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(id = "234", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "235", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }) })

    private Atlas duplicateEdgeNoCoverageTwoEdges;

    public Atlas duplicateEdgeCompleteCoverageTwoEdges()
    {
        return this.duplicateEdgeCompleteCoverageTwoEdges;
    }

    public Atlas duplicateEdgeCompleteCoverageTwoEdgesArea()
    {
        return this.duplicateEdgeCompleteCoverageTwoEdgesArea;
    }

    public Atlas duplicateEdgeCompleteCoverageThreeEdges()
    {
        return this.duplicateEdgeCompleteCoverageThreeEdges;
    }

    public Atlas getDuplicateEdgeCompleteCoverageThreeEdgesAreaOneTag()
    {
        return this.duplicateEdgeCompleteCoverageThreeEdgesAreaOneTag;
    }

    public Atlas getDuplicateEdgeCompleteCoverageThreeEdgesAreaTwoTag()
    {
        return this.duplicateEdgeCompleteCoverageThreeEdgesAreaTwoTag;
    }

    public Atlas duplicateEdgePartialCoverageTwoEdges()
    {
        return this.duplicateEdgePartialCoverageTwoEdges;
    }

    public Atlas duplicateEdgePartialCoverageThreeEdges()
    {
        return this.duplicateEdgePartialCoverageThreeEdges;
    }

    public Atlas duplicateEdgeNoCoverageTwoEdges()
    {
        return this.duplicateEdgeNoCoverageTwoEdges;
    }

}
