package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutClosedLoopCheck}
 *
 * @author mkalender
 */
public class RoundaboutClosedLoopCheckTest
{
    @Rule
    public RoundaboutClosedLoopCheckTestRule setup = new RoundaboutClosedLoopCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void masterRoundaboutEdgeWithValence1NodesAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgeWithValence1NodesAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutClosedLoopCheck.MINIMUM_VALENCE_INSTRUCTION)));
    }

    @Test
    public void masterRoundaboutEdgesWithMiniRoundaboutTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithMiniRoundaboutTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void masterRoundaboutEdgesWithTrafficCalmingTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithTrafficCalmingTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void masterRoundaboutEdgesWithTurningCircleTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithTurningCircleTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void masterRoundaboutEdgesWithTurningLoopTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithTurningLoopTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMasterRoundaboutEdgesWithABidirectionalRoadAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithABidirectionalRoadAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(
                flag.getInstructions().contains(RoundaboutClosedLoopCheck.ONE_WAY_INSTRUCTION)));
    }

    @Test
    public void testMasterRoundaboutEdgesWithDeadEndNodesAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithDeadEndNodesAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutClosedLoopCheck.MINIMUM_VALENCE_INSTRUCTION)));
    }

    @Test
    public void testMasterRoundaboutEdgesWithOneMissingOneWayTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithOneMissingOneWayTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMasterRoundaboutEdgesWithValence2NodesAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithValence2NodesAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNonRoundaboutEdgeAtlas()
    {
        this.verifier.actual(this.setup.nonRoundaboutEdgeAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
