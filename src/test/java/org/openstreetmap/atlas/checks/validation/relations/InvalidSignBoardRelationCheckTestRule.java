package org.openstreetmap.atlas.checks.validation.relations;

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
 * Cases for unit tests for InvlaidSignBoardRelationCheckTestRule.
 *
 * @author micah-nacht
 */
public class InvalidSignBoardRelationCheckTestRule extends CoreTestRule
{
    private static final String START = "47.620897, -122.343806";
    private static final String MIDDLE = "47.619740, -122.343903";
    private static final String SIGN = "47.619740, -122.343903";
    private static final String END = "47.619754, -122.345008";
    private static final String OTHER = "47.618575, -122.343581";

    private static final String ONE = "1000000";
    private static final String ONE_A = "1000001";
    private static final String TWO = "2000000";
    private static final String THREE = "3000000";
    private static final String FOUR = "4000000";
    private static final String MINUS_ONE = "-1000000";
    private static final String MINUS_TWO = "-2000000";

    // Valid
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas valid;

    // No destination
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign" }))
    private Atlas noDestination;

    // No from
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas noFrom;

    // No to
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas noTo;

    // No sign
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas noSign;

    // From not an edge
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE),
                    @Point(coordinates = @Loc(value = START), id = TWO) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = TWO, role = "from", type = "POINT"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas fromNotEdge;

    // To not an edge
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE),
                    @Point(coordinates = @Loc(value = START), id = TWO) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "POINT"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas toNotEdge;

    // Sign not a point
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = END) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = THREE, role = "sign", type = "EDGE") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas signNotPoint;

    // From and to don't meet
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE),
            @Node(coordinates = @Loc(value = OTHER), id = FOUR) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = OTHER) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas toFromNoMeeting;

    // From's reversed meets to's main
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = START) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = MINUS_ONE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas reverseFromMeetsTo;

    // From's main meets to's reversed
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = MIDDLE) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = MINUS_TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas fromMeetsReverseTo;

    // From's reversed meets to's reversed
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = START) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = MIDDLE) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = MINUS_ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = MINUS_TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas reverseFromMeetsReverseTo;

    // No from or to members
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas noFromOrTo;

    // Two from edges
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = END) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = THREE, role = "from", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas twoFrom;

    // Two to edges
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = END) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = THREE, role = "to", type = "EDGE"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas twoTo;

    // Two sign
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE),
                    @Point(coordinates = @Loc(value = OTHER), id = TWO) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = END) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE"),
                                            @Member(id = TWO, role = "sign", type = "POINT"),
                                            @Member(id = ONE, role = "sign", type = "POINT") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas twoSign;

    // Doesn't flag way-sectioned from
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE),
            @Node(coordinates = @Loc(value = OTHER), id = FOUR) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = ONE_A),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = OTHER) }, id = TWO) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "to", type = "EDGE") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas waySectionedFrom;

    // Doesn't flag from in multiple connected parts
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE),
            @Node(coordinates = @Loc(value = OTHER), id = FOUR) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = OTHER) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "from", type = "EDGE"),
                                            @Member(id = THREE, role = "to", type = "EDGE") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas multipleFroms;

    // Flags from that is totally disconnected
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = ONE),
            @Node(coordinates = @Loc(value = MIDDLE), id = TWO),
            @Node(coordinates = @Loc(value = END), id = THREE),
            @Node(coordinates = @Loc(value = OTHER), id = FOUR) }, points = {
                    @Point(coordinates = @Loc(value = SIGN), id = ONE) }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = MIDDLE) }, id = ONE),
                            @Edge(coordinates = { @Loc(value = END),
                                    @Loc(value = OTHER) }, id = TWO),
                            @Edge(coordinates = { @Loc(value = MIDDLE),
                                    @Loc(value = END) }, id = THREE) }, relations = @Relation(members = {
                                            @Member(id = ONE, role = "from", type = "EDGE"),
                                            @Member(id = TWO, role = "from", type = "EDGE"),
                                            @Member(id = THREE, role = "to", type = "EDGE") }, tags = {
                                                    "type=destination_sign",
                                                    "destination=Space Needle" }))
    private Atlas disconnectedFrom;

    public Atlas getDisconnectedFrom()
    {
        return this.disconnectedFrom;
    }

    public Atlas getFromMeetsReverseTo()
    {
        return this.fromMeetsReverseTo;
    }

    public Atlas getFromNotEdge()
    {
        return this.fromNotEdge;
    }

    public Atlas getMultipleFroms()
    {
        return this.multipleFroms;
    }

    public Atlas getNoDestination()
    {
        return this.noDestination;
    }

    public Atlas getNoFrom()
    {
        return this.noFrom;
    }

    public Atlas getNoFromOrTo()
    {
        return this.noFromOrTo;
    }

    public Atlas getNoTo()
    {
        return this.noTo;
    }

    public Atlas getReverseFromMeetsReverseTo()
    {
        return this.reverseFromMeetsReverseTo;
    }

    public Atlas getReverseFromMeetsTo()
    {
        return this.reverseFromMeetsTo;
    }

    public Atlas getToFromNoMeeting()
    {
        return this.toFromNoMeeting;
    }

    public Atlas getToNotEdge()
    {
        return this.toNotEdge;
    }

    public Atlas getTwoTo()
    {
        return this.twoTo;
    }

    public Atlas getValidSignBoard()
    {
        return this.valid;
    }

    public Atlas getWaySectionedFrom()
    {
        return this.waySectionedFrom;
    }

}
