package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Loads the data for the InvalidMiniRoundaboutCheck unit tests.
 *
 * @author nachtm
 */
public class InvalidMiniRoundaboutCheckTestRule extends CoreTestRule
{

    public static final String ONE = "40.9608783, -5.6421932";
    public static final String TWO = "40.9609609, -5.6424269";
    public static final String THREE = "40.9609826, -5.6425880";
    public static final String FOUR = "40.9610646, -5.6425413";

    public static final String NODE_TAG = "Node";
    public static final String ITEM_TYPE_TAG = "ItemType";
    public static final String EDGE_TAG = "Edge";

    // One two-way street ending in a roundabout -- should be a turning loop/circle despite extra
    // pedestrian edge.
    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "-100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=pedestrian") })
    private Atlas turningCircle;

    // One two-way edge into the roundabout and two one-way edges out -- should be flagged
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = FOUR)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "-100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = TWO) }, id = "102", tags = "highway=motorway") })
    private Atlas notEnoughValence;

    // One one-way in and one one-way out -- shouldn't be a turning circle despite valence == 2
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=motorway") })
    private Atlas noTurns;

    // A mini-roundabout with valence of 6 -- should be accepted
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = FOUR)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "-100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO) }, id = "101", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "-101", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = TWO) }, id = "102", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = FOUR) }, id = "-102", tags = "highway=motorway") })
    private Atlas validRoundabout;

    // A mini-roundabout with valence of 6, but 5 edges are pedestrian. Should be flagged.
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = FOUR)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "-100", tags = "highway=pedestrian"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO) }, id = "101", tags = "highway=pedestrian"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "-101", tags = "highway=pedestrian"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = TWO) }, id = "102", tags = "highway=pedestrian"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = FOUR) }, id = "-102", tags = "highway=pedestrian") })
    private Atlas pedestrianRoundabout;

    // One two-way street ending in a roundabout -- should be a turning loop/circle despite extra
    // pedestrian edge.
    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), tags = {
                    "highway=mini_roundabout", "direction=clockwise" }),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "-100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=pedestrian") })
    private Atlas turningCircleWithDirection;

    // One one-way in and one one-way out -- shouldn't be a turning circle despite valence == 2
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = {
                    "highway=mini_roundabout", "direction=anticlockwise" }),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=motorway") })
    private Atlas noTurnsWithDirection;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), tags = "highway=mini_roundabout") })
    private Atlas noRoads;

    public Atlas getTurningCircle()
    {
        return this.turningCircle;
    }

    public Atlas getNotEnoughValence()
    {
        return this.notEnoughValence;
    }

    public Atlas getNoTurns()
    {
        return this.noTurns;
    }

    public Atlas getValidRoundabout()
    {
        return this.validRoundabout;
    }

    public Atlas getPedestrianRoundabout()
    {
        return this.pedestrianRoundabout;
    }

    public Atlas getNoRoads()
    {
        return this.noRoads;
    }

    public Atlas getNoTurnsWithDirection()
    {
        return this.noTurnsWithDirection;
    }

    public Atlas getTurningCircleWithDirection()
    {
        return this.turningCircleWithDirection;
    }
}
