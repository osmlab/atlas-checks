package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link BoundaryIntersectionsCheckTest} data generator
 *
 * @author srachanski
 */
public class BoundaryIntersectionsCheckTestRule extends CoreTestRule {
    private static final String COORD_1 = "0, 0";
    private static final String COORD_2 = "0, 2";
    private static final String COORD_3 = "2, 2";
    private static final String COORD_4 = "2, 0";
    private static final String COORD_5 = "0, 3";
    private static final String COORD_6 = "3, 3";
    private static final String COORD_7 = "0, 0";
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
    
    
    private static final String EDGE_ONE = "1000001";
    private static final String EDGE_TWO = "2000001";
    private static final String EDGE_THREE = "3000001";
    private static final String EDGE_FOUR = "4000001";
    private static final String EDGE_FIVE = "5000001";
    private static final String EDGE_SIX = "6000001";
    private static final String EDGE_SEVEN = "7000001";
    private static final String EDGE_EIGHT = "8000001";
    private static final String EDGE_NINE = "9000001";
    
    private static final String RELATION_ONE = "1000011";
    private static final String RELATION_TWO = "2000011";
    private static final String RELATION_THREE = "3000011";
    
    @TestAtlas(
            nodes = {
                    @Node(coordinates = @Loc(value = COORD_1)),
                    @Node(coordinates = @Loc(value = COORD_2)),
                    @Node(coordinates = @Loc(value = COORD_3)),
                    @Node(coordinates = @Loc(value = COORD_4)),
                    @Node(coordinates = @Loc(value = COORD_5)),
                    @Node(coordinates = @Loc(value = COORD_6)),
                    @Node(coordinates = @Loc(value = COORD_7))
            },
            edges = {
                    @Edge(coordinates = {
                            @Loc(value = COORD_1),
                            @Loc(value = COORD_2),
                            @Loc(value = COORD_3),
                            @Loc(value = COORD_4),
                            @Loc(value = COORD_1)},
                            tags = {"highway=motorway"},
                            id = EDGE_ONE),
                    @Edge(coordinates = {
                            @Loc(value = COORD_1),
                            @Loc(value = COORD_5),
                            @Loc(value = COORD_6),
                            @Loc(value = COORD_7),
                            @Loc(value = COORD_1)},
                            tags = {"highway=motorway"},
                            id = EDGE_TWO)},
            relations = {
                    @Relation(id = RELATION_ONE,
                            members = {
                                    @Relation.Member(id = EDGE_ONE, role = "outer", type = "EDGE")}, tags = {
                            "type=boundary",
                            "boundary=administrative"}),
                    @Relation(id = RELATION_TWO,
                            members = {
                                    @Relation.Member(id = EDGE_TWO, role = "outer", type = "EDGE")}, tags = {
                            "type=boundary",
                            "boundary=administrative"})})
    private Atlas crossingBoundariesTwoAreasIntersectEachOther;
    
    @TestAtlas(
            nodes = {
                    @Node(coordinates = @Loc(value = COORD_8)),
                    @Node(coordinates = @Loc(value = COORD_9)),
                    @Node(coordinates = @Loc(value = COORD_10)),
                    @Node(coordinates = @Loc(value = COORD_11)),
                    @Node(coordinates = @Loc(value = COORD_12)),
                    @Node(coordinates = @Loc(value = COORD_13)),
                    @Node(coordinates = @Loc(value = COORD_14)),
                    @Node(coordinates = @Loc(value = COORD_15))
            },
            edges = {
                    @Edge(coordinates = {
                            @Loc(value = COORD_8),
                            @Loc(value = COORD_9),
                            @Loc(value = COORD_10),
                            @Loc(value = COORD_11),
                            @Loc(value = COORD_8)},
                            tags = {"highway=motorway"},
                            id = EDGE_ONE),
                    @Edge(coordinates = {
                            @Loc(value = COORD_12),
                            @Loc(value = COORD_13),
                            @Loc(value = COORD_14),
                            @Loc(value = COORD_15),
                            @Loc(value = COORD_12)},
                            tags = {"highway=motorway"},
                            id = EDGE_TWO)},
            relations = {
                    @Relation(id = RELATION_ONE,
                            members = {
                                    @Relation.Member(id = EDGE_ONE, role = "outer", type = "EDGE")}, tags = {
                            "type=boundary",
                            "boundary=administrative"}),
                    @Relation(id = RELATION_TWO,
                            members = {
                                    @Relation.Member(id = EDGE_TWO, role = "outer", type = "EDGE")}, tags = {
                            "type=boundary",
                            "boundary=administrative"})})
    private Atlas nonCrossingBoundariesTwoSeparate;
    
    @TestAtlas(
            nodes = {
                    @Node(coordinates = @Loc(value = COORD_8)),
                    @Node(coordinates = @Loc(value = COORD_9)),
                    @Node(coordinates = @Loc(value = COORD_10)),
                    @Node(coordinates = @Loc(value = COORD_11)),
                    @Node(coordinates = @Loc(value = COORD_12)),
                    @Node(coordinates = @Loc(value = COORD_13)),
                    @Node(coordinates = @Loc(value = COORD_14)),
                    @Node(coordinates = @Loc(value = COORD_15)),
                    @Node(coordinates = @Loc(value = COORD_16)),
                    @Node(coordinates = @Loc(value = COORD_17)),
                    @Node(coordinates = @Loc(value = COORD_18)),
                    @Node(coordinates = @Loc(value = COORD_19))
            },
            edges = {
                    @Edge(coordinates = {
                            @Loc(value = COORD_8),
                            @Loc(value = COORD_9)},
                            id = EDGE_ONE),
                    @Edge(coordinates = {
                            @Loc(value = COORD_9),
                            @Loc(value = COORD_10)},
                            id = EDGE_TWO),
                    @Edge(coordinates = {
                            @Loc(value = COORD_10),
                            @Loc(value = COORD_11)},
                            id = EDGE_THREE),
                    @Edge(coordinates = {
                            @Loc(value = COORD_11),
                            @Loc(value = COORD_8)},
                            id = EDGE_FOUR),
                    @Edge(coordinates = {
                            @Loc(value = COORD_12),
                            @Loc(value = COORD_13),
                            @Loc(value = COORD_14),
                            @Loc(value = COORD_15),
                            @Loc(value = COORD_12)},
                            id = EDGE_FIVE),
                    @Edge(coordinates = {
                            @Loc(value = COORD_16),
                            @Loc(value = COORD_17)},
                            id = EDGE_SIX),
                    @Edge(coordinates = {
                            @Loc(value = COORD_17),
                            @Loc(value = COORD_18)},
                            id = EDGE_SEVEN),
                    @Edge(coordinates = {
                            @Loc(value = COORD_18),
                            @Loc(value = COORD_19)},
                            id = EDGE_EIGHT),
                    @Edge(coordinates = {
                            @Loc(value = COORD_19),
                            @Loc(value = COORD_16)},
                            id = EDGE_NINE)},
            relations = {
                    @Relation(id = RELATION_ONE,
                            members = {
                                    @Relation.Member(id = EDGE_ONE, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_TWO, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_THREE, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_FOUR, role = "outer", type = "EDGE")
                            },
                            tags = {
                                    "type=boundary",
                                    "boundary=administrative"}),
                    @Relation(id = RELATION_TWO,
                            members = {
                                    @Relation.Member(id = EDGE_FIVE, role = "outer", type = "EDGE")},
                            tags = {
                                    "type=boundary",
                                    "boundary=maritime"}),
                    @Relation(id = RELATION_THREE,
                            members = {
                                    @Relation.Member(id = EDGE_SIX, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_SEVEN, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_EIGHT, role = "outer", type = "EDGE"),
                                    @Relation.Member(id = EDGE_NINE, role = "outer", type = "EDGE")
                            },
                            tags = {
                                    "type=boundary",
                                    "boundary=political"})})
    private Atlas crossingBoundariesTwoAreasIntersectOneOther;
    
    public Atlas crossingBoundariesTwoAreasIntersectEachOther() {
        return this.crossingBoundariesTwoAreasIntersectEachOther;
    }
    
    public Atlas nonCrossingBoundariesTwoSeparate() {
        return this.nonCrossingBoundariesTwoSeparate;
    }
    
    public Atlas crossingBoundariesTwoAreasIntersectOneOther() {
        return this.crossingBoundariesTwoAreasIntersectOneOther;
    }
    
}
