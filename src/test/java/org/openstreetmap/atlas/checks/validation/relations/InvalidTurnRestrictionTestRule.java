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
 */
public class InvalidTurnRestrictionTestRule extends CoreTestRule
{
    // Laguna en Medio
    protected static final String ONE = "18.4360044, -71.7194204";
    protected static final String TWO = "18.4360737, -71.6970306";
    protected static final String THREE = "18.4273807, -71.7052283";

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
}
