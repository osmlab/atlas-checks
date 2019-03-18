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
 * @author bbreithaupt
 */
public class SignPostCheckTest
{
    private static SignPostCheck CHECK = new SignPostCheck(
            ConfigurationResolver.emptyConfiguration());

    private static SignPostCheck CHECK2 = new SignPostCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SignPostCheck.source.filter\":\"highway->motorway,trunk,primary\", \"SignPostCheck.ramp.filter\":\"highway->motorway_link,trunk_link,primary_link\"}"));

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
    public void motorroadPrimaryLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorroadPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayMotorwayLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayMotorwayLinkWithJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkWithJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMotorwayLinkWithJunctionAndDestinationRefTest()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkWithJunctionAndDestinationRefAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMultipleLinksMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayMultipleLinksMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(2);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayPrimaryLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayShortMotorwayLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(
                this.setup.motorwayShortMotorwayLinkMissingJunctionAndDestinationAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayShortTrunkLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayShortTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayTrunkLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void motorwayTrunkLinkWithDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayTrunkLinkWithDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, false, true));
    }

    @Test
    public void primaryMotorwayLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.primaryMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void primaryTrunkLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.primaryTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK2);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void trunkMotorwayLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void trunkMotorwayLinkWithJunctionTest()
    {
        this.verifier.actual(this.setup.trunkMotorwayLinkWithJunctionAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }

    @Test
    public void trunkShortMotorwayLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.trunkShortMotorwayLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void trunkShortTrunkLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.trunkShortTrunkLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void trunkTrunkLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.trunkTrunkLinkMissingJunctionAndDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 2, true, true));
    }

    @Test
    public void unclassifiedPrimaryLinkMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.unclassifiedPrimaryLinkMissingJunctionAndDestinationAtlas(),
                CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void motorwayMotorwayLinkBranchMissingDestinationTest()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkBranchMissingDestinationAtlas(), CHECK);
        this.verifier.verifyExpectedSize(2);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }

    @Test
    public void motorwayMotorwayLinkTwoWayBranchMissingDestinationNoBranchCheckAtlas()
    {
        this.verifier.actual(this.setup.motorwayMotorwayLinkTwoWayBranchMissingDestinationAtlas(),
                new SignPostCheck(ConfigurationResolver
                        .inlineConfiguration("{\"SignPostCheck.link.branch.check\":false}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }

    @Test
    public void motorwayMotorwayLinkTwoWayBranchMissingDestinationRelationTest()
    {
        this.verifier.actual(
                this.setup.motorwayMotorwayLinkTwoWayBranchMissingDestinationRelationAtlas(),
                CHECK);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 1, true, false));
    }

    @Test
    public void twoWayTrunksUTurnMissingJunctionAndDestinationTest()
    {
        this.verifier.actual(this.setup.twoWayTrunksUTurnMissingJunctionAndDestinationAtlas(),
                new SignPostCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SignPostCheck.linkLength.minimum.meters\":10.0}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verify(flag, 3, true, true));
    }
}
