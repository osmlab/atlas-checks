package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link SharpAngleCheck}
 *
 * @author bbreithaupt
 */
public class SharpAngleCheckTest
{
    @Rule
    public SharpAngleCheckTestRule setup = new SharpAngleCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver
            .inlineConfiguration("{\"SharpAngleCheck\":{\"threshold.degrees\": 97.0}}");

    @Test
    public void notSharpeAngleTest()
    {
        this.verifier.actual(this.setup.notSharpeAngleAtlas(),
                new SharpAngleCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void sharpeAngleTest()
    {
        this.verifier.actual(this.setup.sharpeAngleAtlas(),
                new SharpAngleCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void sharpeAnglesTest()
    {
        this.verifier.actual(this.setup.sharpeAnglesAtlas(),
                new SharpAngleCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier
                .verify(flag -> Assert.assertTrue(flag.getInstructions().contains("The node at ")));
    }
}
