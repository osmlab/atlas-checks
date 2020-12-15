package org.openstreetmap.atlas.checks.utility;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link CommonMethods}
 *
 * @author Vladimir Lemberg
 */

public class CommonMethodsTest
{

    @Rule
    public CommonMethodsTestRule setup = new CommonMethodsTestRule();

    @Test
    public void testOneMemberRelationReversed()
    {
        assertEquals(1, CommonMethods
                .getOSMRelationMemberSize(this.setup.getOneMemberRelationReversed().relation(123)));
    }

    @Test
    public void testOneMemberRelationSectioned()
    {
        assertEquals(1, CommonMethods.getOSMRelationMemberSize(
                this.setup.getOneMemberRelationSectioned().relation(123)));
    }

    @Test
    public void testValidRelation()
    {
        assertEquals(3, CommonMethods
                .getOSMRelationMemberSize(this.setup.getValidRelation().relation(123)));
    }
}
