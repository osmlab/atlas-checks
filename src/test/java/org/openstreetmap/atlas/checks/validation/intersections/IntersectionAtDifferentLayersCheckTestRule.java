package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link IntersectionAtDifferentLayersCheckTest} data generator
 *
 * @author vladlemberg
 */
public class IntersectionAtDifferentLayersCheckTestRule extends CoreTestRule
{
    private static final String TEST_NODE_1 = "37.31968,-121.92166";
    private static final String TEST_NODE_2 = "37.31973,-121.92166";
    private static final String TEST_NODE_3 = "37.32004,-121.92167";
    private static final String TEST_NODE_4 = "37.31974,-121.92131";
    private static final String TEST_NODE_5 = "37.31972,-121.92211";

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers. true positive case.
             */
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
    private Atlas invalidLayers;

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers but one of the Edge is an Area.
             */
            // nodes
            nodes = { @Node(id = "1234000000", coordinates = @Loc(value = TEST_NODE_1)),
                    @Node(id = "2345000000", coordinates = @Loc(value = TEST_NODE_2)),
                    @Node(id = "3456000000", coordinates = @Loc(value = TEST_NODE_3)),
                    @Node(id = "4567000000", coordinates = @Loc(value = TEST_NODE_4)),
                    @Node(id = "5678000000", coordinates = @Loc(value = TEST_NODE_5)) },
            // edges
            edges = { @Edge(id = "2345000001", coordinates = { @Loc(value = TEST_NODE_1),
                    @Loc(value = TEST_NODE_2) }, tags = { "highway=primary", "tunnel=yes" }),
                    @Edge(id = "2345000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_3) }, tags = { "highway=primary",
                                    "tunnel=yes" }),
                    @Edge(id = "3456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian",
                                    "area=yes" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian",
                                    "area=yes" }) })
    private Atlas invalidLayersArea;

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers. in addition it matches the
             * great separation filter.
             */
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
    private Atlas invalidLayersGreatSeparationFilter;

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers but Node is a pedestrian
             * crossing.
             */
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
    private Atlas invalidLayersCrossing;

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers but one of the Edge matches
             * indoor mapping filter.
             */
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
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian",
                                    "indoor=yes" }),
                    @Edge(id = "4456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=pedestrian",
                                    "level=3" }),
                    @Edge(id = "5456000001", coordinates = { @Loc(value = TEST_NODE_4),
                            @Loc(value = TEST_NODE_2) }, tags = { "highway=corridor" }),
                    @Edge(id = "3456000002", coordinates = { @Loc(value = TEST_NODE_2),
                            @Loc(value = TEST_NODE_5) }, tags = { "highway=pedestrian",
                                    "indoor=yes" }) })
    private Atlas invalidLayersIndoorMapping;

    @TestAtlas(
            /*
             * this atlas contains intersection at different layers but Node is a railway level
             * crossing.
             */
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

    public Atlas invalidLayers()
    {
        return this.invalidLayers;
    }

    public Atlas invalidLayersArea()
    {
        return this.invalidLayersArea;
    }

    public Atlas invalidLayersCrossing()
    {
        return this.invalidLayersCrossing;
    }

    public Atlas invalidLayersGreatSeparationFilter()
    {
        return this.invalidLayersGreatSeparationFilter;
    }

    public Atlas invalidLayersIndoorMapping()
    {
        return this.invalidLayersIndoorMapping;
    }

    public Atlas invalidLayersRailwayCrossing()
    {
        return this.invalidLevelsRailwayCrossing;
    }
}
