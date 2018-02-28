package org.openstreetmap.atlas.checks.validation.intersections;

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
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
    }

    @Test
    public void roundaboutWithValenceOne()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceOne(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
    }

    @Test
    public void roundaboutWithValenceTwo()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceTwo(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
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
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
    }

    @Test
    public void roundaboutWithValenceEleven()
    {
        this.verifier.actual(this.setup.roundaboutWithValenceEleven(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
    }

}
