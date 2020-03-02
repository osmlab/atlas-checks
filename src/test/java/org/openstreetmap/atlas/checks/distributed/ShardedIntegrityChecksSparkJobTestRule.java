package org.openstreetmap.atlas.checks.distributed;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test rule for {@link ShardedIntegrityChecksSparkJobTest}.
 *
 * @author bbreithaupt
 */
public class ShardedIntegrityChecksSparkJobTestRule extends CoreTestRule
{
    private static final String TEST1_1 = "48.4199329, -123.3708146";
    private static final String TEST1_2 = "48.4197566, -123.3695244";
    private static final String TEST2_1 = "-41.2774703, 174.7770302";
    private static final String TEST2_2 = "-41.2783868, 174.7770197";

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = TEST1_1)),
                    @Node(coordinates = @Loc(value = TEST1_2)) },
            // Edges
            edges = { @Edge(coordinates = { @Loc(value = TEST1_1), @Loc(value = TEST1_2) }),
                    @Edge(coordinates = { @Loc(value = TEST1_2), @Loc(value = TEST1_1) }) })
    private Atlas bcAtlas;

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = TEST2_1)),
                    @Node(coordinates = @Loc(value = TEST2_2)) },
            // Edges
            edges = { @Edge(coordinates = { @Loc(value = TEST2_1), @Loc(value = TEST2_2) }) })
    private Atlas nzAtlas;

    public Atlas bcAtlas()
    {
        return this.bcAtlas;
    }

    public Atlas nzAtlas()
    {
        return this.nzAtlas;
    }
}
