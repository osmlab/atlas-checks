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
 */
public class RoundaboutValenceCheckTest
{

    private final RoundaboutValenceCheck check = new RoundaboutValenceCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"RoundaboutValenceCheck\":{\"connections.minimum\":2.0,\"connections.maximum\":14.0}}"));
    @Rule
    public RoundaboutValenceCheckTestRule setup = new RoundaboutValenceCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void roundaboutWithValenceZero()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceZero(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceZeroFourteenMaxConnections()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceZero(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceOne()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceOne(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceTwo()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTwo(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceTwoFourteenMaxConnections()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTwo(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceFour()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceFour(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceTen()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTen(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutWithValenceTenFourteenMaxConnections()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTen(), check);
        this.verifier.verifyEmpty();

    }

    @Test
    public void roundaboutWithValenceEleven()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceEleven(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceFourteen()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceFourteen(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void roundaboutWithValenceFifteen()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceFifteen(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

}
