package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link LoneBuildingNodeCheckTest} data generator
 *
 * @author vladlemberg
 */

public class LoneBuildingNodeCheckTestRule extends CoreTestRule
{

    private static final String BUILDING_1 = "37.3260680,-121.9199798";
    private static final String BUILDING_2 = "37.3261021,-121.9200220";
    private static final String BUILDING_3 = "37.3261418,-121.9199718";
    private static final String BUILDING_4 = "37.3261076,-121.9199296";
    private static final String BUILDING_NODE = "37.3260911,-121.9199859";

    @TestAtlas(
            // points
            points = { @Point(id = "500000", coordinates = @Loc(value = BUILDING_NODE), tags = {
                    "building=yes" }) })
    private Atlas loneBuildingNodeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = BUILDING_4)) },
            // points
            points = { @Point(id = "5000000", coordinates = @Loc(value = BUILDING_NODE), tags = {
                    "building=yes", "addr:housenumber=123" }) },

            // areas
            areas = { @Area(id = "600000", coordinates = { @Loc(value = BUILDING_1),
                    @Loc(value = BUILDING_2), @Loc(value = BUILDING_3), @Loc(value = BUILDING_4),
                    @Loc(value = BUILDING_1) }, tags = { "building=yes" }) })
    private Atlas enclosedBuildingAreaNodeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = BUILDING_4)) },

            // points
            points = { @Point(id = "5000000", coordinates = @Loc(value = BUILDING_NODE), tags = {
                    "building=yes", "addr:housenumber=123" }) },

            // edges
            edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = BUILDING_1),
                            @Loc(value = BUILDING_2) }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = BUILDING_2),
                            @Loc(value = BUILDING_3) }),
                    @Edge(id = "34000000", coordinates = { @Loc(value = BUILDING_3),
                            @Loc(value = BUILDING_4) }),
                    @Edge(id = "41000000", coordinates = { @Loc(value = BUILDING_4),
                            @Loc(value = BUILDING_1) }) },

            // relations
            relations = { @Relation(id = "1234000000", members = {
                    @Member(id = "12000000", type = "edge", role = "outer"),
                    @Member(id = "23000000", type = "edge", role = "outer"),
                    @Member(id = "34000000", type = "edge", role = "outer"),
                    @Member(id = "41000000", type = "edge", role = "outer") }, tags = {
                            "type=multipolygon", "building=yes" }) })
    private Atlas enclosedBuildingRelationNodeAtlas;

    public Atlas enclosedBuildingAreaNodeAtlas()
    {
        return this.enclosedBuildingAreaNodeAtlas;
    }

    public Atlas enclosedBuildingRelationNodeAtlas()
    {
        return this.enclosedBuildingRelationNodeAtlas;
    }

    public Atlas loneBuildingNodeAtlas()
    {
        return this.loneBuildingNodeAtlas;
    }
}
