package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link LoneNodeCheckTest} data provider.
 * 
 * @author mm-ciub on 06/04/2021.
 */
public class LoneNodeCheckTestRule extends CoreTestRule
{
    public static final String ONE = "40.9608783, -5.6421932";
    public static final String TWO = "40.9609609, -5.6424269";
    public static final String THREE = "40.9609826, -5.6425880";
    public static final String FOUR = "40.9610646, -5.6425413";

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), id = "1234", tags = "highway=give_way"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = FOUR)) },

            edges = {
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, id = "1201", tags = "railway=rail"),
                    @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = ONE) }, id = "1202") })
    private Atlas edgeWithRailwayAtlas;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), id = "1234", tags = "highway=give_way"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = { @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                    @TestAtlas.Loc(value = TWO),
                    @TestAtlas.Loc(value = ONE) }, id = "1201", tags = "source=survey") })
    private Atlas loneNodeAtlas;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), id = "1234", tags = "highway=primary"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = { @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                    @TestAtlas.Loc(value = TWO),
                    @TestAtlas.Loc(value = ONE) }, id = "1201", tags = "source=survey") })
    private Atlas loneNodeWithUnexpectedHighwayAtlas;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), id = "1234", tags = "highway=primary"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = { @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                    @TestAtlas.Loc(value = TWO),
                    @TestAtlas.Loc(value = ONE) }, id = "1201", tags = "highway=primary") })
    private Atlas unexpectedHighwayValueAtlas;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = ONE), id = "1234", tags = "highway=give_way"),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = THREE)) },

            edges = { @TestAtlas.Edge(coordinates = { @TestAtlas.Loc(value = THREE),
                    @TestAtlas.Loc(value = TWO),
                    @TestAtlas.Loc(value = ONE) }, id = "1201", tags = "highway=primary") })
    private Atlas validHighwayValueAtlas;

    public Atlas edgeWithRailwayAtlas()
    {
        return this.edgeWithRailwayAtlas;
    }

    public Atlas loneNodeAtlas()
    {
        return this.loneNodeAtlas;
    }

    public Atlas loneNodeWithUnexpectedHighwayAtlas()
    {
        return this.loneNodeWithUnexpectedHighwayAtlas;
    }

    public Atlas unexpectedHighwayValueAtlas()
    {
        return this.unexpectedHighwayValueAtlas;
    }

    public Atlas validHighwayValueAtlas()
    {
        return this.validHighwayValueAtlas;
    }
}
