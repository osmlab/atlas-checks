package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

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

    @TestAtlas(points = {
            @TestAtlas.Point(coordinates = @Loc(value = TWO), id = "1234", tags = "highway=stop"),
            @TestAtlas.Point(coordinates = @Loc(value = THREE)),
            @TestAtlas.Point(coordinates = @Loc(value = FOUR)) },

            areas = {
                    @Area(coordinates = { @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, id = "1201", tags = { "highway=trunk" }),
                    @Area(coordinates = { @Loc(value = FOUR), @Loc(value = ONE) }, id = "1202") })
    private Atlas areaWithHighwayAtlas;

    @TestAtlas(points = {
            @TestAtlas.Point(coordinates = @Loc(value = ONE), id = "1234", tags = "highway=give_way"),
            @TestAtlas.Point(coordinates = @Loc(value = THREE)),
            @TestAtlas.Point(coordinates = @Loc(value = FOUR)) },

            lines = {
                    @TestAtlas.Line(coordinates = { @Loc(value = THREE), @Loc(value = TWO),
                            @Loc(value = ONE) }, id = "1201", tags = "railway=rail"),
                    @TestAtlas.Line(coordinates = { @Loc(value = FOUR),
                            @Loc(value = ONE) }, id = "1202") })
    private Atlas edgeWithRailwayAtlas;

    @TestAtlas(points = {
            @TestAtlas.Point(coordinates = @Loc(value = ONE), id = "1234", tags = "highway=give_way"),
            @TestAtlas.Point(coordinates = @Loc(value = THREE)) },

            lines = { @TestAtlas.Line(coordinates = { @Loc(value = THREE), @Loc(value = TWO),
                    @Loc(value = ONE) }, id = "1201", tags = "source=survey") })
    private Atlas loneNodeAtlas;

    public Atlas areaWithHighwayAtlas()
    {
        return this.areaWithHighwayAtlas;
    }

    public Atlas edgeWithRailwayAtlas()
    {
        return this.edgeWithRailwayAtlas;
    }

    public Atlas loneNodeAtlas()
    {
        return this.loneNodeAtlas;
    }
}
