package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link IntersectionAtDifferentLevelsCheckTest} data generator
 *
 * @author vladlemberg
 */
public class IntersectionAtDifferentLevelsCheckTestRule extends CoreTestRule
{
    private static final String TEST_NODE_1 = "37.31968,-121.92166";
    private static final String TEST_NODE_2 = "37.31973,-121.92166";
    private static final String TEST_NODE_3 = "37.32004,-121.92167";
    private static final String TEST_NODE_4 = "37.31974,-121.92131";
    private static final String TEST_NODE_5 = "37.31972,-121.92211";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1234000000", coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(id = "2345000000", coordinates = @Loc(value = TEST_NODE_2)),
                    @Node(id = "3456000000", coordinates = @Loc(value = TEST_NODE_3)),
                    @Node(id = "4567000000", coordinates = @Loc(value = TEST_NODE_4)),
                    @Node(id = "5678000000", coordinates = @Loc(value = TEST_NODE_5)) },
            // edges
            edges = {
                    @Edge(id = "2345000001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "2345000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_3) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "3456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidLevels;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1234000000", coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(id = "2345000000", coordinates = @Loc(value = TEST_NODE_2)),
                    @Node(id = "3456000000", coordinates = @Loc(value = TEST_NODE_3)),
                    @Node(id = "4567000000", coordinates = @Loc(value = TEST_NODE_4)),
                    @Node(id = "5678000000", coordinates = @Loc(value = TEST_NODE_5)) },
            // edges
            edges = { @Edge(id = "2345000001", coordinates = { @Loc(value = TEST_NODE_1),
                    @Loc(value = TEST_NODE_2) }, tags = { "highway=primary", "bridge=yes" }),
                    @Edge(id = "2345000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_3) }, tags = { "highway=primary",
                                    "bridge=yes" }),
                    @Edge(id = "3456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidLevelsFilter;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1234000000", coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(id = "2345000000", coordinates = @Loc(value = TEST_NODE_2), tags = {
                            "highway=crossing" }),
                    @Node(id = "3456000000", coordinates = @Loc(value = TEST_NODE_3)),
                    @Node(id = "4567000000", coordinates = @Loc(value = TEST_NODE_4)),
                    @Node(id = "5678000000", coordinates = @Loc(value = TEST_NODE_5)) },
            // edges
            edges = {
                    @Edge(id = "2345000001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "2345000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_3) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "3456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidLevelsCrossing;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1234000000", coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(id = "2345000000", coordinates = @Loc(value = TEST_NODE_2), tags = {
                            "railway=level_crossing" }),
                    @Node(id = "3456000000", coordinates = @Loc(value = TEST_NODE_3)),
                    @Node(id = "4567000000", coordinates = @Loc(value = TEST_NODE_4)),
                    @Node(id = "5678000000", coordinates = @Loc(value = TEST_NODE_5)) },
            // edges
            edges = {
                    @Edge(id = "2345000001", coordinates = { @Loc(value = TEST_NODE_1),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "2345000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_3) }, tags = { "highway=primary", "layer=1" }),
                    @Edge(id = "3456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian" }) })
    private Atlas invalidLevelsRailwayCrossing;

    public Atlas getInvalidLevels()
    {
        return this.invalidLevels;
    }

    public Atlas getInvalidLevelsCrossing()
    {
        return this.invalidLevelsCrossing;
    }

    public Atlas getInvalidLevelsFilter()
    {
        return this.invalidLevelsFilter;
    }

    public Atlas getInvalidLevelsRailwayCrossing()
    {
        return this.invalidLevelsRailwayCrossing;
    }
}