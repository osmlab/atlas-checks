package org.openstreetmap.atlas.checks.validation.relations;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Cases for unit tests for RouteRelationCheck.
 *
 * @author Lluc
 */
public class RouteRelationCheckTestRule extends CoreTestRule
{
    private static final String START = "47.618897, -122.343806";
    private static final String TWO = "47.628897, -122.353806";
    private static final String THREE = "47.638897, -122.363806";
    private static final String FOUR = "47.648897, -122.373806";
    private static final String FIVE = "47.658897, -122.383806";
    private static final String SIX = "47.668897, -122.393806";
    private static final String SEVEN = "47.678897, -122.403806";
    private static final String EIGHT = "47.688897, -122.413806";
    private static final String STOP1 = "47.618899, -122.343809";
    private static final String PLATFORM1 = "47.648899, -122.373809";
    private static final String PLATFORM2 = "47.678899, -122.403809";

    private static final String START_ID = "10000000";
    private static final String TWO_ID = "20000000";
    private static final String THREE_ID = "30000000";
    private static final String FOUR_ID = "40000000";
    private static final String FIVE_ID = "50000000";
    private static final String SIX_ID = "60000000";
    private static final String SEVEN_ID = "70000000";
    private static final String EIGHT_ID = "80000000";

    private static final String Edge1ID = "10000000";
    private static final String Edge2ID = "20000000";
    private static final String Edge3ID = "30000000";
    private static final String Edge4ID = "40000000";
    private static final String Line5ID = "50000000";
    private static final String Line6ID = "60000000";
    private static final String Edge5ID = "70000000";

    private static final String STOP1ID = "80000000";
    private static final String PLATFORM1ID = "90000000";
    private static final String PLATFORM2ID = "100000000";
    private static final String RELATION1ID = "110000000";
    private static final String RELATION2ID = "120000000";
    private static final String RELATION_ROUTE_MASTER1ID = "130000000";
    private static final String RELATION_ROUTE_MASTER2ID = "140000000";

    // Valid
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge5ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line6ID, role = "", type = "LINE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = PLATFORM1ID, role = "platform", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=railway",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION2ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas validRouteOne;

    // public not in route master
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = PLATFORM1ID, role = "platform", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION2ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteOne;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),

                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteTwo;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),

                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteThree;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),

                                                                    @Relation(members = {
                                                                            @Member(id = Edge5ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = Line6ID, role = "", type = "LINE"),
                                                                            @Member(id = PLATFORM1ID, role = "platform", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteFour;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge5ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line6ID, role = "", type = "LINE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = PLATFORM1ID, role = "platform", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=railway",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION"),
                                                                            @Member(id = RELATION2ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteMasterOne;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge5ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line6ID, role = "", type = "LINE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=111",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = PLATFORM1ID, role = "platform", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=railway",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=111",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION"),
                                                                            @Member(id = RELATION2ID, role = "", type = "RELATION") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=bus",
                                                                                    "network=lcn",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteMasterTwo;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = Line6ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge2ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bicycle",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION"),
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION_ROUTE_MASTER1ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=train",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteMasterThree;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = START), id = START_ID),
            @Node(coordinates = @Loc(value = TWO), id = TWO_ID),
            @Node(coordinates = @Loc(value = THREE), id = THREE_ID),
            @Node(coordinates = @Loc(value = FOUR), id = FOUR_ID),
            @Node(coordinates = @Loc(value = FIVE), id = FIVE_ID),
            @Node(coordinates = @Loc(value = SIX), id = SIX_ID),
            @Node(coordinates = @Loc(value = SEVEN), id = SEVEN_ID),
            @Node(coordinates = @Loc(value = EIGHT), id = EIGHT_ID) }, points = {
                    @Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID),
                    @Point(coordinates = @Loc(value = PLATFORM2), id = PLATFORM2ID), }, edges = {
                            @Edge(coordinates = { @Loc(value = START),
                                    @Loc(value = TWO) }, id = Edge1ID),
                            @Edge(coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, id = Edge2ID),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, id = Edge3ID),
                            @Edge(coordinates = { @Loc(value = FOUR),
                                    @Loc(value = FIVE) }, id = Edge4ID),
                            @Edge(coordinates = { @Loc(value = SEVEN),
                                    @Loc(value = EIGHT) }, id = Edge5ID) }, lines = {
                                            @Line(id = Line5ID, coordinates = { @Loc(value = FIVE),
                                                    @Loc(value = SIX) }, tags = {
                                                            "natural=coastline" }),
                                            @Line(id = Line6ID, coordinates = { @Loc(value = SIX),
                                                    @Loc(value = SEVEN) }, tags = {
                                                            "natural=coastline" }) }, relations = {
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge5ID, role = "", type = "EDGE"),
                                                                            @Member(id = Line5ID, role = "", type = "LINE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION1ID, tags = {
                                                                                    "type=route",
                                                                                    "route=bus",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = Edge3ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = Edge1ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION2ID, tags = {
                                                                                    "type=route",
                                                                                    "route=train",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }),
                                                                    @Relation(members = {
                                                                            @Member(id = RELATION1ID, role = "", type = "RELATION"),
                                                                            @Member(id = RELATION2ID, role = "", type = "RELATION"),
                                                                            @Member(id = Edge4ID, role = "", type = "EDGE"),
                                                                            @Member(id = STOP1ID, role = "stop", type = "POINT") }, id = RELATION_ROUTE_MASTER2ID, tags = {
                                                                                    "type=route_master",
                                                                                    "route_master=train",
                                                                                    "network=Metlink",
                                                                                    "operator=Valley Flyer",
                                                                                    "ref=110",
                                                                                    "colour=#A10082" }) })
    private Atlas invalidRouteMasterFour;

    public Atlas getInvalidRouteFour()
    {
        return this.invalidRouteFour;
    }

    public Atlas getInvalidRouteMasterFour()
    {
        return this.invalidRouteMasterFour;
    }

    public Atlas getInvalidRouteMasterOne()
    {
        return this.invalidRouteMasterOne;
    }

    public Atlas getInvalidRouteMasterThree()
    {
        return this.invalidRouteMasterThree;
    }

    public Atlas getInvalidRouteMasterTwo()
    {
        return this.invalidRouteMasterTwo;
    }

    public Atlas getInvalidRouteOne()
    {
        return this.invalidRouteOne;
    }

    public Atlas getInvalidRouteThree()
    {
        return this.invalidRouteThree;
    }

    public Atlas getInvalidRouteTwo()
    {
        return this.invalidRouteTwo;
    }

    public Atlas getValidRouteOne()
    {
        return this.validRouteOne;
    }

}
