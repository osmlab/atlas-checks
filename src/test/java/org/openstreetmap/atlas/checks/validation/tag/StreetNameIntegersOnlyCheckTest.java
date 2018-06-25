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
    public void motorwayWithIntegerNameTagTest()
    {
        this.verifier.actual(this.setup.motorwayWithIntegerNameTagAtlas(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void motorwayWithNonIntegerNameTagTest()
    {
        this.verifier.actual(this.setup.motorwayWithNonIntegerNameTagAtlas(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void motorwayWithMixedNameTagTest()
    {
        this.verifier.actual(this.setup.motorwayWithMixedNameTagAtlas(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void motorwayWithMixedNameTagIntegerNameLeftTagTest()
    {
        this.verifier.actual(this.setup.motorwayWithMixedNameTagIntegerNameLeftTagAtlas(),
                new StreetNameIntegersOnlyCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
