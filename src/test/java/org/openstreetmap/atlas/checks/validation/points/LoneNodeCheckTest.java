package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link LoneNodeCheck} unit tests.
 * 
 * @author mm-ciub on 06/04/2021.
 */
public class LoneNodeCheckTest
{

    private static final LoneNodeCheck CHECK = new LoneNodeCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public LoneNodeCheckTestRule setup = new LoneNodeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void edgeWithRailwayTagTest()
    {
        this.verifier.actual(this.setup.edgeWithRailwayAtlas(), CHECK);
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void loneNodeTest()
    {
        this.verifier.actual(this.setup.loneNodeAtlas(), CHECK);
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            flags.forEach(flag -> Assert.assertEquals(1, flag.getRawInstructions().size()));
        });
    }

}
