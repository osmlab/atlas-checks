package org.openstreetmap.atlas.checks.base.checks;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Atlas used to test piers. The atlas is very simple, containing 3 edges with one of the edges
 * containing the tag "man_made=pier".
 * 
 * @author cuthbertm
 */
public class PierTestRule extends CoreTestRule
{
    private static final String ONE = "29.920386, -2.089355";
    private static final String TWO = "29.920535, -2.088497";
    private static final String THREE = "29.920014, -2.088754";
    private static final String FOUR = "29.915067, -2.065988";
    private static final String FIVE = "29.901898, -2.092338";

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)),
            @Node(id = "3", coordinates = @Loc(value = THREE)),
            @Node(id = "4", coordinates = @Loc(value = FOUR)),
            @Node(id = "5", coordinates = @Loc(value = FIVE)) },

            edges = {
                    // man_made pier will cause this edge to be skipped
                    @Edge(id = "100", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk", "man_made=pier" }),
                    @Edge(id = "101", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                    @Edge(id = "102", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = FOUR), @Loc(value = FIVE) }, tags = { "highway=trunk" }) })

    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
