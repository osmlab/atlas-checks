package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutHighwayTagCheck}
 * @author elaineleong
 */
public class RoundaboutHighwayTagCheckTest
{
    @Rule
    public RoundaboutHighwayTagCheckTestRule setup = new RoundaboutHighwayTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void roundaboutWithHighwayTagFiveTest()
    {
        this.verifier.actual(this.setup.roundaboutWithHighwayTagFiveAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithHighwayTagFourTest()
    {
        this.verifier.actual(this.setup.roundaboutWithHighwayTagFourAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithHighwayTagOneTest()
    {
        this.verifier.actual(this.setup.roundaboutWithHighwayTagOneAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithHighwayTagZeroTest()
    {
        this.verifier.actual(this.setup.roundaboutWithHighwayTagZeroAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
