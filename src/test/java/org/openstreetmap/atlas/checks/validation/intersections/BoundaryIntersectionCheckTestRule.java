package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;

/**
 * {@link BoundaryIntersectionCheckTest} data generator
 *
 * @author srachanski
 */
public class BoundaryIntersectionCheckTestRule extends CoreTestRule
{
    private static final String COORD_1 = "0, 0";
    private static final String COORD_2 = "0, 2";
    private static final String COORD_3 = "2, 2";
    private static final String COORD_4 = "2, 0";
    private static final String COORD_5 = "0, 3";
    private static final String COORD_6 = "3, 3";
    private static final String COORD_7 = "3, 0";
    private static final String COORD_8 = "0, 1";
    private static final String COORD_9 = "0, 2";
    private static final String COORD_10 = "2, 2";
    private static final String COORD_11 = "2, 1";
    private static final String COORD_12 = "3, 1";
    private static final String COORD_13 = "3, 2";
    private static final String COORD_14 = "5, 2";
    private static final String COORD_15 = "5, 1";
    private static final String COORD_16 = "1, 0";
    private static final String COORD_17 = "1, 3";
    private static final String COORD_18 = "4, 3";
    private static final String COORD_19 = "4, 0";
    private static final String COORD_20 = "1, 1";
    private static final String COORD_21 = "1, 2";

    private static final String LINE_ONE = "1000001";
    private static final String LINE_TWO = "2000001";
    private static final String LINE_THREE = "3000001";
    private static final String LINE_FOUR = "4000001";
    private static final String LINE_FIVE = "5000001";
    private static final String LINE_SIX = "6000001";
    private static final String LINE_SEVEN = "7000001";
    private static final String LINE_EIGHT = "8000001";
    private static final String LINE_NINE = "9000001";

    private static final String EDGE_ONE = "11000001";
    private static final String EDGE_TWO = "12000001";

    private static final String RELATION_ONE = "21000011";
    private static final String RELATION_TWO = "22000011";
    private static final String RELATION_THREE = "23000011";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_7)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_5),
                            @Loc(value = COORD_6), @Loc(value = COORD_7),
                            @Loc(value = COORD_1) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas crossingBoundariesTwoAreasTouchEachOther;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_7)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_15),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_5),
                            @Loc(value = COORD_6),
                            @Loc(value = COORD_1) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=political" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas crossingBoundariesWithDifferentTypes;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_20), @Loc(value = COORD_5),
                            @Loc(value = COORD_6),
                            @Loc(value = COORD_20) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas crossingBoundariesTwoAreasIntersectEachOther;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_20)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE, tags = { "type=boundary",
                                    "boundary=administrative" }),
                    @Line(coordinates = { @Loc(value = COORD_20), @Loc(value = COORD_5),
                            @Loc(value = COORD_6), @Loc(value = COORD_1) }, id = LINE_TWO, tags = {
                                    "type=boundary", "boundary=administrative" }) }, relations = {
                                            @Relation(id = RELATION_ONE, members = {
                                                    @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }),
                                            @Relation(id = RELATION_TWO, members = {
                                                    @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }) })
    private Atlas crossingBoundariesWithOnlyTagsOnWays;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_20)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = EDGE_ONE),
                    @Edge(coordinates = { @Loc(value = COORD_20), @Loc(value = COORD_5),
                            @Loc(value = COORD_6),
                            @Loc(value = COORD_20) }, id = EDGE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = EDGE_ONE, role = "outer", type = "edge") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = EDGE_TWO, role = "outer", type = "edge") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas crossingBoundariesTwoAreasIntersectEachOtherWithEdges;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_8)),
            @Node(coordinates = @Loc(value = COORD_9)), @Node(coordinates = @Loc(value = COORD_10)),
            @Node(coordinates = @Loc(value = COORD_11)),
            @Node(coordinates = @Loc(value = COORD_12)),
            @Node(coordinates = @Loc(value = COORD_13)),
            @Node(coordinates = @Loc(value = COORD_14)),
            @Node(coordinates = @Loc(value = COORD_15)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_8), @Loc(value = COORD_9),
                            @Loc(value = COORD_10), @Loc(value = COORD_11),
                            @Loc(value = COORD_8) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_12), @Loc(value = COORD_13),
                            @Loc(value = COORD_14), @Loc(value = COORD_15),
                            @Loc(value = COORD_12) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas nonCrossingBoundariesTwoSeparate;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_8)),
            @Node(coordinates = @Loc(value = COORD_9)), @Node(coordinates = @Loc(value = COORD_10)),
            @Node(coordinates = @Loc(value = COORD_11)),
            @Node(coordinates = @Loc(value = COORD_12)),
            @Node(coordinates = @Loc(value = COORD_13)),
            @Node(coordinates = @Loc(value = COORD_14)),
            @Node(coordinates = @Loc(value = COORD_15)) }, edges = {
                    @Edge(coordinates = { @Loc(value = COORD_8), @Loc(value = COORD_9),
                            @Loc(value = COORD_10), @Loc(value = COORD_11),
                            @Loc(value = COORD_8) }, id = EDGE_ONE),
                    @Edge(coordinates = { @Loc(value = COORD_12), @Loc(value = COORD_13),
                            @Loc(value = COORD_14), @Loc(value = COORD_15),
                            @Loc(value = COORD_12) }, id = EDGE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = EDGE_ONE, role = "outer", type = "edge") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = EDGE_TWO, role = "outer", type = "edge") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas nonCrossingBoundariesTwoSeparateWithEdges;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_8)),
            @Node(coordinates = @Loc(value = COORD_9)), @Node(coordinates = @Loc(value = COORD_10)),
            @Node(coordinates = @Loc(value = COORD_11)),
            @Node(coordinates = @Loc(value = COORD_12)),
            @Node(coordinates = @Loc(value = COORD_13)),
            @Node(coordinates = @Loc(value = COORD_14)),
            @Node(coordinates = @Loc(value = COORD_15)),
            @Node(coordinates = @Loc(value = COORD_16)),
            @Node(coordinates = @Loc(value = COORD_17)),
            @Node(coordinates = @Loc(value = COORD_18)),
            @Node(coordinates = @Loc(value = COORD_19)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_8),
                            @Loc(value = COORD_9) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_9),
                            @Loc(value = COORD_10) }, id = LINE_TWO),
                    @Line(coordinates = { @Loc(value = COORD_10),
                            @Loc(value = COORD_11) }, id = LINE_THREE),
                    @Line(coordinates = { @Loc(value = COORD_11),
                            @Loc(value = COORD_8) }, id = LINE_FOUR),
                    @Line(coordinates = { @Loc(value = COORD_12), @Loc(value = COORD_13),
                            @Loc(value = COORD_14), @Loc(value = COORD_15),
                            @Loc(value = COORD_12) }, id = LINE_FIVE),
                    @Line(coordinates = { @Loc(value = COORD_16),
                            @Loc(value = COORD_17) }, id = LINE_SIX),
                    @Line(coordinates = { @Loc(value = COORD_17),
                            @Loc(value = COORD_18) }, id = LINE_SEVEN),
                    @Line(coordinates = { @Loc(value = COORD_18),
                            @Loc(value = COORD_19) }, id = LINE_EIGHT),
                    @Line(coordinates = { @Loc(value = COORD_19),
                            @Loc(value = COORD_16) }, id = LINE_NINE) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_THREE, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_FOUR, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_FIVE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_THREE, members = {
                                            @Relation.Member(id = LINE_SIX, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_SEVEN, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_EIGHT, role = "outer", type = "line"),
                                            @Relation.Member(id = LINE_NINE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas crossingBoundariesTwoAreasIntersectOneOther;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_7)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_19),
                            @Loc(value = COORD_6), @Loc(value = COORD_5),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_20), @Loc(value = COORD_11),
                            @Loc(value = COORD_3), @Loc(value = COORD_21),
                            @Loc(value = COORD_20) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }) })
    private Atlas nonCrossingOneContainOther;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_7)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_5),
                            @Loc(value = COORD_6), @Loc(value = COORD_7),
                            @Loc(value = COORD_1) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=nonBoundary",
                                                    "boundary=administrative" }) })
    private Atlas crossingOneWithWrongType;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = COORD_1)),
            @Node(coordinates = @Loc(value = COORD_2)), @Node(coordinates = @Loc(value = COORD_3)),
            @Node(coordinates = @Loc(value = COORD_4)), @Node(coordinates = @Loc(value = COORD_5)),
            @Node(coordinates = @Loc(value = COORD_6)),
            @Node(coordinates = @Loc(value = COORD_7)) }, lines = {
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_2),
                            @Loc(value = COORD_3), @Loc(value = COORD_4),
                            @Loc(value = COORD_1) }, id = LINE_ONE),
                    @Line(coordinates = { @Loc(value = COORD_1), @Loc(value = COORD_5),
                            @Loc(value = COORD_6), @Loc(value = COORD_7),
                            @Loc(value = COORD_1) }, id = LINE_TWO) }, relations = {
                                    @Relation(id = RELATION_ONE, members = {
                                            @Relation.Member(id = LINE_ONE, role = "outer", type = "line") }, tags = {
                                                    "type=boundary", "boundary=administrative" }),
                                    @Relation(id = RELATION_TWO, members = {
                                            @Relation.Member(id = LINE_TWO, role = "outer", type = "line") }, tags = {
                                                    "type=nonBoundary" }) })
    private Atlas crossingOneMissingBoundarySpecificTag;

    public Atlas boundariesTouchEachOther()
    {
        return this.crossingBoundariesTwoAreasTouchEachOther;
    }

    public Atlas crossingBoundariesTwoAreasIntersectEachOther()
    {
        return this.crossingBoundariesTwoAreasIntersectEachOther;
    }

    public Atlas crossingBoundariesTwoAreasIntersectEachOtherWithEdges()
    {
        return this.crossingBoundariesTwoAreasIntersectEachOtherWithEdges;
    }

    public Atlas crossingBoundariesTwoAreasIntersectOneOther()
    {
        return this.crossingBoundariesTwoAreasIntersectOneOther;
    }

    public Atlas crossingBoundariesWithDifferentTypes()
    {
        return this.crossingBoundariesWithDifferentTypes;
    }

    public Atlas crossingBoundariesWithOnlyTagsOnWays()
    {
        return this.crossingBoundariesWithOnlyTagsOnWays;
    }

    public Atlas crossingOneMissingBoundarySpecificTag()
    {
        return this.crossingOneMissingBoundarySpecificTag;
    }

    public Atlas crossingOneWithWrongType()
    {
        return this.crossingOneWithWrongType;
    }

    public Atlas nonCrossingBoundariesTwoSeparate()
    {
        return this.nonCrossingBoundariesTwoSeparate;
    }

    public Atlas nonCrossingBoundariesTwoSeparateWithEdges()
    {
        return this.nonCrossingBoundariesTwoSeparateWithEdges;
    }

    public Atlas nonCrossingOneContainOther()
    {
        return this.nonCrossingOneContainOther;
    }
}
