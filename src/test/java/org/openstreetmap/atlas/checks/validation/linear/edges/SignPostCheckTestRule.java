package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link SignPostCheckTest} data generator
 *
 * @author mkalender
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

    static final String LINK_1 = "47.639336, -122.322525";
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
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1) }, tags = {
                            "highway=trunk_link" }) })
    private Atlas trunkShortTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1) }, tags = {
                            "highway=motorway_link" }) })
    private Atlas trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1) }, tags = {
                            "highway=trunk_link" }) })
    private Atlas motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1) }, tags = {
                            "highway=motorway_link" }) })
    private Atlas motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
            edges = { @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                    @Loc(value = HIGHWAY_3) }, tags = { "highway=primary", "motorroad=yes" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary",
                                    "motorroad=yes" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=primary",
                                    "diff=treat-me-different" }) })
    private Atlas motorroadPrimaryMissingJunctionAndDestinationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = HIGHWAY_1)),
                    @Node(coordinates = @Loc(value = HIGHWAY_3)),
                    @Node(coordinates = @Loc(value = HIGHWAY_5)),
                    @Node(coordinates = @Loc(value = LINK_1)),
                    @Node(coordinates = @Loc(value = LINK_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2),
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
            edges = { @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                    @Loc(value = HIGHWAY_3) }, tags = { "highway=primary", "motorroad=yes" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary",
                                    "motorroad=yes" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=unclassified" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=unclassified" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = LINK_1), @Loc(value = LINK_2),
                            @Loc(value = LINK_3) }, tags = { "highway=primary_link" }),
                    @Edge(coordinates = { @Loc(value = LINK_3), @Loc(value = LINK_4) }, tags = {
                            "highway=primary_link" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = LINK_4), @Loc(value = HIGHWAY2_2),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = LINK_1), @Loc(value = LINK_2),
                            @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY2_1),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = LINK_4), @Loc(value = HIGHWAY2_2),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
            edges = {
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
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
                    @Edge(coordinates = { @Loc(value = HIGHWAY_1), @Loc(value = HIGHWAY_2),
                            @Loc(value = HIGHWAY_3) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = HIGHWAY_4),
                            @Loc(value = HIGHWAY_5) }, tags = { "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = HIGHWAY_3), @Loc(value = LINK_1),
                            @Loc(value = LINK_2), @Loc(value = LINK_3),
                            @Loc(value = LINK_4) }, tags = { "highway=motorway_link",
                                    "destination=somewhere" }) })
    private Atlas motorwayMotorwayLinkWithJunctionAndDestinationAtlas;

    public Atlas motorroadPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorroadPrimaryLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorroadPrimaryMissingJunctionAndDestinationAtlas()
    {
        return this.motorroadPrimaryMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayLinkMotorwayMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayLinkMotorwayMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        return this.motorwayMotorwayLinkMissingJunctionAndDestinationAtlas;
    }

    public Atlas motorwayMotorwayLinkWithJunctionAndDestinationAtlas()
    {
        return this.motorwayMotorwayLinkWithJunctionAndDestinationAtlas;
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

    public Atlas primaryLinkTrunkMissingJunctionAndDestinationAtlas()
    {
        return this.primaryLinkTrunkMissingJunctionAndDestinationAtlas;
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

    public Atlas unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        return this.unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas;
    }
}
