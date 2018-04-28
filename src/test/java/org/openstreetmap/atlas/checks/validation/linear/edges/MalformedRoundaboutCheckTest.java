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

    private final MalformedRoundaboutCheck check = new MalformedRoundaboutCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public MalformedRoundaboutCheckTestRule setup = new MalformedRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingAtlas(), check);

        this.verifier.verifyEmpty();
    }

    @Test
    public void testClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutRightDrivingAtlas(), check);

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testCounterClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutLeftDrivingAtlas(), check);

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testCounterClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingAtlas(), check);

        this.verifier.verifyEmpty();
    }

    @Test
    public void testMultiDirectionalRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.multiDirectionalRoundaboutAtlas(), check);

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
