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
    public void testInvalidHighwayLeftSidewalkRight()
    {
        this.verifier.actual(this.setup.getInvalidHighwayLeftSidewalkRight(),
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
    public void testInvalidValidSidewalkBothSide()
    {
        this.verifier.actual(this.setup.getInvalidSidewalkBothSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testValidSidewalkBothSide()
    {
        this.verifier.actual(this.setup.getValidSidewalkBothSide(),
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
    public void testValidSidewalkRightSide()
    {
        this.verifier.actual(this.setup.getValidSidewalkRightSide(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }
}
