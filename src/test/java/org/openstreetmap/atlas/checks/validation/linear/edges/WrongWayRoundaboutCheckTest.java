package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link WrongWayRoundaboutCheck}
 *
 * @author savannahostrowski
 */
public class WrongWayRoundaboutCheckTest
{

    private final WrongWayRoundaboutCheck check = new WrongWayRoundaboutCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public WrongWayRoundaboutCheckTestRule setup = new WrongWayRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingAtlas(), check);

        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutRightDrivingAtlas(), check);

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testClockwiseRoundaboutWithConnectionsRightDriving()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutWithConnectionsRightDriving(), check);

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

        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
