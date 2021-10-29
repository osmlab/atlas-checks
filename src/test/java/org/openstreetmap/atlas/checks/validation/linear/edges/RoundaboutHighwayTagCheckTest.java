package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutHighwayTagCheck}
 * 
 * @author elaineleong
 */
public class RoundaboutHighwayTagCheckTest
{
    @Rule
    public RoundaboutHighwayTagCheckTestRule setup = new RoundaboutHighwayTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void roundaboutWithHighwayTagZeroTest()
    {
        this.verifier.actual(this.setup.roundaboutWithHighwayTagZeroAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
