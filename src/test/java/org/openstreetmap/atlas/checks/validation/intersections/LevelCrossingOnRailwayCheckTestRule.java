package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Unit test rule for {@link LevelCrossingOnRailwayCheck}.
 *
 * @author atiannicelli
 */
public class LevelCrossingOnRailwayCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "20.538246,10.546134";
    private static final String TEST_2 = "20.535768,10.543755";
    private static final String TEST_3 = "20.535773,10.548353";
    private static final String R_NODE_1 = "42.6572418,-71.1448285";
    private static final String R_NODE_2 = "42.6567733,-71.1450118";
    private static final String H1_NODE_1 = "42.6571529,-71.1452251";
    private static final String H1_NODE_2 = "42.6569201,-71.1446396";
    private static final String H2_NODE_1 = "42.657005,-71.1452115";
    private static final String H2_NODE_2 = "42.6568232,-71.1447537";
    private static final String INT1 = "42.6570284,-71.144912";

    @TestAtlas(
            // This atlas contains invalid features with level_crossing tag.
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = TEST_1), tags = {
                            "railway=level_crossing" }),
                    @Node(id = "223456789000000", coordinates = @Loc(value = TEST_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = TEST_3), tags = {}) },
            // points
            points = { @Point(id = "423456789000000", coordinates = @Loc(value = TEST_1), tags = {
                    "railway=level_crossing" }) },
            // edges
            edges = { @Edge(id = "523456789000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "railway=level_crossing" }) },
            // lines
            lines = { @Line(id = "623456789000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "railway=level_crossing" }) },
            // areas
            areas = { @Area(id = "723456789000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "railway=level_crossing" }) },
            // relations
            relations = {
                    @Relation(id = "823456789000000", members = @Member(id = "123456789000000", role = "member", type = "node"), tags = {
                            "railway=level_crossing" }) })
    private Atlas invalidObjectsWithTag;

    @TestAtlas(
            /*
             * This test atlas includes nodes, edges, and lines to test for missing intersections.
             * Highways are represented as edges in atlas so this test includes two highways. One
             * highway has a valid intersection with the railway line and the other highway does
             * not.
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {}),
                    @Node(id = "623456789000000", coordinates = @Loc(value = H2_NODE_1), tags = {}),
                    @Node(id = "723456789000000", coordinates = @Loc(value = H2_NODE_2), tags = {}) },
            // edges
            edges = { @Edge(id = "233456789000000", coordinates = { @Loc(value = H1_NODE_1),
                    @Loc(value = H1_NODE_2), @Loc(value = INT1) }, tags = { "highway=secondary" }),
                    @Edge(id = "333456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary" }) },
            // lines
            lines = { @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                    @Loc(value = R_NODE_2), @Loc(value = INT1) }, tags = { "railway=rail" }) })

    private Atlas noIntersectionNode;

    /*
     * Valid intersections tests: Test to following valid intersections
     */

    @TestAtlas(
            /*-
             * With Level Crossing Tag and no layer tags
             *  1.1) highway(edge)/railway=rail(line) intersection
             *  1.2) highway(edge)/railway=tram(line) intersection
             *  1.3) highway(edge)/railway=disused(line) intersection
             *  1.4) highway(edge)/railway=preserved(line) intersection
             *  1.5) highway(edge)/railway=miniature(line) intersection
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {
                            "railway=level_crossing" }),
                    @Node(id = "623456789000000", coordinates = @Loc(value = H2_NODE_1), tags = {}),
                    @Node(id = "723456789000000", coordinates = @Loc(value = H2_NODE_2), tags = {}) },
            // edges
            edges = {
                    // 1.1-1.4: intersecting edge with no layer
                    @Edge(id = "113456789000000", coordinates = { @Loc(value = H1_NODE_1),
                            @Loc(value = H1_NODE_2),
                            @Loc(value = INT1) }, tags = { "highway=secondary" }) },
            // lines
            lines = {
                    // 1.1: intersecting rail no layer
                    @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=rail" }),
                    // 1.2: intersecting tram no layer
                    @Line(id = "233456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=tram" }),
                    // 1.3: intersecting disused no layer
                    @Line(id = "333456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=disused" }),
                    // 1.4: intersecting preserved no layer
                    @Line(id = "433456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=preserved" }),
                    // 1.5: intersecting miniature no layer
                    @Line(id = "533456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=miniature" }) })

    private Atlas validIntersectionNoLayer;

    @TestAtlas(
            /*
             * Ignore intersections with construction. Generally this would fail because the node
             * should be tagged but is not. This test should pass because we want to ignore
             * construction.
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {}) },
            // edges
            edges = {
                    // 1.1-1.4: intersecting edge with no layer
                    @Edge(id = "113456789000000", coordinates = { @Loc(value = H1_NODE_1),
                            @Loc(value = H1_NODE_2), @Loc(value = INT1) }, tags = {
                                    "highway=secondary", "construction:lanes=2" }) },
            // lines
            lines = {
                    // 1.1: intersecting rail no layer
                    @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=rail" }) })

    private Atlas ignoreConstruction;

    @TestAtlas(
            /*
             * Test invalid level crossing with no railway
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {
                            "railway=level_crossing" }) },
            // edges
            edges = { @Edge(id = "113456789000000", coordinates = { @Loc(value = H1_NODE_1),
                    @Loc(value = H1_NODE_2),
                    @Loc(value = INT1) }, tags = { "highway=secondary" }) })

    private Atlas invalidIntersectionNoRailway;

    @TestAtlas(
            /*
             * Test invalid level crossing with no highway
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {
                            "railway=level_crossing" }) },
            // lines
            lines = { @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                    @Loc(value = R_NODE_2), @Loc(value = INT1) }, tags = { "railway=rail" }) })

    private Atlas invalidIntersectionNoHighway;

    @TestAtlas(
            /*
             * Ignore intersections with construction. Generally this would fail because the node
             * should be tagged but is not. This test should pass because we want to ignore
             * construction.
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "323456789000000", coordinates = @Loc(value = H1_NODE_1), tags = {}),
                    @Node(id = "423456789000000", coordinates = @Loc(value = H1_NODE_2), tags = {}),
                    @Node(id = "523456789000000", coordinates = @Loc(value = INT1), tags = {
                            "railway=level_crossing" }) },
            // edges
            edges = {
                    // 1.1-1.4: intersecting edge with no layer
                    @Edge(id = "113456789000000", coordinates = { @Loc(value = H1_NODE_1),
                            @Loc(value = H1_NODE_2),
                            @Loc(value = INT1) }, tags = { "highway=residential" }) },
            // lines
            lines = {
                    // 1.1: intersecting rail no layer
                    @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2),
                            @Loc(value = INT1) }, tags = { "railway=disused", "layer=0" }) })

    private Atlas validIntersectionLayerZero;

    @TestAtlas(
            /*-
             * Valid intersections With no intersection node
             *  3.1) highway(edge)/railway(line) intersection with edge layer > 0
             *  3.2) highway(edge)/railway(line) intersection with edge layer < 0
             *  3.3) highway(edge)/railway(line) intersection with line layer > 0
             *  3.4) highway(edge)/railway(line) intersection with line layer < 0
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "623456789000000", coordinates = @Loc(value = H2_NODE_1), tags = {}),
                    @Node(id = "723456789000000", coordinates = @Loc(value = H2_NODE_2), tags = {}) },
            // edges
            edges = {
                    // 3.3 3.4: no intersection edge with no layer
                    @Edge(id = "113456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary" }),
                    // 3.1: no intersection edge with layer tag = 2
                    @Edge(id = "213456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary", "layer=2" }),
                    // 3.2: no intersection edge with layer tag = -2
                    @Edge(id = "313456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary",
                                    "layer=-2" }) },
            // lines
            lines = {
                    // 3.3: non intersecting rail layer = 1
                    @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2) }, tags = { "railway=rail", "layer=1" }),
                    // 3.4: non intersecting rail layer = -1
                    @Line(id = "233456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2) }, tags = { "railway=rail", "layer=-1" }) })

    private Atlas validIntersectionLayers;

    @TestAtlas(
            /*
             * Bridge Layer test. Test that an intersection with no intersection node on a bridge or
             * tunnel is flagged appropriately.
             */
            // nodes
            nodes = {
                    @Node(id = "123456789000000", coordinates = @Loc(value = R_NODE_1), tags = {}),
                    @Node(id = "223456789000000", coordinates = @Loc(value = R_NODE_2), tags = {}),
                    @Node(id = "623456789000000", coordinates = @Loc(value = H2_NODE_1), tags = {}),
                    @Node(id = "723456789000000", coordinates = @Loc(value = H2_NODE_2), tags = {}) },
            // edges
            edges = {
                    // no intersection edge with bridge tag
                    @Edge(id = "113456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary",
                                    "bridge=yes" }),
                    // no intersection edge with tunnel tag
                    @Edge(id = "213456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary",
                                    "tunnel=yes" }),
                    // no intersection edge with no tag
                    @Edge(id = "313456789000000", coordinates = { @Loc(value = H2_NODE_1),
                            @Loc(value = H2_NODE_2) }, tags = { "highway=secondary" }) },
            // lines
            lines = {
                    // no intersection line with bridge tag
                    @Line(id = "133456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2) }, tags = { "railway=rail", "bridge=yes" }),
                    // no intersection line with tunnel tag
                    @Line(id = "233456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2) }, tags = { "railway=rail", "tunnel=yes" }),
                    // no intersection line with no tag
                    @Line(id = "333456789000000", coordinates = { @Loc(value = R_NODE_1),
                            @Loc(value = R_NODE_2) }, tags = { "railway=rail" }) })

    private Atlas bridgeLayers;

    public Atlas getBridgeLayers()
    {
        return this.bridgeLayers;
    }

    public Atlas getIgnoreConstruction()
    {
        return this.ignoreConstruction;
    }

    public Atlas getInvalidIntersectionNoHighway()
    {
        return this.invalidIntersectionNoHighway;
    }

    public Atlas getInvalidIntersectionNoRailway()
    {
        return this.invalidIntersectionNoRailway;
    }

    public Atlas getInvalidObjectsWithTag()
    {
        return this.invalidObjectsWithTag;
    }

    public Atlas getNoIntersectionNode()
    {
        return this.noIntersectionNode;
    }

    public Atlas getValidIntersectionLayerZero()
    {
        return this.validIntersectionLayerZero;
    }

    public Atlas getValidIntersectionLayers()
    {
        return this.validIntersectionLayers;
    }

    public Atlas getValidIntersectionNoLayer()
    {
        return this.validIntersectionNoLayer;
    }
}
