package org.openstreetmap.atlas.checks.event;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Unit test rule for {@link CheckFlagEventTest}.
 *
 * @author bbreithaupt
 */
public class CheckFlagEventTestRule extends CoreTestRule
{
    private static final String TEST_1 = "31.335310,-121.009566";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = TEST_1), tags = { "layer=1" }) },
            // Relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "1", type = "node", role = "not_real") }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
