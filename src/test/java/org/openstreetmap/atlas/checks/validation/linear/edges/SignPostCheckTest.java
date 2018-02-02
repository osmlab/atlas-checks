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
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public SignPostCheckTestRule setup = new SignPostCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static void verify(final CheckFlag flag, final boolean shouldFlagDestination,
            final boolean shouldFlagJunction)
    {
        // Verify that only node is flagged
        final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
        Assert.assertEquals(1, flaggedObjects.size());

        // Verify the node id
        Assert.assertEquals(SignPostCheckTestRule.JUNCTION_NODE_ID, flag.getIdentifier());

        // Verify the instruction tag keys and values
        Assert.assertTrue(!shouldFlagDestination || flag.getInstructions().contains("destination"));
        Assert.assertTrue(!shouldFlagJunction
                || flag.getInstructions().contains("highway=motorway_junction"));
    }

    @Test
    public void motorwayMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, true, true));
    }

    @Test
    public void motorwayMotorwayLinkWithJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkWithJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMultipleLinksMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayMultipleLinksMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, true, true));
    }

    @Test
    public void motorwayPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
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
        this.verifier.verify(flag -> verify(flag, true, true));
    }

    @Test
    public void motorwayTrunkLinkWithDestinationAtlas()
    {
        this.verifier.actual(this.setup.motorwayTrunkLinkWithDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, false, true));
    }

    @Test
    public void primaryMotorwayLinkAtlas()
    {
        this.verifier.actual(this.setup.primaryMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void primaryTrunkLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.primaryTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void trunkMotorwayLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, true, true));
    }

    @Test
    public void trunkMotorwayLinkWithJunctionAtlas()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkWithJunctionAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, true, false));
    }

    @Test
    public void trunkPrimaryLinkMissingJunctionAndDestinationAtlas()
    {
        this.verifier.actual(this.setup.trunkPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
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
        this.verifier.verify(flag -> verify(flag, true, true));
    }
}
