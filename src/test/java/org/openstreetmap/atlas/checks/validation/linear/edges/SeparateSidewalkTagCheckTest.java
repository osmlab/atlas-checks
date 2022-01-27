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
    public void sidewalkRightSideTruePositive()
    {
        this.verifier.actual(this.setup.getSidewalkRightSideTruePositive(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void sidewalkLeftSideTruePositive()
    {
        this.verifier.actual(this.setup.getSidewalkLeftSideTruePositive(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void sidewalkBothSideTruePositive()
    {
        this.verifier.actual(this.setup.getSidewalkBothSideTruePositive(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void highwaySidewalkRightSeparateSidewalkLeft()
    {
        this.verifier.actual(this.setup.getHighwaySidewalkRightSeparateSidewalkLeft(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void highwaySidewalkLeftSeparateSidewalkRight()
    {
        this.verifier.actual(this.setup.getHighwaySidewalkLeftSeparateSidewalkRight(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void highwaySidewalkBothOnlyOneSidewalkDetected()
    {
        this.verifier.actual(this.setup.getHighwaySidewalkBothOnlyOneSidewalkDetected(),
                new SeparateSidewalkTagCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

}
