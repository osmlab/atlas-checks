package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test data for {@link SelfIntersectingPolyLineCheckTest}
 *
 * @author mgostintsev
 */
public class SelfIntersectingPolylineTestCaseRule extends CoreTestRule
{
    public static final String VALID_EDGE_ID = "101740465";
    public static final String VALID_LINE_ID = "102740465";
    public static final String VALID_LINE_ID_2 = "10283065";
    public static final String VALID_LINE_ID_3 = "10284065";
    public static final String VALID_AREA_ID = "103740465";
    public static final String INVALID_EDGE_ID_1 = "104740465";
    public static final String INVALID_EDGE_ID_2 = "105540465";
    public static final String INVALID_EDGE_ID_3 = "105640465";
    public static final String INVALID_EDGE_ID_4 = "105740465";
    public static final String INVALID_LINE_ID_1 = "106740465";
    public static final String INVALID_LINE_ID_2 = "107740465";
    public static final String INVALID_AREA_ID_1 = "108740465";
    public static final String INVALID_AREA_ID_2 = "109740465";
    public static final String INVALID_AREA_ID_3 = "109840465";
    private static final String ONE = "-0.8385242, -80.4892702";
    private static final String TWO = "-0.8385546, -80.4891487";
    private static final String THREE = "-0.8386005, -80.4892233";
    private static final String FOUR = "-0.8384783, -80.4891956";
    private static final String FIVE = "-0.83855, -80.48922";

    // Valid Line with no self-intersections
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = VALID_LINE_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO) }) })
    private Atlas validLineNoSelfIntersection;

    // Invalid Line with a non-shape self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = INVALID_LINE_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR) }) })
    private Atlas invalidLineNonShapeSelfIntersection;

    // Invalid Line with a shape-point self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = INVALID_LINE_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR) }) })
    private Atlas invalidLineShapePointSelfIntersection;

    // Invalid Line geometry with Waterway tag
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = VALID_LINE_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "waterway=river" }) })
    private Atlas invalidLineGeometryWaterwayTag;

    // Invalid Line geometry with highway=footway tag
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = VALID_LINE_ID_3, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=footway" }) })
    private Atlas invalidLineGeometryHighwayFootwayTag;

    // Valid Edge with no self-intersections
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = VALID_EDGE_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO) }, tags = { "highway=trunk" }) })
    private Atlas validEdgeNoSelfIntersection;

    // Invalid Edge with a non-shape self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)),
            @Node(id = "4", coordinates = @Loc(value = FOUR)) }, edges = {
                    @Edge(id = INVALID_EDGE_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }) })
    private Atlas invalidEdgeNonShapeIntersection;

    // Invalid Edge with a shape-point self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)),
            @Node(id = "4", coordinates = @Loc(value = FOUR)),
            @Node(id = "5", coordinates = @Loc(value = FIVE)) }, edges = {
                    @Edge(id = INVALID_EDGE_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR) }, tags = { "highway=trunk" }) })
    private Atlas invalidEdgeShapeIntersection;

    // Invalid Edge geometry with highway=primary tag
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)),
            @Node(id = "4", coordinates = @Loc(value = FOUR)),
            @Node(id = "5", coordinates = @Loc(value = FIVE)) }, edges = {
                    @Edge(id = INVALID_EDGE_ID_3, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE),
                            @Loc(value = FOUR) }, tags = { "highway=primary" }) })
    private Atlas invalidEdgeGeometryHighwayPrimaryTag;

    // Invalid Edge geometry with Building tag
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)),
            @Node(id = "4", coordinates = @Loc(value = FOUR)) }, edges = {
                    @Edge(id = INVALID_EDGE_ID_4, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "building=yes" }) })
    private Atlas invalidEdgeGeometryBuildingTag;

    // Valid Area with no self-intersections
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, areas = {
                    @Area(id = VALID_AREA_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }) })
    private Atlas validAreaNoSelfIntersection;

    // Invalid Area with a non shape-point self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, areas = {
                    @Area(id = INVALID_AREA_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }) })
    private Atlas invalidAreaNonShapeSelfIntersection;

    // Invalid Area with a shape point self-intersection
    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, areas = {
                    @Area(id = INVALID_AREA_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }) })
    private Atlas invalidAreaShapeIntersection;

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)) }, areas = {
                    @Area(id = INVALID_AREA_ID_3, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "building=yes" }) })
    private Atlas invalidAreaBuildingTag;

    public Atlas getValidLineNoSelfIntersection()
    {
        return this.validLineNoSelfIntersection;
    }

    public Atlas getInvalidLineNonShapeSelfIntersection()
    {
        return this.invalidLineNonShapeSelfIntersection;
    }

    public Atlas getInvalidLineShapePointSelfIntersection()
    {
        return this.invalidLineShapePointSelfIntersection;
    }

    public Atlas getInvalidLineGeometryWaterwayTag()
    {
        return this.invalidLineGeometryWaterwayTag;
    }

    public Atlas getInvalidLineGeometryHighwayFootwayTag()
    {
        return this.invalidLineGeometryHighwayFootwayTag;
    }

    public Atlas getValidEdgeNoSelfIntersection()
    {
        return this.validEdgeNoSelfIntersection;
    }

    public Atlas getInvalidEdgeNonShapeIntersection()
    {
        return this.invalidEdgeNonShapeIntersection;
    }

    public Atlas getInvalidEdgeShapeIntersection()
    {
        return this.invalidEdgeShapeIntersection;
    }

    public Atlas getInvalidEdgeGeometryHighwayPrimaryTag()
    {
        return this.invalidEdgeGeometryHighwayPrimaryTag;
    }

    public Atlas getInvalidEdgeGeometryBuildingTag()
    {
        return this.invalidEdgeGeometryBuildingTag;
    }

    public Atlas getValidAreaNoSelfIntersection()
    {
        return this.validAreaNoSelfIntersection;
    }

    public Atlas getInvalidAreaNonShapeSelfIntersection()
    {
        return this.invalidAreaNonShapeSelfIntersection;
    }

    public Atlas getInvalidAreaShapeIntersection()
    {
        return this.invalidAreaShapeIntersection;
    }

    public Atlas getInvalidAreaBuildingTag()
    {
        return this.invalidAreaBuildingTag;
    }
}
