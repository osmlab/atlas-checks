package org.openstreetmap.atlas.checks.validation.relations;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Tests for {@link OpenBoundaryCheck}
 *
 * @author v-garei
 */
public class OpenBoundaryCheckTestRule extends CoreTestRule
{

    private static final String POLYGON_NODE_1 = "47.6367419, -122.2342792";
    private static final String POLYGON_NODE_2 = "47.6370622, -122.2360741";
    private static final String POLYGON_NODE_3 = "47.6372561, -122.2370973";

    private static final String POLYGON_2_NODE_1 = "47.6364969, -122.2379916";
    private static final String POLYGON_2_NODE_2 = "47.6365008, -122.2371735";
    private static final String POLYGON_2_NODE_3 = "47.6357896, -122.2371660";
    private static final String POLYGON_2_NODE_4 = "47.6357857, -122.2379841";

    private static final String RELATION_ID_OPEN_MULTIPOLYGON = "123";
    private static final String RELATION_ID_OPEN_MULTIPOLYGON2 = "124";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = POLYGON_2_NODE_1)),
                    @Node(coordinates = @Loc(value = POLYGON_2_NODE_2)),
                    @Node(coordinates = @Loc(value = POLYGON_2_NODE_3)),
                    @Node(coordinates = @Loc(value = POLYGON_2_NODE_4)) },
            // lines
            edges = {
                    @Edge(id = "2000001", coordinates = { @TestAtlas.Loc(value = POLYGON_2_NODE_1),
                            @Loc(value = POLYGON_2_NODE_2) }, tags = { "natural=water" }),
                    @Edge(id = "2000002", coordinates = { @TestAtlas.Loc(value = POLYGON_2_NODE_2),
                            @Loc(value = POLYGON_2_NODE_3) }, tags = { "natural=water" }),
                    @Edge(id = "2000003", coordinates = { @TestAtlas.Loc(value = POLYGON_2_NODE_3),
                            @Loc(value = POLYGON_2_NODE_4) }, tags = { "natural=water" }),
                    @Edge(id = "2000004", coordinates = { @TestAtlas.Loc(value = POLYGON_2_NODE_4),
                            @Loc(value = POLYGON_2_NODE_1) }, tags = { "natural=water" }) },
            // relations
            relations = { @Relation(id = RELATION_ID_OPEN_MULTIPOLYGON2, members = {
                    @Member(id = "2000001", type = "edge", role = "outer"),
                    @Member(id = "2000002", type = "edge", role = "outer"),
                    @Member(id = "2000003", type = "edge", role = "outer"),
                    @Member(id = "2000004", type = "edge", role = "outer") }, tags = {
                            "type=boundary", "admin_level=3" }) })

    private Atlas polygonClosedFalsePositive;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = POLYGON_NODE_1)),
                    @Node(coordinates = @Loc(value = POLYGON_NODE_2)),
                    @Node(coordinates = @Loc(value = POLYGON_NODE_3)) },
            // lines
            edges = {
                    @Edge(id = "1000001", coordinates = { @TestAtlas.Loc(value = POLYGON_NODE_1),
                            @Loc(value = POLYGON_NODE_2) }),
                    @Edge(id = "1000002", coordinates = { @TestAtlas.Loc(value = POLYGON_NODE_2),
                            @Loc(value = POLYGON_NODE_3) }), },
            // relations
            relations = { @Relation(id = RELATION_ID_OPEN_MULTIPOLYGON, members = {
                    @Member(id = "1000001", type = "edge", role = "outer"),
                    @Member(id = "1000002", type = "edge", role = "outer"), }, tags = {
                            "type=boundary", "admin_level=3" }) })

    private Atlas polygonNotClosedTruePositive;

    public Atlas polygonClosedFalsePositive()
    {
        return this.polygonClosedFalsePositive;
    }

    public Atlas polygonNotClosedTruePositive()
    {
        return this.polygonNotClosedTruePositive;
    }

}
