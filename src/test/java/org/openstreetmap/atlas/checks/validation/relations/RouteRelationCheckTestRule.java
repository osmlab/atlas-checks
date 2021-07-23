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
 * Cases for unit tests for RoutePrintCheck.  
 *
 * @author Lluc
 */
public class RouteRelationCheckTestRule extends CoreTestRule {

    private static final String Edge1StartLOCATION = "-0.5424920790473705, 2.049958909453347";
    private static final String Edge1EndLOCATION = "-0.6424891975087754, 3.049966449275715";
    private static final String Edge1StartLOCATION_ID = "1";
    private static final String Edge1EndLOCATION_ID = "2";
    private static final String Edge1ID = "1001";

    //private static final String SIGN1 = "174.7497732, -36.8120845";
    private static final String STOP1 = "-0.5424920790473705, 2.049958909453347";
    private static final String  STOP1ID= "1001";
    //private static final String PLATFORM1 = "174.7502052 -36.8119194";
    private static final String PLATFORM1 = "-0.6424891975087754, 3.049966449275715";
    private static final String PLATFORM1ID = "1002";
    private static final String RELATION1ID = "100001";
    private static final String RELATION2ID = "100002";
    private static final String RELATION3ID = "100003";
    private static final String RELATION4ID = "100004";
    private static final String RELATION5ID = "100005";
    private static final String RELATION6ID = "100006";
    private static final String RELATION7ID = "100007";
    private static final String RELATION8ID = "100008";
    private static final String RELATION9ID = "100009";
    private static final String RELATION10ID = "1000010" ;
    private static final String RELATIONROUTEMASTER1ID = "10000001";
    private static final String RELATIONROUTEMASTER2ID = "10000002";


    //private static final String Edge2StartLOCATION = "174.7502052, -36.8119194";
    //private static final String Edge2EndLOCATION = "174.7504158, -36.8117337";
    private static final String Edge2StartLOCATION = "-0.6424891975087754, 3.049966449275715";
    private static final String Edge2EndLOCATION = "0.6524859564323545, 3.0609701249391205";
    private static final String Edge2StartLOCATION_ID = "2";
    private static final String Edge2EndLOCATION_ID = "3";
    private static final String Edge2ID = "1002";


    //private static final String Edge3StartLOCATION = "174.7504158, -36.8117337";
    //private static final String Edge3EndLOCATION = "174.7577002, -36.8072803";
    private static final String Edge3StartLOCATION = "0.6524859564323545, 3.0609701249391205";
    private static final String Edge3EndLOCATION = "-0.6824859564323545, 3.0899701249391205";
    private static final String Edge3StartLOCATION_ID = "3";
    private static final String Edge3EndLOCATION_ID = "4";
    private static final String Edge3ID = "1003";


    //private static final String Edge4StartLOCATION = "174.7577002, -36.8072803";
    //private static final String Edge4EndLOCATION = "174.7576198, -36.8081736";
    private static final String Edge4StartLOCATION = "-0.6824859564323545, 3.0899701249391205";
    private static final String Edge4EndLOCATION = "-0.7424238209656542, 3.1500958584584343";
    private static final String Edge4StartLOCATION_ID = "4";
    private static final String Edge4EndLOCATION_ID = "5";
    private static final String Edge4ID = "1004";

    //97 meters
    //private static final String Line5StartLOCATION = "174.7576198, -36.8081736";
    //private static final String Line5StartLOCATIONv2 = "174.7567321, -36.8068639";
    //private static final String Line5EndLOCATION = "174.7577797, -36.8073108";
    private static final String Line5StartLOCATION = "-0.7424238209656542, 3.1500958584584343";
    private static final String Line5StartLOCATIONv2 = "-0.6424009623884409, 3.0500803651706643";
    private static final String Line5EndLOCATION = "-2.6424238209656542, 5.0500958584584343";
    private static final String Line5StartLOCATION_ID = "5";
    private static final String Line5StartLOCATION_IDv2 = "6";
    private static final String Line5EndLOCATION_ID = "7";
    private static final String Line5ID = "1005";


    //private static final String Line6StartLOCATION = "174.7577797, -36.8073108";
    //private static final String Line6StartLOCATIONv2 = "174.7877797, -35.8073108";
    //private static final String Line6EndLOCATION = "174.7576198, -36.8081736";
    private static final String Line6StartLOCATION = "-2.6424238209656542, 5.0500958584584343";
    private static final String Line6StartLOCATIONv2 = "174.7877797, -35.8073108";
    private static final String Line6EndLOCATION = "-3.8424238209656542, 2.1500958584584343";
    private static final String Line6StartLOCATION_ID = "7";
    private static final String Line6StartLOCATION_IDv2 = "8";
    private static final String Line6EndLOCATION_ID = "9";
    private static final String Line6ID = "1006";

    // Valid
    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID)}, points = {
            @Point(coordinates = @Loc(value = STOP1), id = STOP1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, relations = @Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge2ID, role = "", type = "EDGE"),
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION1ID, tags = {
            "type=route",
            "route=bicycle",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}))
    private Atlas validRouteOne;

    // public not in route master
    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID)}, points = {
            @Point(coordinates = @Loc(value = STOP1), id = STOP1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, relations = @Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge2ID, role = "", type = "EDGE"),
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION2ID, tags = {
            "type=route",
            "route=bus",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}))
    private Atlas invalidRouteOne;

    /* disconnected and not in route master */
    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge3StartLOCATION), id = Edge3StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line5EndLOCATION), id = Line5EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line6EndLOCATION), id = Line6EndLOCATION_ID)}, points = {
            @Point(coordinates = @Loc(value = STOP1), id = STOP1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID) }, lines = {
            @Line(id = Line6ID, coordinates = {
                    @Loc(value = Line6StartLOCATION),
                    @Loc(value = Line6EndLOCATION) }, tags = {
                    "natural=coastline" })}, relations = @Relation(members = {
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = Line6ID, role = "", type = "lINE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION3ID, tags = {
            "type=route",
            "route=bus",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}))
    private Atlas invalidRouteTwo;


    // disconnected
    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line5EndLOCATION), id = Line5EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line6EndLOCATION), id = Line6EndLOCATION_ID)}, points = {
            @Point(coordinates = @Loc(value = STOP1), id = STOP1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, lines = {
            @Line(id = Line5ID, coordinates = {
                    @Loc(value = Line5StartLOCATION),
                    @Loc(value = Line5EndLOCATION) }, tags = {
                    "natural=coastline" }),
            @Line(id = Line6ID, coordinates = {
                    @Loc(value = Line6StartLOCATION),
                    @Loc(value = Line6EndLOCATION) }, tags = {
                    "natural=coastline" })}, relations = @Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Line6ID, role = "", type = "lINE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION4ID, tags = {
            "type=route",
            "route=foot",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}))
    private Atlas invalidRouteThree;

    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line5EndLOCATION), id = Line5EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line6EndLOCATION), id = Line6EndLOCATION_ID)},
            points = {@Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, lines = {
            @Line(id = Line5ID, coordinates = {
                    @Loc(value = Line5StartLOCATION),
                    @Loc(value = Line5EndLOCATION) }, tags = {
                    "natural=coastline" }),
            @Line(id = Line6ID, coordinates = {
                    @Loc(value = Line6StartLOCATION),
                    @Loc(value = Line6EndLOCATION) }, tags = {
                    "natural=coastline" })}, relations = {@Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge2ID, role = "", type = "EDGE"),
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION5ID, tags = {
            "type=route",
            "route=train",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}),
            @Relation(members = {
                    @Member(id = Edge2ID, role = "", type = "EDGE"),
                    @Member(id = Edge3ID, role = "", type = "EDGE"),
                    @Member(id = Edge4ID, role = "", type = "EDGE"),
                    @Member(id = PLATFORM1ID, role = "platform", type = "POINT")}, id = RELATION6ID, tags = {
                    "type=route",
                    "route=railway",
                    "network=lcn",
                    "operator=Valley Flyer",
                    "ref=110",
                    "colour=#A10082"}), @Relation(members = {
            @Member(id = RELATION5ID, role = "", type = "RELATION"),
            @Member(id = RELATION6ID, role = "", type = "RELATION")},
            id = RELATIONROUTEMASTER1ID, tags = {
            "type=route_master",
            "route_master=bus",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"})}
    )
    private Atlas validRouteMaster;

    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line5EndLOCATION), id = Line5EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line6EndLOCATION), id = Line6EndLOCATION_ID)},
            points = {@Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, lines = {
            @Line(id = Line5ID, coordinates = {
                    @Loc(value = Line5StartLOCATION),
                    @Loc(value = Line5EndLOCATION) }, tags = {
                    "natural=coastline" }),
            @Line(id = Line6ID, coordinates = {
                    @Loc(value = Line6StartLOCATION),
                    @Loc(value = Line6EndLOCATION) }, tags = {
                    "natural=coastline" })}, relations = {@Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge2ID, role = "", type = "EDGE"),
            @Member(id = Edge3ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = PLATFORM1ID, role = "platform", type = "POINT")}, id = RELATION7ID, tags = {
            "type=route",
            "route=train",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=110",
            "colour=#A10082"}),
            @Relation(members = {
                    @Member(id = Edge2ID, role = "", type = "EDGE"),
                    @Member(id = Edge3ID, role = "", type = "EDGE"),
                    @Member(id = Edge4ID, role = "", type = "EDGE"),
                    @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION8ID, tags = {
                    "type=route",
                    "route=railway",
                    "network=lcn",
                    "operator=Valley Flyer",
                    "ref=110",
                    "colour=#A10082"}),
            @Relation(members = {
                    @Member(id = RELATION7ID, role = "", type = "RELATION"),
                    @Member(id = RELATION8ID, role = "", type = "RELATION"),
                    @Member(id = Edge4ID, role = "", type = "EDGE"),
                    @Member(id = STOP1ID, role = "stop", type = "POINT"),
                    @Member(id = PLATFORM1ID, role = "platform", type = "POINT")},
                    id = RELATIONROUTEMASTER1ID, tags = {
                    "type=route_master",
                    "route_master=bus",
                    "network=Metlink",
                    "operator=Valley Flyer",
                    "ref=110",
                    "colour=#A10082"})}
    )
    private Atlas invalidRouteMasterOne;


    @TestAtlas(nodes = {@Node(coordinates = @Loc(value = Edge1StartLOCATION), id = Edge1StartLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge1EndLOCATION), id = Edge1EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge2EndLOCATION), id = Edge2EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge3EndLOCATION), id = Edge3EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Edge4EndLOCATION), id = Edge4EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line5EndLOCATION), id = Line5EndLOCATION_ID),
            @Node(coordinates = @Loc(value = Line6EndLOCATION), id = Line6EndLOCATION_ID)},
            points = {@Point(coordinates = @Loc(value = STOP1), id = STOP1ID),
                    @Point(coordinates = @Loc(value = PLATFORM1), id = PLATFORM1ID)}, edges = {
            @Edge(coordinates = {@Loc(value = Edge1StartLOCATION),
                    @Loc(value = Edge1EndLOCATION)}, id = Edge1ID),
            @Edge(coordinates = {@Loc(value = Edge2StartLOCATION),
                    @Loc(value = Edge2EndLOCATION)}, id = Edge2ID),
            @Edge(coordinates = {@Loc(value = Edge3StartLOCATION),
                    @Loc(value = Edge3EndLOCATION)}, id = Edge3ID),
            @Edge(coordinates = {@Loc(value = Edge4StartLOCATION),
                    @Loc(value = Edge4EndLOCATION)}, id = Edge4ID)}, lines = {
            @Line(id = Line5ID, coordinates = {
                    @Loc(value = Line5StartLOCATION),
                    @Loc(value = Line5EndLOCATION) }, tags = {
                    "natural=coastline" }),
            @Line(id = Line6ID, coordinates = {
                    @Loc(value = Line6StartLOCATION),
                    @Loc(value = Line6EndLOCATION) }, tags = {
                    "natural=coastline" })}, relations = {@Relation(members = {
            @Member(id = Edge1ID, role = "", type = "EDGE"),
            @Member(id = Edge2ID, role = "", type = "EDGE"),
            @Member(id = Edge4ID, role = "", type = "EDGE"),
            @Member(id = Line6ID, role = "", type = "LINE"),
            @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION9ID, tags = {
            "type=route",
            "route=bus",
            "network=lcn",
            "operator=Valley Flyer",
            "ref=113",
            "colour=#A10087"}),
            @Relation(members = {
                    @Member(id = Edge1ID, role = "", type = "EDGE"),
                    @Member(id = Edge3ID, role = "", type = "EDGE"),
                    @Member(id = Line5ID, role = "", type = "LINE"),
                    @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATION10ID, tags = {
                    "type=route",
                    "route=train",
                    "network=Metlink",
                    "operator=Valley Flyer",
                    "ref=114",
                    "colour=#A10081"}),
            @Relation(members = {
                    @Member(id = RELATION9ID, role = "", type = "RELATION"),
                    @Member(id = Edge4ID, role = "", type = "EDGE"),
                    @Member(id = STOP1ID, role = "stop", type = "POINT")}, id = RELATIONROUTEMASTER2ID, tags = {
                    "type=route_master",
                    "route_master=train",
                    "network=Metlink",
                    "operator=Valley Flyer",
                    "ref=110",
                    "colour=#A10082"})}
    )
    private Atlas invalidRouteMasterTwo;

    public Atlas getValidRouteOne() {
        return this.validRouteOne;
    }

    public Atlas getInvalidRouteOne() {
        return this.invalidRouteOne;
    }


    public Atlas getInvalidRouteTwo() {
        return this.invalidRouteTwo;
    }


    public Atlas getInvalidRouteThree() {
        return this.invalidRouteThree;
    }

    public Atlas getValidRouteMaster() {
        return this.validRouteMaster;
    }

    public Atlas getInvalidRouteMasterOne() {
        return this.invalidRouteMasterOne;
    }

    public Atlas getInvalidRouteMasterTwo() {
        return this.invalidRouteMasterTwo;
    }

}