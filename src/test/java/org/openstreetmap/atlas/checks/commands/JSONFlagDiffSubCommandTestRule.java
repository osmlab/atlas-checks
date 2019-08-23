package org.openstreetmap.atlas.checks.commands;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Unit test rule for subclasses of {@link JSONFlagDiffSubCommand}.
 *
 * @author bbreithaupt
 */
public class JSONFlagDiffSubCommandTestRule extends CoreTestRule
{
    private static final String TEST_1 = "48.8612105319222,2.33573222557574";
    private static final String TEST_2 = "48.8611031384508,2.33615846401356";
    private static final String TEST_3 = "48.8608227210779,2.33599522376078";
    private static final String TEST_4 = "48.8609271319854,2.33555991642003";

    private static final String TEST_INSTRUCTION = "This is a test flag.";
    private static final String TEST_CHECK_1 = "Check1";
    private static final String TEST_CHECK_2 = "Check2";

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)),
            @Node(id = "3000000", coordinates = @Loc(value = TEST_3)),
            @Node(id = "4000000", coordinates = @Loc(value = TEST_4)) })
    private Atlas atlas;

    public CheckFlagEvent getAdditionCheckFlagEvent()
    {
        return this.getSimpleCheckFlagEvent(this.atlas.node(2000000L), TEST_CHECK_1);
    }

    public CheckFlagEvent getConstantCheckFlagEvent()
    {
        final CheckFlagEvent event = this.getSimpleCheckFlagEvent(this.atlas.node(1000000L),
                TEST_CHECK_1);
        event.getCheckFlag().addObject(this.atlas.node(2000000L));
        event.getCheckFlag().addPoint(this.atlas.node(3000000L).getLocation());
        return event;
    }

    public CheckFlagEvent getPostChangeCheckFlagEvent()
    {
        return this.getSimpleCheckFlagEvent(this.atlas.node(3000000L), TEST_CHECK_1);
    }

    public CheckFlagEvent getPreChangeCheckFlagEvent()
    {
        final CheckFlagEvent event = this.getSimpleCheckFlagEvent(this.atlas.node(3000000L),
                TEST_CHECK_1);
        event.getCheckFlag().addObject(this.atlas.node(4000000L));
        return event;
    }

    public CheckFlagEvent getSubtractionCheckFlagEvent()
    {
        return this.getSimpleCheckFlagEvent(this.atlas.node(1000000L), TEST_CHECK_2);
    }

    /**
     * Help to generate simple {@link CheckFlagEvent}s, that have one object and use a test
     * instruction.
     *
     * @param object
     *            an {@link AtlasObject}
     * @param checkName
     *            a {@link String} check name
     * @return a {@link CheckFlagEvent}
     */
    private CheckFlagEvent getSimpleCheckFlagEvent(final AtlasObject object, final String checkName)
    {
        final CheckFlag flag = new CheckFlag(String.valueOf(object.getIdentifier()));
        flag.addObject(object);
        flag.addInstruction(TEST_INSTRUCTION);

        return new CheckFlagEvent(checkName, flag);
    }
}
