package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Loads the data for the {@link InvalidMiniRoundaboutCheck} unit tests.
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
    public static final String ITEM_TYPE_TAG = "itemType";
    public static final String EDGE_TAG = "Edge";

    // One two-way street ending in a roundabout -- should be a turning loop/circle despite extra
    // pedestrian Edge.
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
    private Atlas turningCircleAtlas;

    // One two-way Edge into the roundabout and two one-way Edges out -- should be flagged
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
    private Atlas notEnoughValenceAtlas;

    // One one-way in and one one-way out -- shouldn't be a turning circle despite valence == 2
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = "highway=mini_roundabout"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=motorway") })
    private Atlas noTurnsAtlas;

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
    private Atlas validRoundaboutAtlas;

    // A mini-roundabout with valence of 6, but 5 Edges are pedestrian. Should be flagged.
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
    private Atlas pedestrianRoundaboutAtlas;

    // One two-way street ending in a roundabout -- should be a turning loop/circle despite extra
    // pedestrian Edge.
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
    private Atlas turningCircleWithDirectionAtlas;

    // One one-way in and one one-way out -- shouldn't be a turning circle despite valence == 2
    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TWO), tags = {
                    "highway=mini_roundabout", "direction=anticlockwise" }),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) }, edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, id = "100", tags = "highway=motorway"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, id = "101", tags = "highway=motorway") })
    private Atlas noTurnsWithDirectionAtlas;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), tags = "highway=mini_roundabout") })
    private Atlas noRoadsAtlas;

    public Atlas getTurningCircleAtlas()
    {
        return this.turningCircleAtlas;
    }

    public Atlas getNotEnoughValenceAtlas()
    {
        return this.notEnoughValenceAtlas;
    }

    public Atlas getNoTurnsAtlas()
    {
        return this.noTurnsAtlas;
    }

    public Atlas getValidRoundaboutAtlas()
    {
        return this.validRoundaboutAtlas;
    }

    public Atlas getPedestrianRoundaboutAtlas()
    {
        return this.pedestrianRoundaboutAtlas;
    }

    public Atlas getNoRoadsAtlas()
    {
        return this.noRoadsAtlas;
    }

    public Atlas getNoTurnsWithDirectionAtlas()
    {
        return this.noTurnsWithDirectionAtlas;
    }

    public Atlas getTurningCircleWithDirectionAtlas()
    {
        return this.turningCircleWithDirectionAtlas;
    }
}
