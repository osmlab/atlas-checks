package org.openstreetmap.atlas.checks.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testClosedWay()
    {
        assertTrue(CommonMethods.isClosedWay(this.setup.getClosedWay().edge(12000001)));
    }

    @Test
    public void testFirstSection()

    {
        assertTrue(CommonMethods.isFirstWaySection(this.setup.getClosedWay().edge(12000001)));
    }

    @Test
    public void testFirstSectionNegative()

    {
        assertFalse(CommonMethods.isFirstWaySection(this.setup.getClosedWay().edge(12000002)));
    }

    @Test
    public void testOneMemberRelationArtificialMember()
    {
        assertEquals(1, CommonMethods.getOSMRelationMemberSize(
                this.setup.getOneMemberRelationArtificialMember().relation(123)));
    }

    @Test
    public void testOneMemberRelationEdge()
    {
        assertEquals(1, CommonMethods
                .getOSMRelationMemberSize(this.setup.getOneMemberRelationEdge().relation(123)));
    }

    @Test
    public void testOneMemberRelationNode()
    {
        assertEquals(1, CommonMethods
                .getOSMRelationMemberSize(this.setup.getOneMemberRelationNode().relation(123)));
    }

    @Test
    public void testOneMemberRelationReversed()
    {
        assertEquals(1, CommonMethods.getOSMRelationMemberSize(
                this.setup.getOneMemberRelationReversedEdge().relation(123)));
    }

    @Test
    public void testOneMemberRelationSectioned()
    {
        assertEquals(1, CommonMethods.getOSMRelationMemberSize(
                this.setup.getOneMemberRelationSectionedEdge().relation(123)));
    }

    @Test
    public void testOriginalWayGeometry()
    {
        final String origGeom = "LINESTRING (-71.7194204 18.4360044, -71.6970306 18.4360737, -71.7052283 18.4273807, -71.7194204 18.4360044)";
        assertEquals(origGeom, CommonMethods
                .buildOriginalOsmWayGeometry(this.setup.getOriginalWayGeometry().edge(12000003))
                .toString());
    }

    @Test
    public void testUnClosedWay()
    {
        assertFalse(CommonMethods.isClosedWay(this.setup.getUnClosedWay().edge(12000002)));
    }

    @Test
    public void testValidRelation()
    {
        assertEquals(3, CommonMethods
                .getOSMRelationMemberSize(this.setup.getValidRelation().relation(123)));
    }
}
