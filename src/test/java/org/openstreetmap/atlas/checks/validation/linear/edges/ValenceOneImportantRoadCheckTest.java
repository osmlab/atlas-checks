package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Tests {@link ValenceOneImportantRoadCheck}
 *
 * @author brian_l_davis
 */
public class ValenceOneImportantRoadCheckTest
{
    @Rule
    public ValenceOneImportantRoadCheckTestRule setup = new ValenceOneImportantRoadCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void deadEndTrunkBoundaryTest()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.deadEndTrunkBoundaryAtlas();
        this.verifier.actual(atlas, check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testConnectedBidirectionalTrunk()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getConnectedBidirectionalTrunk();
        this.verifier.actual(atlas, check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testConnectedClassificationChange()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getClassificationChange();
        this.verifier.actual(atlas, check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testConnectedMotorway()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getConnectedMotorway();
        this.verifier.actual(atlas, check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testConnectedTrunk()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getConnectedTrunk();
        this.verifier.actual(atlas, check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testDeadEndTrunk()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getDeadEndTrunk();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(
                    "Hint: This road may be connected to roads with highway=construction or highway=proposed."));
            Assert.assertTrue(flag.getInstructions().contains(
                    "Hint: This road may be connected to roads with restrictive access tags."));
        });
    }

    @Test
    public void testNoEntryMotorway()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getNoEntryMotorway();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(
                    "Hint: This road may be connected to roads with highway=construction or highway=proposed."));
            Assert.assertTrue(flag.getInstructions().contains(
                    "Hint: This road may be connected to roads with restrictive access tags."));
        });
    }

    @Test
    public void testOpposingOpposingOneWays()
    {
        final ValenceOneImportantRoadCheck check = new ValenceOneImportantRoadCheck(
                ConfigurationResolver.emptyConfiguration());
        final Atlas atlas = this.setup.getOpposingOneWays();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
    }
}
