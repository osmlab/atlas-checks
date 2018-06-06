package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link InvalidLanesTagCheck}
 *
 * @author bbreithaupt
 */
public class InvalidLanesTagCheckTest
{
    @Rule
    public InvalidLanesTagCheckTestRule setup = new InvalidLanesTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"InvalidLanesTagCheck\":{\"lanes.filter\":\"lanes->1,1.5,2\"}}");

    @Test
    public void validLanesTag()
    {
        this.verifier.actual(this.setup.validLanesTag(),
                new InvalidLanesTagCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidLanesTag()
    {
        this.verifier.actual(this.setup.invalidLanesTag(),
                new InvalidLanesTagCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validLanesTagTollBooth()
    {
        this.verifier.actual(this.setup.validLanesTagTollBooth(),
                new InvalidLanesTagCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
