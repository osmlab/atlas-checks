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
    public void mainRoundaboutEdgeWithValence1NodesAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgeWithValence1NodesAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutClosedLoopCheck.MINIMUM_VALENCE_INSTRUCTION)));
    }

    @Test
    public void mainRoundaboutEdgesWithMiniRoundaboutTagAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithMiniRoundaboutTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void mainRoundaboutEdgesWithTrafficCalmingTagAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithTrafficCalmingTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void mainRoundaboutEdgesWithTurningCircleTagAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithTurningCircleTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void mainRoundaboutEdgesWithTurningLoopTagAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithTurningLoopTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMainRoundaboutEdgesWithABidirectionalRoadAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithABidirectionalRoadAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(
                flag.getInstructions().contains(RoundaboutClosedLoopCheck.ONE_WAY_INSTRUCTION)));
    }

    @Test
    public void testMainRoundaboutEdgesWithDeadEndNodesAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithDeadEndNodesAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutClosedLoopCheck.MINIMUM_VALENCE_INSTRUCTION)));
    }

    @Test
    public void testMainRoundaboutEdgesWithOneMissingOneWayTagAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithOneMissingOneWayTagAtlas(),
                new RoundaboutClosedLoopCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMainRoundaboutEdgesWithValence2NodesAtlas()
    {
        this.verifier.actual(this.setup.mainRoundaboutEdgesWithValence2NodesAtlas(),
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
