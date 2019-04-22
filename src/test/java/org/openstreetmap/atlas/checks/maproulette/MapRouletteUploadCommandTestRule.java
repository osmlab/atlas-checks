package org.openstreetmap.atlas.checks.maproulette;

import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Data for unit tests for MapRouletteUploadCommand
 *
 * @author nachtm
 */
public class MapRouletteUploadCommandTestRule extends CoreTestRule
{
    private static final String CENTER = "0,0";
    private static final String IDENTIFIER_1 = "1";
    private static final String IDENTIFIER_2 = "2";
    private static final String CHALLENGE = "SomeCheck";
    private static final String INSTRUCTIONS = "Instructions.";

    @TestAtlas(points = { @Point(coordinates = @Loc(value = CENTER), id = "1") })
    private Atlas basicAtlas;

    private CheckFlagEvent getBasicFlag(final String identifier)
    {
        final CheckFlag flag = new CheckFlag(identifier);
        flag.addObject(this.basicAtlas.point(1L));
        flag.addInstruction(INSTRUCTIONS);

        return new CheckFlagEvent(CHALLENGE, flag);
    }

    public CheckFlagEvent getOneBasicFlag()
    {
        return this.getBasicFlag(IDENTIFIER_1);
    }

    public CheckFlagEvent getAnotherBasicFlag()
    {
        return this.getBasicFlag(IDENTIFIER_2);
    }
}
