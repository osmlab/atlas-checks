package org.openstreetmap.atlas.checks.commands;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

public class FlagStatisticsSubCommandTestRule extends CoreTestRule
{
    private static final String TEST_1 = "-15.950131,-5.683072";

    private static final String TEST_INSTRUCTION = "This is a test flag.";

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)) })
    private Atlas atlas;

    /**
     * Generate a simple {@link CheckFlagEvent}, that have one node and use a test instruction.
     *
     * @param checkName
     *            a {@link String} check name
     * @return a {@link CheckFlagEvent}
     */
    public CheckFlagEvent getOneNodeCheckFlagEvent(final String checkName)
    {
        final AtlasObject node = atlas.node(1000000L);
        final CheckFlag flag = new CheckFlag(String.valueOf(node.getIdentifier()));
        flag.addObject(node);
        flag.addInstruction(TEST_INSTRUCTION);

        return new CheckFlagEvent(checkName, flag);
    }

}
