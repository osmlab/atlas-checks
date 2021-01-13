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
 * {@link MissingRelationTypeCheckTest} data generator
 *
 * @author vlemberg
 */
public class MissingRelationTypeCheckTestRule extends CoreTestRule
{
    private static final String ONE = "18.4360044, -71.7194204";
    private static final String TWO = "18.4360737, -71.6970306";
    private static final String THREE = "18.4273807, -71.7052283";
    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "123", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "type=any" }) })
    private Atlas validRelation;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "123", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }) })
    private Atlas missingRelationType;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @TestAtlas.Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @TestAtlas.Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "123", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "disused:type=any" }) })
    private Atlas missingRelationTypeDisused;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @TestAtlas.Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }),
                    @TestAtlas.Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @TestAtlas.Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "123", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "disabled:type=any" }) })
    private Atlas missingRelationTypeDisabled;

    public Atlas getMissingRelationType()
    {
        return this.missingRelationType;
    }

    public Atlas getMissingRelationTypeDisabled()
    {
        return this.missingRelationTypeDisabled;
    }

    public Atlas getMissingRelationTypeDisused()
    {
        return this.missingRelationTypeDisused;
    }

    public Atlas getValidRelation()
    {
        return this.validRelation;
    }
}
