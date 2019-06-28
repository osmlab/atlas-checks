package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutValenceCheck}
 *
 * @author savannahostrowski
 * @author bbreithaupt
 */
public class RoundaboutValenceCheckTest
{
    @Rule
    public RoundaboutValenceCheckTestRule setup = new RoundaboutValenceCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void roundaboutWithValenceFiveTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceFiveAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceFourTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceFourAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceOneTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceOneAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceTwoTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTwoAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceZeroCyclewayTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceZeroCyclewayAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void roundaboutWithValenceZeroTest()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceZeroAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
