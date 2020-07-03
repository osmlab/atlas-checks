package org.openstreetmap.atlas.checks.validation.relations;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test atlases for InvalidTurnRestriction check test
 *
 * @author gpogulsky
 * @author bbreithaupt
 */
public class InvalidTurnRestrictionTestRule extends CoreTestRule
{
    // Laguna en Medio
    protected static final String ONE = "18.4360044, -71.7194204";
    protected static final String TWO = "18.4360737, -71.6970306";
    protected static final String THREE = "18.4273807, -71.7052283";

    private static final String TEST_1 = "47.9601381695206,-122.691874175526";
    private static final String TEST_2 = "47.9603648999763,-122.691773321146";
    private static final String TEST_3 = "47.9603793720993,-122.691746906904";
    private static final String TEST_4 = "47.9603584679204,-122.69140112046";
    private static final String TEST_5 = "47.9605868054162,-122.691672466767";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=road" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=road" }),
                    @Edge(id = "31", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "2", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "23", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                            "restriction=no_u_turn" }) })
    private Atlas goodAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=road" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=road" }),
                    @Edge(id = "31", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "highway=road" }) },
            // relations - TO edge doesn't go through VIA node
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "2", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "31", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                            "restriction=no_u_turn" }) })
    private Atlas invalidRelationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=road", "oneway=true" }),
                    @Edge(id = "32", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=road", "oneway=true" }) },
            // relations - 2 one-way edges meeting at the VIA node with no turns allowed
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "2", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "32", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                            "restriction=only_straight_on" }) })
    private Atlas invalidAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_3)) },
            // relations
            relations = {
                    @Relation(members = @Member(id = "1000000", type = "node", role = "via"), tags = {
                            "type=restriction", "restriction=only_right_turn" }) })
    private Atlas onlyViaAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_3) }) },
            // relations
            relations = { @Relation(members = { @Member(id = "1000000", type = "edge", role = "to"),
                    @Member(id = "1000000", type = "edge", role = "from") }, tags = {
                            "type=restriction", "restriction=no_u_turn" }) })
    private Atlas sameFromToNoViaAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = TEST_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_4) }) },
            // relations
            relations = { @Relation(members = { @Member(id = "1000000", type = "edge", role = "to"),
                    @Member(id = "2000000", type = "edge", role = "from"),
                    @Member(id = "2000000", type = "node", role = "via"),
                    @Member(id = "3000000", type = "node", role = "via") }, tags = {
                            "type=restriction", "restriction=only_right_turn" }) })
    private Atlas doubleViaAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = TEST_4)),
                    @Node(id = "5000000", coordinates = @Loc(value = TEST_5)), },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }) },
            // relations
            relations = { @Relation(members = { @Member(id = "1000000", type = "edge", role = "to"),
                    @Member(id = "2000000", type = "edge", role = "from"),
                    @Member(id = "3000000", type = "edge", role = "from"),
                    @Member(id = "3000000", type = "node", role = "via") }, tags = {
                            "type=restriction", "restriction=only_right_turn" }) })
    private Atlas doubleFromAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = TEST_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = TEST_4)),
                    @Node(id = "5000000", coordinates = @Loc(value = TEST_5)), },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_5) }) },
            // relations
            relations = { @Relation(members = { @Member(id = "1000000", type = "edge", role = "to"),
                    @Member(id = "2000000", type = "edge", role = "from"),
                    @Member(id = "3000000", type = "edge", role = "from"),
                    @Member(id = "3000000", type = "node", role = "via") }, tags = {
                            "type=restriction", "restriction=only_right_turn" }) })
    private Atlas disconnectedFromAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST_3)),
                    @Node(id = "5000000", coordinates = @Loc(value = TEST_5)), },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_3) }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_3) }) },
            // relations
            relations = { @Relation(members = { @Member(id = "1000000", type = "edge", role = "to"),
                    @Member(id = "2000000", type = "edge", role = "from"),
                    @Member(id = "3000000", type = "node", role = "via") }, tags = {
                            "type=restriction", "restriction=only_right_turn" }) })
    private Atlas redundantRestrictionAtlas;

    public Atlas disconnectedFromAtlas()
    {
        return this.disconnectedFromAtlas;
    }

    public Atlas doubleFromAtlas()
    {
        return this.doubleFromAtlas;
    }

    public Atlas doubleViaAtlas()
    {
        return this.doubleViaAtlas;
    }

    public Atlas getGoodAtlas()
    {
        return this.goodAtlas;
    }

    public Atlas getInvalidAtlas()
    {
        return this.invalidAtlas;
    }

    public Atlas getInvalidRelationAtlas()
    {
        return this.invalidRelationAtlas;
    }

    public Atlas onlyViaAtlas()
    {
        return this.onlyViaAtlas;
    }

    public Atlas redundantRestrictionAtlas()
    {
        return this.redundantRestrictionAtlas;
    }

    public Atlas sameFromToNoViaAtlas()
    {
        return this.sameFromToNoViaAtlas;
    }
}
