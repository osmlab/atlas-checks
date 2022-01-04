package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link InvalidTurnLanesValueCheck}
 *
 * @author mselaineleong
 */
public class InvalidTurnLanesValueCheckTest
{
    @Rule
    public InvalidTurnLanesValueCheckTestRule setup = new InvalidTurnLanesValueCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidTurnLanesValue()
    {
        this.verifier.actual(this.setup.invalidTurnLanesValue(),
                new InvalidTurnLanesValueCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validTurnLanesValue()
    {
        this.verifier.actual(this.setup.validTurnLanesValue(),
                new InvalidTurnLanesValueCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
