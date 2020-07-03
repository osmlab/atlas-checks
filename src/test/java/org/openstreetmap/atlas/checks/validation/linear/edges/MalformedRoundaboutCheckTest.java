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
 * @author bbreithaupt
 */
public class MalformedRoundaboutCheckTest
{
    @Rule
    public MalformedRoundaboutCheckTestRule setup = new MalformedRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void clockwiseRoundaboutLeftDrivingMissingTagTest()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingMissingTagAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void counterClockwiseConnectedDoubleRoundaboutRightDrivingTest()
    {
        this.verifier.actual(
                this.setup.counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutBridgeRightDrivingEnclosedTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutBridgeRightDrivingEnclosedAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingCyclewayTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingCyclewayAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingEnclosedBridgeTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingEnclosedBridgeAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingEnclosedPathTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingEnclosedPathAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas()
    {
        this.verifier.actual(
                this.setup.counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOneWayNoTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOutsideConnectionTest()
    {
        this.verifier.actual(
                this.setup.counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingWrongEdgesTagTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingWrongEdgesTagAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testClockwiseRoundaboutLeftDrivingConcave()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingConcaveAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
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
    public void testCounterClockwiseRoundaboutRightDrivingMadeLeftAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"traffic.countries.left\":[\"USA\"]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testMultiDirectionalRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.multiDirectionalRoundaboutAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testRoundaboutWithSyntheticNode()
    {
        this.verifier.actual(this.setup.syntheticNode(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
