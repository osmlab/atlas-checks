package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test atlases for {@link AtGradeSignPostCheckTest}
 *
 * @author sayas01
 */
public class AtGradeSignPostCheckTestRule extends CoreTestRule
{

    private static final String INTERSECTING_LOC_ONE = "42.6843556, 23.3008163";
    private static final String EDGE_ONE_START_LOC = "42.6844473, 23.3007951";
    private static final String EDGE_ONE_END_LOC = "42.6833464, 23.3010915";
    private static final String EDGE_TWO_START_LOC = "42.6843436, 23.3007210";
    private static final String EDGE_TWO_END_LOC = "42.6843649, 23.3008922";
    private static final String ROUNDABOUT_LOC_ONE = "43.2097940, 23.5532672";
    private static final String ROUNDABOUT_LOC_TWO = "43.2097318, 23.5529942";
    private static final String ROUNDABOUT_LOC_THREE = "43.2095883, 23.5530627";
    private static final String ROUNDABOUT_LOC_FOUR = "43.2095262, 23.5532058";
    private static final String ROUNDABOUT_LOC_FIVE = "43.2095410, 23.5532935";
    private static final String ROUNDABOUT_LOC_SIX = "43.2097333, 23.5533566";
    private static final String ROUNDABOUT_CONNECTOR_LOC_ONE = "43.2100746, 23.5528153";
    private static final String ATGRADE_LINK_JUNCTION = "2.2293749, 102.2986680";
    private static final String LINK_JUNCTION_IN_EDGE_START_LOC = "2.2294533, 102.2991744";
    private static final String LINK_ROAD_END_LOC = "2.2290532, 102.2987598";
    private static final String LINK_JUNCTION_OUT_EDGE_END = "2.2293645, 102.2985750";

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = INTERSECTING_LOC_ONE)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_END_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_END_LOC)) },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = { @Loc(value = EDGE_ONE_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=primary" }),
                    @Edge(id = "100010002", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_ONE_END_LOC) }, tags = { "highway=primary" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = EDGE_TWO_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=residential" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_TWO_END_LOC) }, tags = { "highway=secondary" }) })
    private Atlas missingDestinationSignRelationAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "300010000", coordinates = @Loc(value = INTERSECTING_LOC_ONE)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_END_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_END_LOC)) },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = { @Loc(value = EDGE_ONE_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=primary" }),
                    @Edge(id = "100010002", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_ONE_END_LOC) }, tags = { "highway=primary" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = EDGE_TWO_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=residential" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_TWO_END_LOC) }, tags = {
                                    "highway=secondary" }) }, relations = {
                                            @Relation(id = "200010000", members = {
                                                    @Member(id = "100010001", type = "edge", role = "from"),
                                                    @Member(id = "300010000", type = "node", role = "intersection"),
                                                    @Member(id = "100010004", type = "edge", role = "to") }, tags = {
                                                            "destination=Център",
                                                            "type=destination_sign" }) })
    private Atlas incompleteDestinationSignRelationAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "300010000", coordinates = @Loc(value = INTERSECTING_LOC_ONE)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_ONE_END_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_START_LOC)),
                    @Node(coordinates = @Loc(value = EDGE_TWO_END_LOC)) },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = { @Loc(value = EDGE_ONE_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=primary" }),
                    @Edge(id = "100010002", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_ONE_END_LOC) }, tags = { "highway=primary" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = EDGE_TWO_START_LOC),
                            @Loc(value = INTERSECTING_LOC_ONE) }, tags = { "highway=residential" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = INTERSECTING_LOC_ONE),
                            @Loc(value = EDGE_TWO_END_LOC) }, tags = {
                                    "highway=secondary" }) }, relations = {
                                            @Relation(id = "200010000", members = {
                                                    @Member(id = "100010001", type = "edge", role = "from"),
                                                    @Member(id = "300010000", type = "node", role = "intersection"),
                                                    @Member(id = "100010004", type = "edge", role = "to") }, tags = {
                                                            "destination=Център",
                                                            "type=destination_sign" }),
                                            @Relation(id = "400010000", members = {
                                                    @Member(id = "100010001", type = "edge", role = "from"),
                                                    @Member(id = "300010000", type = "node", role = "intersection"),
                                                    @Member(id = "100010004", type = "edge", role = "to") }, tags = {
                                                            "type=destination_sign" }), })
    private Atlas missingDestinationSignTagAtlas;

    @TestAtlas(
            // Nodes
            nodes = {
                    @Node(id = "300010000", coordinates = @Loc(value = ROUNDABOUT_CONNECTOR_LOC_ONE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_FIVE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_FOUR)),
                    @Node(id = "700010000", coordinates = @Loc(value = ROUNDABOUT_LOC_ONE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_THREE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_TWO)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_SIX)), },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = { @Loc(value = ROUNDABOUT_LOC_ONE),
                            @Loc(value = ROUNDABOUT_LOC_TWO) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010002", coordinates = { @Loc(value = ROUNDABOUT_LOC_TWO),
                            @Loc(value = ROUNDABOUT_LOC_THREE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = ROUNDABOUT_LOC_THREE),
                            @Loc(value = ROUNDABOUT_LOC_FOUR) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = ROUNDABOUT_LOC_FOUR),
                            @Loc(value = ROUNDABOUT_LOC_FIVE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010005", coordinates = { @Loc(value = ROUNDABOUT_LOC_FIVE),
                            @Loc(value = ROUNDABOUT_LOC_SIX) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010006", coordinates = { @Loc(value = ROUNDABOUT_LOC_SIX),
                            @Loc(value = ROUNDABOUT_LOC_ONE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "200010004", coordinates = { @Loc(value = ROUNDABOUT_LOC_ONE),
                            @Loc(value = ROUNDABOUT_CONNECTOR_LOC_ONE) }, tags = {
                                    "highway=primary", "oneway=yes" }) },
            // Relations
            relations = {
                    @Relation(id = "200010000", members = {
                            @Member(id = "100010006", type = "edge", role = "from"),
                            @Member(id = "700010000", type = "node", role = "intersection"),
                            @Member(id = "200010004", type = "edge", role = "to") }, tags = {
                                    "destination=Център", "type=destination_sign" }),
                    @Relation(id = "400010000", members = {
                            @Member(id = "100010001", type = "edge", role = "from"),
                            @Member(id = "300010000", type = "node", role = "intersection"),
                            @Member(id = "100010004", type = "edge", role = "to") }, tags = {
                                    "type=destination_sign" }), })
    private Atlas roundaboutConnectorMissingDestinationSignTagAtlas;

    @TestAtlas(
            // Nodes
            nodes = {
                    @Node(id = "300010000", coordinates = @Loc(value = ROUNDABOUT_CONNECTOR_LOC_ONE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_FIVE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_FOUR)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_ONE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_THREE)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_TWO)),
                    @Node(coordinates = @Loc(value = ROUNDABOUT_LOC_SIX)), },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = { @Loc(value = ROUNDABOUT_LOC_ONE),
                            @Loc(value = ROUNDABOUT_LOC_TWO) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010002", coordinates = { @Loc(value = ROUNDABOUT_LOC_TWO),
                            @Loc(value = ROUNDABOUT_LOC_THREE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = ROUNDABOUT_LOC_THREE),
                            @Loc(value = ROUNDABOUT_LOC_FOUR) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = ROUNDABOUT_LOC_FOUR),
                            @Loc(value = ROUNDABOUT_LOC_FIVE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010005", coordinates = { @Loc(value = ROUNDABOUT_LOC_FIVE),
                            @Loc(value = ROUNDABOUT_LOC_SIX) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "100010006", coordinates = { @Loc(value = ROUNDABOUT_LOC_SIX),
                            @Loc(value = ROUNDABOUT_LOC_ONE) }, tags = { "highway=primary",
                                    "junction=roundabout" }),
                    @Edge(id = "200010004", coordinates = { @Loc(value = ROUNDABOUT_LOC_ONE),
                            @Loc(value = ROUNDABOUT_CONNECTOR_LOC_ONE) }, tags = {
                                    "highway=primary", "oneway=yes" }) })
    private Atlas roundaboutIntersectionMissingDestinationSignRelationAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "300010000", coordinates = @Loc(value = ATGRADE_LINK_JUNCTION)),
                    @Node(coordinates = @Loc(value = LINK_JUNCTION_IN_EDGE_START_LOC)),
                    @Node(coordinates = @Loc(value = LINK_ROAD_END_LOC)),
                    @Node(coordinates = @Loc(value = LINK_JUNCTION_OUT_EDGE_END)) },
            // Edges
            edges = {
                    @Edge(id = "100010001", coordinates = {
                            @Loc(value = LINK_JUNCTION_IN_EDGE_START_LOC),
                            @Loc(value = ATGRADE_LINK_JUNCTION) }, tags = { "highway=primary" }),
                    @Edge(id = "100010002", coordinates = {
                            @Loc(value = LINK_JUNCTION_IN_EDGE_START_LOC),
                            @Loc(value = LINK_ROAD_END_LOC) }, tags = { "highway=primary_link" }),
                    @Edge(id = "100010003", coordinates = { @Loc(value = ATGRADE_LINK_JUNCTION),
                            @Loc(value = LINK_ROAD_END_LOC) }, tags = { "highway=secondary" }),
                    @Edge(id = "100010004", coordinates = { @Loc(value = ATGRADE_LINK_JUNCTION),
                            @Loc(value = LINK_JUNCTION_OUT_EDGE_END) }, tags = {
                                    "highway=secondary" }) })
    private Atlas linkRoadConnectedAtGradeJunctionAtlas;

    public Atlas getIncompleteDestinationSignRelationAtlas()
    {
        return this.incompleteDestinationSignRelationAtlas;
    }

    public Atlas getLinkRoadConnectedAtGradeJunctionAtlas()
    {
        return this.linkRoadConnectedAtGradeJunctionAtlas;
    }

    public Atlas getMissingDestinationSignRelationAtlas()
    {
        return this.missingDestinationSignRelationAtlas;
    }

    public Atlas getMissingDestinationSignTagAtlas()
    {
        return this.missingDestinationSignTagAtlas;
    }

    public Atlas getRoundaboutConnectorMissingDestinationSignTagAtlas()
    {
        return this.roundaboutConnectorMissingDestinationSignTagAtlas;
    }

    public Atlas getRoundaboutIntersectionMissingDestinationSignRelation()
    {
        return this.roundaboutIntersectionMissingDestinationSignRelationAtlas;
    }
}
