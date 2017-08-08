package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link SnakeRoadCheckTest} test data
 *
 * @author mgostintsev
 */
public class SnakeRoadCheckTestRule extends CoreTestRule
{
    private static final String ONE = "40.85473, 46.16590";
    private static final String TWO = "40.85616, 46.16755";
    private static final String THREE = "40.85557, 46.16598";
    private static final String FOUR = "40.8527, 46.1598";

    @TestAtlas(loadFromTextResource = "SnakeRoads.txt.gz")
    private Atlas snakeRoadAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)) },

            edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=residential" }),
                    @Edge(id = "2", coordinates = { @Loc(value = FOUR), @Loc(value = ONE),
                            @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=residential" }) })
    private Atlas noSnakeRoadsAtlas;

    public Atlas getNoSnakeRoadAtlas()
    {
        return this.noSnakeRoadsAtlas;
    }

    public Atlas getSnakeRoadAtlas()
    {
        return this.snakeRoadAtlas;
    }
}
