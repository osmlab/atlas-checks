package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link SeparateSidewalkTagCheck}
 *
 * @author vladlemberg
 */

public class SeparateSidewalkTagCheckTest
{
    @Rule
    public SeparateSidewalkTagCheckTestRule setup = new SeparateSidewalkTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private Configuration configuration = ConfigurationResolver.inlineConfiguration(
            "{\"SeparateSidewalkTagCheck\": {\"edge.length\": 20.0,\"sidewalk.search.distance\": 15.0,\"maximum.highway.type\": \"primary\"}}");

    @Test
    public void testInvalidClosedWayRightFootwayLeft()
    {
        this.verifier.actual(this.setup.getInvalidHighwayClosedWaySidewalkLeftSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidHighwayLeftSidewalkRight()
    {
        this.verifier.actual(this.setup.getInvalidHighwayLeftSidewalkRight(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayLeftSidewalkRightAlternativeMapping()
    {
        this.verifier.actual(this.setup.getInvalidHighwayLeftSidewalkRightSideAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayLeftSidewalkRightAlternativeMapping2()
    {
        this.verifier.actual(this.setup.getInvalidHighwayLeftSidewalkRightSideAlternativeMapping2(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayRightFootwayLeft()
    {
        this.verifier.actual(this.setup.getInvalidHighwayRightFootwayLeft(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayRightSidewalkLeft()
    {
        this.verifier.actual(this.setup.getInvalidHighwayRightSidewalkLeft(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayRightSidewalkLeftAlternativeMapping()
    {
        this.verifier.actual(this.setup.getInvalidHighwayRightSidewalkLeftAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayRightSidewalkLeftAlternativeMapping2()
    {
        this.verifier.actual(this.setup.getInvalidHighwayRightSidewalkLeftAlternativeMapping2(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidHighwayRightSidewalkLeftReverseHeading()
    {
        this.verifier.actual(this.setup.getInvalidHighwayRightSidewalkLeftReverseHeading(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidSidewalkBothSameSide()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkBothSameSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidSidewalkBothSidesAlternativeMapping()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkBothSideAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidSidewalkBothSidesLeftMissing()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkBothSideLeftMissing(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidSidewalkBothSidesRightMissing()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkBothSideRightMissing(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidSidewalkDualCarriageWay()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkLHighwayDualCarriageWay(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidSidewalkEdgeDefaultLength()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkLHighwayShotEdge(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNoSidewalk()
    {
        this.verifier.actual(this.setup.getNoSidewalk(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSidewalkCrossing()
    {
        this.verifier.actual(this.setup.getSidewalkCrossing(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSidewalkDifferentLayer()
    {
        this.verifier.actual(this.setup.getSidewalkDifferentLayer(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSidewalkHeadingOutsideDegreeRange()
    {
        this.verifier.actual(this.setup.getSidewalkHeadingOutsideDegreeRange(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSidewalkSharingLocation()
    {
        this.verifier.actual(this.setup.getSidewalkSharingLocation(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkBothSides()
    {
        this.verifier.actual(this.setup.getValidSidewalkBothSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkBothSidesAlternativeMapping()
    {
        this.verifier.actual(this.setup.getValidSidewalkBothSideAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkLeftSide()
    {
        this.verifier.actual(this.setup.getValidSidewalkLeftSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkLeftSideAlternativeMapping()
    {
        this.verifier.actual(this.setup.getValidSidewalkLeftSideAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkRightSide()
    {
        this.verifier.actual(this.setup.getValidSidewalkRightSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSidewalkRightSideAlternativeMapping()
    {
        this.verifier.actual(this.setup.getValidSidewalkRightSideAlternativeMapping(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }
}
