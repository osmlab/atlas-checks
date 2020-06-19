package org.openstreetmap.atlas.checks.validation.relations;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test rule for {@link InvalidMultiPolygonRelationCheckTest}
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class InvalidMultiPolygonRelationCheckTestRule extends CoreTestRule
{
    // Laguna en Medio
    protected static final String ONE = "18.4360044, -71.7194204";
    protected static final String RELATION_ID_OPEN_MULTIPOLYGON = "123";
    protected static final String THREE = "18.4273807, -71.7052283";
    protected static final String TWO = "18.4360737, -71.6970306";
    private static final String TEST_INVALIDMEMBERTYPE_0 = "1.43716502212, 103.92972681624";
    private static final String TEST_INVALIDMEMBERTYPE_1 = "1.42082076868, 103.90655028191";
    private static final String TEST_INVALIDMEMBERTYPE_2 = "1.403578354, 103.91292832043";
    private static final String TEST_INVALIDMEMBERTYPE_3 = "1.40160265251, 103.95443048656";
    private static final String TEST_INVALIDMEMBERTYPE_4 = "1.42100037649, 103.95838307381";
    private static final String TEST_INVALIDMEMBERTYPE_5 = "1.43572816935, 103.95038806778";
    private static final String TEST_INVALIDMEMBERTYPE_6 = "1.41937279399, 103.93426970921";
    private static final String TEST_NOOUTERRELATION_0 = "1.43958970913, 103.91306306772";
    private static final String TEST_NOOUTERRELATION_1 = "1.42773565932, 103.90488839864";
    private static final String TEST_NOOUTERRELATION_2 = "1.41193016458, 103.91459020371";
    private static final String TEST_NOOUTERRELATION_3 = "1.41624076477, 103.94180915682";
    private static final String TEST_NOOUTERRELATION_4 = "1.42638860433, 103.94621090171";
    private static final String TEST_NOOUTERRELATION_5 = "1.43141760561, 103.96399754433";
    private static final String TEST_NOOUTERRELATION_6 = "1.44273281808, 103.95447540232";
    private static final String TEST_NOOUTERRELATION_7 = "1.43842226756, 103.93857522179";
    private static final String TEST_NOOUTERRELATION_8 = "1.44839040324, 103.9256394817";
    private static final String TEST_ONEELEMENTRELATION_0 = "1.41193016458, 103.91459020371";
    private static final String TEST_ONEELEMENTRELATION_1 = "1.41624076477, 103.94180915682";
    private static final String TEST_ONEELEMENTRELATION_2 = "1.42638860433, 103.94621090171";
    private static final String TEST_ONEELEMENTRELATION_3 = "1.43141760561, 103.96399754433";
    private static final String TEST_ONEELEMENTRELATION_4 = "1.44273281808, 103.95447540232";
    private static final String TEST_ONEELEMENTRELATION_5 = "1.43842226756, 103.93857522179";
    private static final String TEST_ONEELEMENTRELATION_6 = "1.44839040324, 103.9256394817";
    private static final String TEST_OPENRELATION_0 = "1.41193016458, 103.91459020371";
    private static final String TEST_OPENRELATION_1 = "1.41624076477, 103.94180915682";
    private static final String TEST_OPENRELATION_2 = "1.42638860433, 103.94621090171";
    private static final String TEST_OPENRELATION_3 = "1.43141760561, 103.96399754433";
    private static final String TEST_OPENRELATION_4 = "1.44273281808, 103.95447540232";
    private static final String TEST_OPENRELATION_5 = "1.43842226756, 103.93857522179";
    private static final String TEST_OPENRELATION_6 = "1.44839040324, 103.9256394817";
    private static final String TEST_TOOMANYMISSINGROLES_0 = "1.44713316332, 103.9158478451";
    private static final String TEST_TOOMANYMISSINGROLES_1 = "1.44246340891, 103.94648039629";
    private static final String TEST_TOOMANYMISSINGROLES_10 = "1.42324547295, 103.95276860328";
    private static final String TEST_TOOMANYMISSINGROLES_2 = "1.4326748542, 103.90138496903";
    private static final String TEST_TOOMANYMISSINGROLES_3 = "1.42450272602, 103.89150350091";
    private static final String TEST_TOOMANYMISSINGROLES_4 = "1.40214148035, 103.92653779699";
    private static final String TEST_TOOMANYMISSINGROLES_5 = "1.41875527784, 103.89608490885";
    private static final String TEST_TOOMANYMISSINGROLES_6 = "1.4219882192, 103.94728888005";
    private static final String TEST_TOOMANYMISSINGROLES_7 = "1.40438659504, 103.93641926511";
    private static final String TEST_TOOMANYMISSINGROLES_8 = "1.44138577192, 103.95285843481";
    private static final String TEST_TOOMANYMISSINGROLES_9 = "1.4247721373, 103.9670518163";
    private static final String TEST_VALIDRELATIONTWONOROLE_0 = "1.43288811648, 103.92151376692";
    private static final String TEST_VALIDRELATIONTWONOROLE_1 = "1.4332473302, 103.94221993422";
    private static final String TEST_VALIDRELATIONTWONOROLE_15 = "1.4233693219, 103.92131539708";
    private static final String TEST_VALIDRELATIONTWONOROLE_16 = "1.41349049207, 103.92110952504";
    private static final String TEST_VALIDRELATIONTWONOROLE_2 = "1.41353539417, 103.94217501846";
    private static final String TEST_VALIDRELATIONTWONOROLE_4 = "1.43701907091, 103.92137901963";
    private static final String TEST_VALIDRELATIONTWONOROLE_5 = "1.40810223421, 103.92110952504";
    private static final String TEST_VALIDRELATIONTWONOROLE_6 = "1.42354853991, 103.90664664897";
    private static final String TEST_VALIDRELATIONTWONOROLE_8 = "1.4233689323, 103.93355119173";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_0 = "1.43288811648, 103.92151376692";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_1 = "1.4332473302, 103.94221993422";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_2 = "1.41353539417, 103.94217501846";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_3 = "1.41349049207, 103.92110952504";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_4 = "1.43701907091, 103.92137901963";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_5 = "1.40810223421, 103.92110952504";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_6 = "1.42354853991, 103.90664664897";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_7 = "1.4233693219, 103.92131539708";
    private static final String TEST_VALIDRELATIONWITHWAYSECTIONING_8 = "1.4233689323, 103.93355119173";
    private static final String TEST_VALIDRELATION_0 = "1.43958970913, 103.91306306772";
    private static final String TEST_VALIDRELATION_1 = "1.42773565932, 103.90488839864";
    private static final String TEST_VALIDRELATION_10 = "1.43419031086, 103.89654055711";
    private static final String TEST_VALIDRELATION_11 = "1.41407426416, 103.89600156794";
    private static final String TEST_VALIDRELATION_12 = "1.40850639889, 103.91621366183";
    private static final String TEST_VALIDRELATION_13 = "1.39835848123, 103.92322052105";
    private static final String TEST_VALIDRELATION_14 = "1.41146994174, 103.94549874009";
    private static final String TEST_VALIDRELATION_15 = "1.41155974601, 103.96373454036";
    private static final String TEST_VALIDRELATION_16 = "1.43419031086, 103.97038207346";
    private static final String TEST_VALIDRELATION_17 = "1.45116308784, 103.96454302412";
    private static final String TEST_VALIDRELATION_18 = "1.45233052286, 103.93786306018";
    private static final String TEST_VALIDRELATION_19 = "1.45367756252, 103.91845945004";
    private static final String TEST_VALIDRELATION_2 = "1.41193016458, 103.91459020371";
    private static final String TEST_VALIDRELATION_3 = "1.41624076477, 103.94180915682";
    private static final String TEST_VALIDRELATION_4 = "1.42638860433, 103.94621090171";
    private static final String TEST_VALIDRELATION_5 = "1.43141760561, 103.96399754433";
    private static final String TEST_VALIDRELATION_6 = "1.44273281808, 103.95447540232";
    private static final String TEST_VALIDRELATION_7 = "1.43842226756, 103.93857522179";
    private static final String TEST_VALIDRELATION_8 = "1.44839040324, 103.9256394817";
    private static final String TEST_VALIDRELATION_9 = "1.4507140742, 103.9069610144";
    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)) },
            // lines
            lines = {
                    @Line(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "natural=water" }),
                    @Line(id = "23", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "natural=water" }),
                    @Line(id = "31", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "natural=water" }) },
            // relations
            relations = {
                    @Relation(id = RELATION_ID_OPEN_MULTIPOLYGON, members = {
                            @Member(id = "12", type = "line", role = "outer"),
                            @Member(id = "23", type = "line", role = "outer"), }, tags = {
                                    "natural=water", "type=multipolygon", "water=lake" }),
                    @Relation(id = "1231", members = {
                            @Member(id = "12", type = "line", role = "outer"),
                            @Member(id = "23", type = "line", role = "outer"),
                            @Member(id = "31", type = "line", role = "outer") }, tags = {
                                    "natural=water", "type=multipolygon", "water=lake" }) })
    private Atlas atlas;
    @TestAtlas(points = {
            @Point(id = "38985", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_0)),
            @Point(id = "38988", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_1)),
            @Point(id = "38992", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_2)),
            @Point(id = "38994", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_3)),
            @Point(id = "38996", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_4)),
            @Point(id = "38998", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_5)),
            @Point(id = "39007000000", coordinates = @Loc(value = TEST_INVALIDMEMBERTYPE_6), tags = {
                    "barrier=fence" }) }, areas = {
                            @Area(id = "38989000000", coordinates = {
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_0),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_1),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_2),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_3),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_4),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_5),
                                    @Loc(value = TEST_INVALIDMEMBERTYPE_0) }, tags = {
                                            "barrier=fence" }) }, relations = {
                                                    @Relation(id = "39033000000", members = {
                                                            @Member(id = "38989000000", type = "area", role = "outer"),
                                                            @Member(id = "39007000000", type = "point", role = "inner") }, tags = {
                                                                    "type=multipolygon" }) })
    private Atlas invalidMemberType;
    @TestAtlas(points = { @Point(id = "39525", coordinates = @Loc(value = TEST_NOOUTERRELATION_0)),
            @Point(id = "39527", coordinates = @Loc(value = TEST_NOOUTERRELATION_1)),
            @Point(id = "39529", coordinates = @Loc(value = TEST_NOOUTERRELATION_2)),
            @Point(id = "39531", coordinates = @Loc(value = TEST_NOOUTERRELATION_3)),
            @Point(id = "39533", coordinates = @Loc(value = TEST_NOOUTERRELATION_4)),
            @Point(id = "39535", coordinates = @Loc(value = TEST_NOOUTERRELATION_5)),
            @Point(id = "39537", coordinates = @Loc(value = TEST_NOOUTERRELATION_6)),
            @Point(id = "39539", coordinates = @Loc(value = TEST_NOOUTERRELATION_7)),
            @Point(id = "39541", coordinates = @Loc(value = TEST_NOOUTERRELATION_8)) }, lines = {
                    @Line(id = "39565", coordinates = { @Loc(value = TEST_NOOUTERRELATION_2),
                            @Loc(value = TEST_NOOUTERRELATION_3),
                            @Loc(value = TEST_NOOUTERRELATION_4),
                            @Loc(value = TEST_NOOUTERRELATION_5),
                            @Loc(value = TEST_NOOUTERRELATION_6),
                            @Loc(value = TEST_NOOUTERRELATION_7),
                            @Loc(value = TEST_NOOUTERRELATION_8) }),
                    @Line(id = "39647", coordinates = { @Loc(value = TEST_NOOUTERRELATION_8),
                            @Loc(value = TEST_NOOUTERRELATION_0),
                            @Loc(value = TEST_NOOUTERRELATION_1),
                            @Loc(value = TEST_NOOUTERRELATION_2) }) }, relations = {
                                    @Relation(id = "39569000000", members = {
                                            @Member(id = "39565", type = "line", role = "inner"),
                                            @Member(id = "39647", type = "line", role = "inner") }, tags = {
                                                    "type=multipolygon", "forest=yes",
                                                    "name=Test Forest" }) })
    private Atlas noOuterRelation;
    @TestAtlas(points = {
            @Point(id = "39529", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_0)),
            @Point(id = "39531", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_1)),
            @Point(id = "39533", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_2)),
            @Point(id = "39535", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_3)),
            @Point(id = "39537", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_4)),
            @Point(id = "39539", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_5)),
            @Point(id = "39541", coordinates = @Loc(value = TEST_ONEELEMENTRELATION_6)) }, areas = {
                    @Area(id = "39766", coordinates = { @Loc(value = TEST_ONEELEMENTRELATION_0),
                            @Loc(value = TEST_ONEELEMENTRELATION_1),
                            @Loc(value = TEST_ONEELEMENTRELATION_2),
                            @Loc(value = TEST_ONEELEMENTRELATION_3),
                            @Loc(value = TEST_ONEELEMENTRELATION_4),
                            @Loc(value = TEST_ONEELEMENTRELATION_5),
                            @Loc(value = TEST_ONEELEMENTRELATION_6),
                            @Loc(value = TEST_ONEELEMENTRELATION_0) }) }, relations = {
                                    @Relation(id = "39570000000", members = {
                                            @Member(id = "39766", type = "area", role = "outer") }, tags = {
                                                    "type=multipolygon", "forest=yes",
                                                    "name=Test Forest" }) })
    private Atlas oneMemberRelation;
    @TestAtlas(points = { @Point(id = "39529", coordinates = @Loc(value = TEST_OPENRELATION_0)),
            @Point(id = "39531", coordinates = @Loc(value = TEST_OPENRELATION_1)),
            @Point(id = "39533", coordinates = @Loc(value = TEST_OPENRELATION_2)),
            @Point(id = "39535", coordinates = @Loc(value = TEST_OPENRELATION_3)),
            @Point(id = "39537", coordinates = @Loc(value = TEST_OPENRELATION_4)),
            @Point(id = "39539", coordinates = @Loc(value = TEST_OPENRELATION_5)),
            @Point(id = "39541", coordinates = @Loc(value = TEST_OPENRELATION_6)) }, lines = {
                    @Line(id = "39565000000", coordinates = { @Loc(value = TEST_OPENRELATION_2),
                            @Loc(value = TEST_OPENRELATION_3), @Loc(value = TEST_OPENRELATION_4),
                            @Loc(value = TEST_OPENRELATION_5), @Loc(value = TEST_OPENRELATION_6) }),
                    @Line(id = "39766000000", coordinates = { @Loc(value = TEST_OPENRELATION_0),
                            @Loc(value = TEST_OPENRELATION_1),
                            @Loc(value = TEST_OPENRELATION_2) }) }, relations = {
                                    @Relation(id = "39569000000", members = {
                                            @Member(id = "39565000000", type = "line", role = "outer"),
                                            @Member(id = "39766000000", type = "line", role = "outer") }, tags = {
                                                    "type=multipolygon", "forest=yes",
                                                    "name=Test Forest" }) })
    private Atlas openRelation;
    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = TEST_VALIDRELATION_9)),
            @Point(id = "39009", coordinates = @Loc(value = TEST_VALIDRELATION_10)),
            @Point(id = "39011", coordinates = @Loc(value = TEST_VALIDRELATION_11)),
            @Point(id = "39013", coordinates = @Loc(value = TEST_VALIDRELATION_12)),
            @Point(id = "39015", coordinates = @Loc(value = TEST_VALIDRELATION_13)),
            @Point(id = "38985", coordinates = @Loc(value = TEST_VALIDRELATION_0)),
            @Point(id = "39019", coordinates = @Loc(value = TEST_VALIDRELATION_15)),
            @Point(id = "38988", coordinates = @Loc(value = TEST_VALIDRELATION_1)),
            @Point(id = "39021", coordinates = @Loc(value = TEST_VALIDRELATION_16)),
            @Point(id = "39023", coordinates = @Loc(value = TEST_VALIDRELATION_17)),
            @Point(id = "38992", coordinates = @Loc(value = TEST_VALIDRELATION_2)),
            @Point(id = "39025", coordinates = @Loc(value = TEST_VALIDRELATION_18)),
            @Point(id = "38994", coordinates = @Loc(value = TEST_VALIDRELATION_3)),
            @Point(id = "39027", coordinates = @Loc(value = TEST_VALIDRELATION_19)),
            @Point(id = "38996", coordinates = @Loc(value = TEST_VALIDRELATION_4)),
            @Point(id = "38998", coordinates = @Loc(value = TEST_VALIDRELATION_5)),
            @Point(id = "39017", coordinates = @Loc(value = TEST_VALIDRELATION_14)),
            @Point(id = "39000", coordinates = @Loc(value = TEST_VALIDRELATION_6)),
            @Point(id = "39002", coordinates = @Loc(value = TEST_VALIDRELATION_7)),
            @Point(id = "39004", coordinates = @Loc(value = TEST_VALIDRELATION_8)) }, areas = {
                    @Area(id = "39010", coordinates = { @Loc(value = TEST_VALIDRELATION_9),
                            @Loc(value = TEST_VALIDRELATION_10),
                            @Loc(value = TEST_VALIDRELATION_11),
                            @Loc(value = TEST_VALIDRELATION_12),
                            @Loc(value = TEST_VALIDRELATION_13),
                            @Loc(value = TEST_VALIDRELATION_14),
                            @Loc(value = TEST_VALIDRELATION_15),
                            @Loc(value = TEST_VALIDRELATION_16),
                            @Loc(value = TEST_VALIDRELATION_17),
                            @Loc(value = TEST_VALIDRELATION_18),
                            @Loc(value = TEST_VALIDRELATION_19),
                            @Loc(value = TEST_VALIDRELATION_9) }),
                    @Area(id = "38989", coordinates = { @Loc(value = TEST_VALIDRELATION_0),
                            @Loc(value = TEST_VALIDRELATION_1), @Loc(value = TEST_VALIDRELATION_2),
                            @Loc(value = TEST_VALIDRELATION_3), @Loc(value = TEST_VALIDRELATION_4),
                            @Loc(value = TEST_VALIDRELATION_5), @Loc(value = TEST_VALIDRELATION_6),
                            @Loc(value = TEST_VALIDRELATION_7), @Loc(value = TEST_VALIDRELATION_8),
                            @Loc(value = TEST_VALIDRELATION_0) }) }, relations = {
                                    @Relation(id = "39190", members = {
                                            @Member(id = "39010", type = "area", role = "outer"),
                                            @Member(id = "38989", type = "area", role = "inner") }, tags = {
                                                    "type=multipolygon", "forest=yes",
                                                    "name=Test Forest" }) })
    private Atlas validRelation;
    @TestAtlas(nodes = {
            @Node(id = "39043", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_16)),
            @Node(id = "39055", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_4)),
            @Node(id = "39058", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_5)),
            @Node(id = "39070", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_8)),
            @Node(id = "39066", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_6)),
            @Node(id = "39067", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
            @Node(id = "39038", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_0)) }, points = {
                    @Point(id = "39041", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_2)),
                    @Point(id = "39221", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
                    @Point(id = "39219", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
                    @Point(id = "39220", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
                    @Point(id = "39222", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_16)),
                    @Point(id = "39223", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
                    @Point(id = "39224", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_16)),
                    @Point(id = "39227", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_16)),
                    @Point(id = "39226", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_15)),
                    @Point(id = "39039", coordinates = @Loc(value = TEST_VALIDRELATIONTWONOROLE_1)) }, edges = {
                            @Edge(id = "39104000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_16),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_5) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39104000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_5),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_16) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39056000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_0),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39056000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_0) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39056000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_16) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39056000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_16),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39096000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_4),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_0) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39096000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_0),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_4) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39068000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_6),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39068000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_6) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39068000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_8) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39068000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                                    @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }, tags = {
                                            "highway=primary" }) }, lines = {
                                                    @Line(id = "39040000000", coordinates = {
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_0),
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_2),
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_16) }, tags = {
                                                                    "barrier=unclassified" }),
                                                    @Line(id = "39225", coordinates = {
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                                                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_16) }) }, relations = {
                                                                    @Relation(id = "39152000000", members = {
                                                                            @Member(id = "39040000000", type = "line", role = "outer"),
                                                                            @Member(id = "39056000003", type = "edge", role = ""),
                                                                            @Member(id = "39056000001", type = "edge", role = "") }, tags = {
                                                                                    "type=multipolygon",
                                                                                    "name=The Park" }) })
    private Atlas validRelationTwoNoRole;
    @TestAtlas(nodes = {
            @Node(id = "39043", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3)),
            @Node(id = "39055", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_4)),
            @Node(id = "39058", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5)),
            @Node(id = "39070", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_8)),
            @Node(id = "39066", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6)),
            @Node(id = "39067", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7)),
            @Node(id = "39038", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0)) }, points = {
                    @Point(id = "39041", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_2)),
                    @Point(id = "39039", coordinates = @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_1)) }, edges = {
                            @Edge(id = "39056000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39056000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39056000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39056000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39096000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_4),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39096000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_4) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39068000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39068000001", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39068000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_8) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39068000003", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_8),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "39104000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5) }, tags = {
                                            "highway=primary" }),
                            @Edge(id = "-39104000002", coordinates = {
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5),
                                    @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3) }, tags = {
                                            "highway=primary" }) }, lines = {
                                                    @Line(id = "39040", coordinates = {
                                                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_0),
                                                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_1),
                                                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_2),
                                                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_3) }, tags = {
                                                                    "barrier=unclassified" }) }, relations = {
                                                                            @Relation(id = "39152", members = {
                                                                                    @Member(id = "39040", type = "line", role = "outer"),
                                                                                    @Member(id = "39056000003", type = "edge", role = "outer"),
                                                                                    @Member(id = "39056000001", type = "edge", role = "outer") }, tags = {
                                                                                            "type=multipolygon",
                                                                                            "name=The Park" }) })
    private Atlas validRelationWithWaySectioning;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_1),
                    @Loc(value = TEST_VALIDRELATION_2), @Loc(value = TEST_VALIDRELATION_3),
                    @Loc(value = TEST_VALIDRELATION_1) }) },
            // Lines
            lines = {
                    @Line(id = "1000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6) }),
                    @Line(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "1000000", type = "line", role = "outer"),
                    @Member(id = "2000000", type = "line", role = "outer") }, tags = "type=multipolygon") })
    private Atlas overlappingOutersAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_1),
                    @Loc(value = TEST_VALIDRELATION_2), @Loc(value = TEST_VALIDRELATION_3),
                    @Loc(value = TEST_VALIDRELATION_1) }) },
            // Lines
            lines = {
                    @Line(id = "1000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6) }),
                    @Line(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "1000000", type = "line", role = "outer"),
                    @Member(id = "2000000", type = "line", role = "outer") }, tags = {
                            "type=multipolygon", "synthetic_relation_member_added=1000000" }) })
    private Atlas overlappingOutersCountrySlicedAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_10),
                    @Loc(value = TEST_VALIDRELATION_11), @Loc(value = TEST_VALIDRELATION_14),
                    @Loc(value = TEST_VALIDRELATION_6), @Loc(value = TEST_VALIDRELATION_10) }),
                    @Area(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_2),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1) }),
                    @Area(id = "3000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "2000000", type = "area", role = "inner"),
                    @Member(id = "3000000", type = "area", role = "outer") }, tags = "type=multipolygon") })
    private Atlas outerInHoleAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_1),
                    @Loc(value = TEST_VALIDRELATION_2), @Loc(value = TEST_VALIDRELATION_3),
                    @Loc(value = TEST_VALIDRELATION_1) }) },
            // Lines
            lines = {
                    @Line(id = "1000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6) }),
                    @Line(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_6),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_7),
                            @Loc(value = TEST_VALIDRELATIONWITHWAYSECTIONING_5) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "inner"),
                    @Member(id = "1000000", type = "line", role = "outer"),
                    @Member(id = "2000000", type = "line", role = "outer") }, tags = "type=multipolygon") })
    private Atlas innerOuterOverlapAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_10),
                    @Loc(value = TEST_VALIDRELATION_11), @Loc(value = TEST_VALIDRELATION_14),
                    @Loc(value = TEST_VALIDRELATION_6), @Loc(value = TEST_VALIDRELATION_10) }),
                    @Area(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_2),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1) }),
                    @Area(id = "3000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "2000000", type = "area", role = "inner"),
                    @Member(id = "3000000", type = "area", role = "inner") }, tags = "type=multipolygon") })
    private Atlas innerOverlapAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_10),
                    @Loc(value = TEST_VALIDRELATION_11), @Loc(value = TEST_VALIDRELATION_14),
                    @Loc(value = TEST_VALIDRELATION_6), @Loc(value = TEST_VALIDRELATION_10) }),
                    @Area(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1) }),
                    @Area(id = "3000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "2000000", type = "area", role = "inner"),
                    @Member(id = "3000000", type = "area", role = "inner") }, tags = "type=multipolygon") })
    private Atlas innerTouchAtlas;

    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "1000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_0),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_2),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_0) }),
                    @Area(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_5),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_5) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "2000000", type = "area", role = "inner") }, tags = "type=multipolygon") })
    private Atlas innerOutsideOuterAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST_VALIDRELATION_10),
                    @Loc(value = TEST_VALIDRELATION_11), @Loc(value = TEST_VALIDRELATION_14),
                    @Loc(value = TEST_VALIDRELATION_6), @Loc(value = TEST_VALIDRELATION_10) }),
                    @Area(id = "2000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_1) }),
                    @Area(id = "3000000", coordinates = {
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_8),
                            @Loc(value = TEST_INVALIDMEMBERTYPE_6),
                            @Loc(value = TEST_VALIDRELATIONTWONOROLE_15) }) },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "outer"),
                    @Member(id = "2000000", type = "area", role = "inner"),
                    @Member(id = "3000000", type = "area", role = "inner") }, tags = {
                            "type=multipolygon", "synthetic_invalid_geometry=yes" }) })
    private Atlas innerTouchSyntheticInvalidAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getInvalidMemberType()
    {
        return this.invalidMemberType;
    }

    public Atlas getNoOuterRelation()
    {
        return this.noOuterRelation;
    }

    public Atlas getOneMemberRelation()
    {
        return this.oneMemberRelation;
    }

    public Atlas getOpenRelation()
    {
        return this.openRelation;
    }

    public Atlas getValidRelation()
    {
        return this.validRelation;
    }

    public Atlas getValidRelationTwoNoRole()
    {
        return this.validRelationTwoNoRole;
    }

    public Atlas getValidRelationWithWaySectioning()
    {
        return this.validRelationWithWaySectioning;
    }

    public Atlas innerOuterOverlapAtlas()
    {
        return this.innerOuterOverlapAtlas;
    }

    public Atlas innerOutsideOuterAtlas()
    {
        return this.innerOutsideOuterAtlas;
    }

    public Atlas innerOverlapAtlas()
    {
        return this.innerOverlapAtlas;
    }

    public Atlas innerTouchAtlas()
    {
        return this.innerTouchAtlas;
    }

    public Atlas innerTouchSyntheticInvalidAtlas()
    {
        return this.innerTouchSyntheticInvalidAtlas;
    }

    public Atlas outerInHoleAtlas()
    {
        return this.outerInHoleAtlas;
    }

    public Atlas overlappingOutersAtlas()
    {
        return this.overlappingOutersAtlas;
    }

    public Atlas overlappingOutersCountrySlicedAtlas()
    {
        return this.overlappingOutersCountrySlicedAtlas;
    }
}
