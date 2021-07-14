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
 * {@link DuplicateRelationCheckTest} data generator
 *
 * @author Xiaohong Tang
 */
public class DuplicateRelationCheckTestRule extends CoreTestRule
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
                                                    "type=any" }),
                                    @Relation(id = "124", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any") }, tags = {
                                                    "type=any" }) })
    private Atlas differentMembersRelations;

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
                                                    "type=any", "last_edit_user_name=any",
                                                    "last_edit_changeset=43406696",
                                                    "last_edit_time=1478282906000",
                                                    "last_edit_user_id=1311281",
                                                    "last_edit_version=3" }),
                                    @Relation(id = "124", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "type=route", "last_edit_user_name=any",
                                                    "last_edit_changeset=43406696",
                                                    "last_edit_time=1478282906000",
                                                    "last_edit_user_id=1311281",
                                                    "last_edit_version=4" }) })
    private Atlas differentOSMTagsAndMembersRelations;

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
                                                    "type=any" }),
                                    @Relation(id = "124", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "outer") }, tags = {
                                                    "type=any" }) })
    private Atlas differentRolesOnMembersRelations;

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
                                                    "type=any" }),
                                    @Relation(id = "124", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "type=any" }) })
    private Atlas duplicateRelations;

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
                                                    "type=any", "last_edit_user_name=any",
                                                    "last_edit_changeset=43406696",
                                                    "last_edit_time=1478282906000",
                                                    "last_edit_user_id=1311281",
                                                    "last_edit_version=3" }),
                                    @Relation(id = "124", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "type=any", "last_edit_user_name=any",
                                                    "last_edit_changeset=43406697",
                                                    "last_edit_time=1478282906001",
                                                    "last_edit_user_id=1311282",
                                                    "last_edit_version=4" }) })
    private Atlas sameOSMTagsAndMembersRelations;

    public Atlas getDifferentMembersRelations()
    {
        return this.differentMembersRelations;
    }

    public Atlas getDifferentOSMTagsAndMembersRelations()
    {
        return this.differentOSMTagsAndMembersRelations;
    }

    public Atlas getDifferentRolesOnMembersRelations()
    {
        return this.differentRolesOnMembersRelations;
    }

    public Atlas getDuplicateRelations()
    {
        return this.duplicateRelations;
    }

    public Atlas getSameOSMTagsAndMembersRelations()
    {
        return this.sameOSMTagsAndMembersRelations;
    }
}
