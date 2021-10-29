package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link MalformedRoundaboutCheck}
 *
 * @author savannahostrowski
 * @author bbreithaupt
 * @author vlemberg
 */
public class MalformedRoundaboutCheckTest
{
    @Rule
    public MalformedRoundaboutCheckTestRule setup = new MalformedRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static void verifyFixSuggestions(final CheckFlag flag, final int count)
    {
        Assert.assertEquals(count, flag.getFixSuggestions().size());
    }

    @Test
    public void clockwiseRoundaboutLeftDrivingMissingTagTest()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingMissingTagAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout does not form a single, one-way, complete, car navigable route.\n"
                        + "3. This roundabout has car navigable ways inside it.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void counterClockwiseConnectedDoubleRoundaboutRightDrivingTest()
    {
        this.verifier.actual(
                this.setup.counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout does not form a single, one-way, complete, car navigable route.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void counterClockwiseRoundaboutBridgeRightDrivingEnclosedTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutBridgeRightDrivingEnclosedAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
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
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingEnclosedPathTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingEnclosedPathAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas()
    {
        this.verifier.actual(
                this.setup.counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout does not form a single, one-way, complete, car navigable route.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOneWayNoTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout does not form a single, one-way, complete, car navigable route.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOutsideConnectionTest()
    {
        this.verifier.actual(
                this.setup.counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingWrongEdgesTagTest()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingWrongEdgesTagAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout has car navigable ways inside it.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void testAngleGreaterThanThreshold()
    {
        this.verifier.actual(this.setup.angleGreaterThanThreshold(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 60.0
                                + ", \"min.nodes\":" + 1.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n2. This roundabout has too sharp of an angle at ((-27.4512972, 153.0120204), (-27.4512764, 153.0121637), (-27.4513581, 153.0121916), (-27.4512564, 153.0120857)), consider adding more nodes or rearranging the roundabout to fix this.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void testClockwiseRoundaboutLeftDrivingAtlas()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testClockwiseRoundaboutLeftDrivingConcave()
    {
        this.verifier.actual(this.setup.clockwiseRoundaboutLeftDrivingConcaveAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testClockwiseRoundaboutRightDrivingAtlas()
    {
        // AutoFix Case
        this.verifier.actual(this.setup.clockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is going the wrong direction, or has been improperly tagged as a roundabout.",
                flags.get(0).getInstructions()));
        this.verifier.verify(flag -> verifyFixSuggestions(flag, 1));
    }

    @Test
    public void testCounterClockwiseRoundaboutLeftDrivingAtlas()
    {
        // AutoFix Case
        this.verifier.actual(this.setup.counterClockwiseRoundaboutLeftDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is going the wrong direction, or has been improperly tagged as a roundabout.",
                flags.get(0).getInstructions()));
        this.verifier.verify(flag -> verifyFixSuggestions(flag, 1));
    }

    @Test
    public void testCounterClockwiseRoundaboutRightDrivingAtlas()
    {
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testCounterClockwiseRoundaboutRightDrivingMadeLeftAtlas()
    {
        // AutoFix Case
        this.verifier.actual(this.setup.counterClockwiseRoundaboutRightDrivingAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"traffic.countries.left\":[\"USA\"]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is going the wrong direction, or has been improperly tagged as a roundabout.",
                flags.get(0).getInstructions()));
        this.verifier.verify(flag -> verifyFixSuggestions(flag, 1));
    }

    @Test
    public void testFewerThanMinOSMNodesRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.fewerThanMinOSMNodesRoundaboutAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver
                        .inlineConfiguration("{\"MalformedRoundaboutCheck\":{\"min.nodes\":" + 9.0
                                + ", \"angle.threshold.maximum_degree\":" + 179.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n2. This roundabout has less than 9 nodes.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void testMultiDirectionalRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.multiDirectionalRoundaboutAtlas(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout does not form a single, one-way, complete, car navigable route.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void testRoundaboutWithEnclosedMultiLayerNavigableRoad()
    {
        this.verifier.actual(this.setup.enclosedMultiLayerNavigableRoad(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testRoundaboutWithEnclosedNavigableRoad()
    {
        this.verifier.actual(this.setup.enclosedNavigableRoad(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. This roundabout is malformed.\n"
                        + "2. This roundabout has car navigable ways inside it.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void testRoundaboutWithEnclosedNavigableRoadArea()
    {
        this.verifier.actual(this.setup.enclosedNavigableRoadArea(),
                new MalformedRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"MalformedRoundaboutCheck\":{\"angle.threshold.maximum_degree\":" + 179.0
                                + ",\"min.nodes\":" + 0.0 + "}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testRoundaboutWithSyntheticNode()
    {
        this.verifier.actual(this.setup.syntheticNode(),
                new MalformedRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
