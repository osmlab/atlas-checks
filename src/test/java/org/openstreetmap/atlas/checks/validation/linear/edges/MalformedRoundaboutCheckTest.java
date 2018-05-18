package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link MalformedRoundaboutCheck}
 *
 * @author savannahostrowski
 */
public class MalformedRoundaboutCheckTest
{
    @Rule
    public MalformedRoundaboutCheckTestRule setup = new MalformedRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.verifyEmpty();
    }

    @Test
    public void testClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testCounterClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutLeftDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testCounterClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.verifyEmpty();
    }

    @Test
    public void testMultiDirectionalRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.multiDirectionalRoundaboutAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
