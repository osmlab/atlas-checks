package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test Rule for {@link CommonMethodsTest}
 *
 * @author Vladimir Lemberg
 */

public class CommonMethodsTestRule extends CoreTestRule
{

    private static final String ONE = "18.4360044, -71.7194204";
    private static final String TWO = "18.4360737, -71.6970306";
    private static final String THREE = "18.4273807, -71.7052283";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Node(id = "2000000", coordinates = @Loc(value = TWO)),
                    @Node(id = "3000000", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "23000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "31000001", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12000001", type = "edge", role = ""),
                    @Member(id = "2000000", type = "node", role = ""),
                    @Member(id = "23000001", type = "edge", role = "") }) })
    private Atlas validRelation;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)) }, points = {
                    @Point(id = "1000000", coordinates = @Loc(value = ONE)) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "1000000", type = "node", role = ""),
                    @Member(id = "1000000", type = "point", role = "") }) })
    private Atlas oneMemberRelationArtificialMember;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Node(id = "2000000", coordinates = @Loc(value = TWO)) },
            // edges
            edges = { @Edge(id = "12000001", coordinates = { @Loc(value = ONE),
                    @Loc(value = TWO) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12000001", type = "edge", role = ""), }) })
    private Atlas oneMemberRelationEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "1000000", type = "node", role = ""), }) })
    private Atlas oneMemberRelationNode;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Node(id = "2000000", coordinates = @Loc(value = TWO)), },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "-12000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12000001", type = "edge", role = ""),
                    @Member(id = "-12000001", type = "edge", role = "") }) })
    private Atlas oneMemberRelationReversedEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
                    @Node(id = "2000000", coordinates = @Loc(value = TWO)),
                    @Node(id = "3000000", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "12000002", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "12000003", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12000001", type = "edge", role = ""),
                    @Member(id = "12000002", type = "edge", role = ""),
                    @Member(id = "12000003", type = "edge", role = "") }) })
    private Atlas oneMemberRelationSectionedEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "12000002", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "12000003", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) })
    private Atlas closedWay;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "12000002", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }), })
    private Atlas unClosedWay;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = "12000002", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(id = "12000003", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) })
    private Atlas originalWayGeometry;

    public Atlas getClosedWay()
    {
        return this.closedWay;
    }

    public Atlas getOneMemberRelationArtificialMember()
    {
        return this.oneMemberRelationArtificialMember;
    }

    public Atlas getOneMemberRelationEdge()
    {
        return this.oneMemberRelationEdge;
    }

    public Atlas getOneMemberRelationNode()
    {
        return this.oneMemberRelationNode;
    }

    public Atlas getOneMemberRelationReversedEdge()
    {
        return this.oneMemberRelationReversedEdge;
    }

    public Atlas getOneMemberRelationSectionedEdge()
    {
        return this.oneMemberRelationSectionedEdge;
    }

    public Atlas getOriginalWayGeometry()
    {
        return this.originalWayGeometry;
    }

    public Atlas getUnClosedWay()
    {
        return this.unClosedWay;
    }

    public Atlas getValidRelation()
    {
        return this.validRelation;
    }
}
