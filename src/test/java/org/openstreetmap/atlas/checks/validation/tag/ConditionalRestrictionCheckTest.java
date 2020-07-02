package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author laura on 09/06/2020.
 */
public class ConditionalRestrictionCheckTest
{

    @Rule
    public ConditionalRestrictionCheckTestRule setup = new ConditionalRestrictionCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void accessLanesType()
    {
        this.verifier.actual(this.setup.getAccessLanes(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidAccessLanes()
    {
        this.verifier.actual(this.setup.getInvalidAccessLanes(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional value \"1 @ (Mo-Fr 06:30-09:00,16:00-18:30)\" does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ")));

    }

    @Test
    public void invalidAccessType()
    {
        this.verifier.actual(this.setup.getInvalidAccessType(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional value \"notallowed @ (Sa 08:00-16:00)\" does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ")));

    }

    @Test
    public void invalidConditionFormat()
    {
        this.verifier.actual(this.setup.getInvalidConditionFormat(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional value \"no@(Sa 08:00-16:00)\" does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ")));

    }

    @Test
    public void invalidConditionalKey()
    {
        this.verifier.actual(this.setup.getInvalidConditionalKey(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional key \"hgv:maxweight:conditional\" does not respect the \"<restriction-type>[:<transportation mode>][:<direction>]:conditional\" format")));
    }

    @Test
    public void reversedLanesTransportationMode()
    {
        this.verifier.actual(this.setup.getReversedLanesTransport(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validRestriction()
    {
        this.verifier.actual(this.setup.getConditionalWay(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
