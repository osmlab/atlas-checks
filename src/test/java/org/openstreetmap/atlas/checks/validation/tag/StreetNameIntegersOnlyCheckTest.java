package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link StreetNameIntegersOnlyCheck}
 *
 * @author bbreithaupt
 */

public class StreetNameIntegersOnlyCheckTest
{
    @Rule
    public StreetNameIntegersOnlyCheckTestRule setup = new StreetNameIntegersOnlyCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void motorwayWithIntegerNameTag()
    {
        this.verifier.actual(this.setup.motorwayWithIntegerNameTag(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void motorwayWithNonIntegerNameTag()
    {
        this.verifier.actual(this.setup.motorwayWithNonIntegerNameTag(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void motorwayWithMixedNameTag()
    {
        this.verifier.actual(this.setup.motorwayWithMixedNameTag(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void motorwayWithMixedNameTagIntegerNameLeftTag()
    {
        this.verifier.actual(this.setup.motorwayWithMixedNameTagIntegerNameLeftTag(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
