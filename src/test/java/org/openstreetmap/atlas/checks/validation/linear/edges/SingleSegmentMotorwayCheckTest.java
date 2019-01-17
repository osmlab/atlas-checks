package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link SingleSegmentMotorwayCheck}.
 *
 * @author bbreithaupt
 */
public class SingleSegmentMotorwayCheckTest
{
    @Rule
    public SingleSegmentMotorwayCheckTestRule setup = new SingleSegmentMotorwayCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validMotorwaySegmentsTest()
    {
        this.verifier.actual(this.setup.validMotorwaySegmentsAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidPrimaryMotorwayPrimarySegmentTest()
    {
        this.verifier.actual(this.setup.invalidPrimaryMotorwayPrimarySegmentAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidMotorwaySegmentOneConnectionTest()
    {
        this.verifier.actual(this.setup.invalidMotorwaySegmentOneConnectionAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidMotorwaySegmentOneConnectionRoundaboutTest()
    {
        this.verifier.actual(this.setup.invalidMotorwaySegmentOneConnectionRoundaboutAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

}
