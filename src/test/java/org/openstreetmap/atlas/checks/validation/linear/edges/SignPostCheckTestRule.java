package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link SignPostCheckTest} data generator
 *
 * @author mkalender
 * @author bbreithaupt
 */
public class SignPostCheckTestRule extends CoreTestRule
{
    private static final String HIGHWAY_1 = "47.636459, -122.322678";
    private static final String HIGHWAY_2 = "47.637684, -122.322617";
    private static final String HIGHWAY_3 = "47.639133, -122.322647";
    private static final String HIGHWAY_4 = "47.641319, -122.322601";
    private static final String HIGHWAY_5 = "47.643585, -122.322487";

    private static final String HIGHWAY2_1 = "47.642048, -122.324181";
    private static final String HIGHWAY2_2 = "47.642769, -122.317772";
    private static final String HIGHWAY2_3 = "47.643078, -122.314384";

    private static final String LINK_1 = "47.639336, -122.322525";
    private static final String LINK_2 = "47.640697, -122.322395";
    private static final String LINK_3 = "47.642033, -122.321556";
    private static final String LINK_4 = "47.642471, -122.319885";

    private static final String LINK2_1 = "47.641216, -122.3218";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1) }, tags = { "highway=trunk_link" }) })
    private Atlas trunkShortTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1) }, tags = { "highway=motorway_link" }) })
    private Atlas trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1) }, tags = { "highway=trunk_link" }) })
    private Atlas motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1) }, tags = { "highway=motorway_link" }) })
    private Atlas motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=trunk_link" }) })
    private Atlas trunkTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }) })
    private Atlas trunkMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=trunk_link" }) })
    private Atlas motorwayTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }) })
    private Atlas motorwayMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)),
                    @Node(coordinates = @Loc(value = LINK2_1)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2),
                            @Loc(value = LINK2_1) }, tags = { "highway=trunk_link" }) })
    private Atlas motorwayMultipleLinksMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = {
                                    "highway=primary", "motorroad=yes" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4), @Loc(value = HIGHWAY_5) }, tags = {
                                    "highway=primary", "motorroad=yes" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=primary_link" }) })
    private Atlas motorroadPrimaryLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=unclassified" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=unclassified" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=primary_link" }) })
    private Atlas unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=primary_link" }) })
    private Atlas motorwayPrimaryLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=trunk_link" }) })
    private Atlas primaryTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_3)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_3)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = LINK_2),
                            @Loc(value = LINK_3) }, tags = { "highway=primary_link" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=primary_link" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = HIGHWAY2_3) }, tags = { "highway=motorway" }) })
    private Atlas primaryLinkTrunkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }) })
    private Atlas primaryMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_3)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = HIGHWAY2_3) }, tags = { "highway=motorway" }) })
    private Atlas motorwayLinkMotorwayMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=trunk_link",
                                    "destination=somewhere" }) })
    private Atlas motorwayTrunkLinkWithDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3), tags = {
                            "highway=motorway_junction" }),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = { @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                    @Loc(value = HIGHWAY_2), @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }) })
    private Atlas trunkMotorwayLinkWithJunctionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3), tags = {
                            "highway=motorway_junction" }),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link",
                                    "destination=somewhere" }) })
    private Atlas motorwayMotorwayLinkWithJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3), tags = {
                            "highway=motorway_junction" }),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1), @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link",
                                    "destination:ref=somewhere" }) })
    private Atlas motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_4)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_3)),
                    @Node(coordinates = @Loc(value = LINK_2)),
                    @Node(coordinates = @Loc(value = LINK_3)),
                    @Node(coordinates = @Loc(value = LINK_4)),
                    @Node(coordinates = @Loc(value = LINK2_1)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_5),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_1) }, tags = { "highway=motorway" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY2_3),
                            @Loc(value = HIGHWAY2_2) }, tags = { "highway=primary" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = HIGHWAY2_2),
                            @Loc(value = LINK_4) }, tags = { "highway=primary" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_1) }, tags = { "highway=primary" }),
                    @Edge(id = "6000000", coordinates = { @Loc(value = LINK_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "7000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = LINK_3),
                            @Loc(value = LINK_2) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "8000000", coordinates = { @Loc(value = HIGHWAY2_2),
                            @Loc(value = LINK2_1),
                            @Loc(value = LINK_2) }, tags = { "highway=motorway_link" }) })
    private Atlas motorwayMotorwayLinkBranchMissingDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_4)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5), tags = {
                            "highway=motorway_junction" }),
                    @Node(coordinates = @Loc(value = HIGHWAY2_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_3)),
                    @Node(coordinates = @Loc(value = LINK_2)),
                    @Node(id = "11000000", coordinates = @Loc(value = LINK_3)),
                    @Node(coordinates = @Loc(value = LINK_4)),
                    @Node(coordinates = @Loc(value = LINK2_1)) },
            // edges
            edges = {
                    @Edge(id = "4000000", coordinates = { @Loc(value = HIGHWAY_5),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_1) }, tags = { "highway=motorway" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY2_3),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = LINK_4) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = HIGHWAY2_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-2000000", coordinates = { @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=primary" }),
                    @Edge(id = "6000000", coordinates = { @Loc(value = LINK_3),
                            @Loc(value = LINK_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "-3000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = LINK_3) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "7000000", coordinates = { @Loc(value = HIGHWAY_5),
                            @Loc(value = LINK_3) }, tags = { "highway=motorway_link",
                                    "destination=somewhere" }), })
    private Atlas motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_4)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5), tags = {
                            "highway=motorway_junction" }),
                    @Node(coordinates = @Loc(value = HIGHWAY2_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY2_3)),
                    @Node(coordinates = @Loc(value = LINK_2)),
                    @Node(id = "11000000", coordinates = @Loc(value = LINK_3)),
                    @Node(coordinates = @Loc(value = LINK_4)),
                    @Node(coordinates = @Loc(value = LINK2_1)) },
            // edges
            edges = {
                    @Edge(id = "4000000", coordinates = { @Loc(value = HIGHWAY_5),
                            @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_1) }, tags = { "highway=motorway" }),
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY2_3),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = LINK_4) }, tags = { "highway=primary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_2),
                            @Loc(value = HIGHWAY2_3) }, tags = { "highway=primary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = HIGHWAY2_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-2000000", coordinates = { @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=primary" }),
                    @Edge(id = "6000000", coordinates = { @Loc(value = LINK_3),
                            @Loc(value = LINK_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "-3000000", coordinates = { @Loc(value = LINK_4),
                            @Loc(value = LINK_3) }, tags = { "highway=motorway_link" }),
                    @Edge(id = "7000000", coordinates = { @Loc(value = HIGHWAY_5),
                            @Loc(value = LINK_3) }, tags = { "highway=motorway_link",
                                    "destination=somewhere" }), },
            // relations
            relations = {
                    @Relation(members = { @Member(id = "7000000", type = "edge", role = "from"),
                            @Member(id = "11000000", type = "node", role = "intersection"),
                            @Member(id = "6000000", type = "edge", role = "to") }, tags = {
                                    "type=destination_sign", "destination=somewhere" }) })
    private Atlas motorwayMotorwayLinkTwoWayBranchMissingDestinationRelationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_2)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_4)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_2)) },
            // edges
            edges = {
                    @Edge(id = "1000000", coordinates = { @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = HIGHWAY_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "-2000000", coordinates = { @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = HIGHWAY_1),
                            @Loc(value = LINK_1) }, tags = { "highway=trunk" }),
                    @Edge(id = "-3000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = HIGHWAY_1) }, tags = { "highway=trunk" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = LINK_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "-4000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = LINK_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = HIGHWAY_3),
                            @Loc(value = LINK_1) }, tags = { "highway=trunk_link" }),
                    @Edge(id = "-5000000", coordinates = { @Loc(value = LINK_1),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk_link" }) })
    private Atlas twoWayTrunksUTurnMissingJunctionAndDestinationAtlas;

    public Atlas motorroadPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorroadPrimaryLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkBranchMissingDestinationAtlas()
    {
        return this.motorwayMotorwayLinkBranchMissingDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas()
    {
        return this.motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkTwoWayBranchMissingDestinationRelationAtlas()
    {
        return this.motorwayMotorwayLinkTwoWayBranchMissingDestinationRelationAtlas;
    }

    public Atlas motorwayMotorwayLinkWithJunctionAndDestinationAtlas()
    {
        return this.motorwayMotorwayLinkWithJunctionAndDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas()
    {
        return this.motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas;
    }

    public Atlas motorwayMultipleLinksMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayMultipleLinksMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayPrimaryLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayTrunkLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayTrunkLinkWithDestinationAtlas()
    {
        return this.motorwayTrunkLinkWithDestinationAtlas;
    }

    public Atlas primaryMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.primaryMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas primaryTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        return this.primaryTrunkLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas trunkMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.trunkMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas trunkMotorwayLinkWithJunctionAtlas()
    {
        return this.trunkMotorwayLinkWithJunctionAtlas;
    }

    public Atlas trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas trunkShortTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        return this.trunkShortTrunkLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas trunkTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        return this.trunkTrunkLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas twoWayTrunksUTurnMissingJunctionAndDestinationAtlas()
    {
        return this.twoWayTrunksUTurnMissingJunctionAndDestinationAtlas;
    }

    public Atlas unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        return this.unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas;
    }
}
