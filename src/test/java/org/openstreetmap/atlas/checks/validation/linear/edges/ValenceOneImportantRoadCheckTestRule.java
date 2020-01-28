package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test rule for {@link ValenceOneImportantRoadCheckTest}
 *
 * @author brian_l_davis
 */
public class ValenceOneImportantRoadCheckTestRule extends CoreTestRule
{
    private static final String FIVE = "37.778717, -122.472711";
    private static final String FOUR = "37.7767352, -122.474839";
    private static final String ONE = "37.7785877, -122.47495";
    private static final String SIX = "37.7768484, -122.472569";
    private static final String THREE = "37.7767922, -122.473706";
    private static final String TWO = "37.7786587, -122.473859";

    // primary <-> trunk -> motorway -> trunk <-> primary (OK)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=primary" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=primary" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk" }),
                    @Edge(id = "3", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=motorway" }),
                    @Edge(id = "4", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "5", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=primary" }),
                    @Edge(id = "-5", coordinates = { @Loc(value = SIX),
                            @Loc(value = FIVE) }, tags = { "highway=primary" }) })
    private Atlas classificationChange;

    // trunk <-> trunk <-> trunk (OK)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)) }, edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk" }),
                    @Edge(id = "-2", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=trunk" }),
                    @Edge(id = "3", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                    @Edge(id = "-3", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=trunk" }) })
    private Atlas connectedBidirectionalTrunk;

    // motorway_link -> motorway -> motorway -> motorway -> motorway_link (OK)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=motorway_link" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = THREE), @Loc(value = FOUR) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = FOUR), @Loc(value = FIVE) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = SIX) }, tags = {
                            "highway=motorway_link" }) })
    private Atlas connectedMotorway;

    // trunk_link -> trunk -> trunk -> trunk -> trunk_link (OK)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk_link" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                            "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = THREE), @Loc(value = FOUR) }, tags = {
                            "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = FOUR), @Loc(value = FIVE) }, tags = {
                            "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = SIX) }, tags = {
                            "highway=trunk_link" }) })
    private Atlas connectedTrunk;

    // primary -> trunk -> trunk -> *trunk* (BAD)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)),
            @Node(coordinates = @Loc(value = FIVE)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=primary" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                            "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = THREE), @Loc(value = FOUR) }, tags = {
                            "highway=trunk" }),
                    @Edge(coordinates = { @Loc(value = FOUR), @Loc(value = FIVE) }, tags = {
                            "highway=trunk" }) }, lines = {
                                    @Line(coordinates = { @Loc(value = FIVE),
                                            @Loc(value = SIX) }, tags = { "highway=construction",
                                                    "access=no" }) })
    private Atlas deadEndTrunk;

    // *motorway* -> motorway -> motorway_link (BAD)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = THREE), @Loc(value = FOUR) }, tags = {
                            "highway=motorway_link" }) }, lines = {
                                    @Line(coordinates = { @Loc(value = SIX),
                                            @Loc(value = ONE) }, tags = { "highway=proposed",
                                                    "access=no" }) })
    private Atlas noEntryMotorway;

    // motorway_link -> motorway -> * <- motorway <- motorway_link (BAD)
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=motorway_link" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = FOUR), @Loc(value = THREE) }, tags = {
                            "highway=motorway" }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = FOUR) }, tags = {
                            "highway=motorway_link" }) })
    private Atlas opposingOneWays;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)),
            @Node(coordinates = @Loc(value = FIVE), tags = {
                    "synthetic_boundary_node=yes" }) }, edges = {
                            @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                                    "highway=primary" }),
                            @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = {
                                    "highway=trunk" }),
                            @Edge(coordinates = { @Loc(value = THREE),
                                    @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                            @Edge(coordinates = { @Loc(value = FOUR), @Loc(value = FIVE) }, tags = {
                                    "highway=trunk" }) })
    private Atlas deadEndTrunkBoundaryAtlas;

    public Atlas deadEndTrunkBoundaryAtlas()
    {
        return this.deadEndTrunkBoundaryAtlas;
    }

    public Atlas getClassificationChange()
    {
        return this.classificationChange;
    }

    public Atlas getConnectedBidirectionalTrunk()
    {
        return this.connectedBidirectionalTrunk;
    }

    public Atlas getConnectedMotorway()
    {
        return this.connectedMotorway;
    }

    public Atlas getConnectedTrunk()
    {
        return this.connectedTrunk;
    }

    public Atlas getDeadEndTrunk()
    {
        return this.deadEndTrunk;
    }

    public Atlas getNoEntryMotorway()
    {
        return this.noEntryMotorway;
    }

    public Atlas getOpposingOneWays()
    {
        return this.opposingOneWays;
    }
}
