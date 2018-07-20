package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link SignPostCheck}
 *
 * @author mkalender
 */
public class SignPostCheckTest
{
    private static SignPostCheck CHECK = new SignPostCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{" + "\"SignPostCheck.source.filter\": \"highway->motorway,trunk,primary|motorroad->yes\", "
                            + "\"SignPostCheck.ramp.filter\": \"highway->motorway_link,trunk_link,primary,primary_link&motorroad->!\","
                            + "\"SignPostCheck.ramp.differentiator.tag\": \"diff\","
                            + "\"SignPostCheck.ramp.angle.difference.degrees\": 90.0" + "}"));

    private static SignPostCheck CHECK2 = new SignPostCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SignPostCheck.source.filter\": \"highway->motorway,trunk\", \"SignPostCheck.ramp.filter\": \"highway->motorway_link,trunk_link\"}"));

    @Rule
    public SignPostCheckTestRule setup = new SignPostCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static void verify(final CheckFlag flag, final int expectedFlaggedObjectCount,
            final boolean shouldFlagDestination, final boolean shouldFlagJunction)
    {
        // Verify that node and edge is flagged
        final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
        Assert.assertEquals(expectedFlaggedObjectCount, flaggedObjects.size());

        // Verify the instruction tag keys and values
        Assert.assertTrue(!shouldFlagDestination || flag.getInstructions().contains("destination"));
        Assert.assertTrue(!shouldFlagJunction
                || flag.getInstructions().contains("highway=motorway_junction"));
    }

    @Test
    public void motorroadPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorroadPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorroadPrimaryMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorroadPrimaryMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayMotorwayLinkWithJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkWithJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMultipleLinksMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMultipleLinksMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 3, true, true));
    }

    @Test
    public void motorwayPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(
                this.setup.motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayTrunkLinkWithDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayTrunkLinkWithDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, false, true));
    }

    @Test
    public void primaryMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.primaryMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void primaryTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.primaryTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void trunkMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void trunkMotorwayLinkWithJunctionAtlas()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkWithJunctionAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }

    @Test
    public void trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void trunkShortTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkShortTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void trunkTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkTrunkLinkMissingJunctionAndDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMotorwayLinkBranchMissingDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkBranchMissingDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, false));
    }

    @Test
    public void motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }
}
