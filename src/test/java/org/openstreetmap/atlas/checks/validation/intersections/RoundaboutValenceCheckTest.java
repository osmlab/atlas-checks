package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutValenceCheck}
 *
 * @author savannahostrowski
 */
public class RoundaboutValenceCheckTest
{
    @Rule
    public RoundaboutValenceCheckTestRule setup = new RoundaboutValenceCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();


    @Test
    public void masterRoundaboutEdgesWithTurningCircleTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithTurningCircleTagAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void masterRoundaboutEdgesWithTurningLoopTagAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithTurningLoopTagAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void masterRoundaboutEdgeWithValence1NodesAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgeWithValence1NodesAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
    }


    @Test
    public void testMasterRoundaboutEdgesWithDeadEndNodesAtlas()
    {
        this.verifier.actual(this.setup.masterRoundaboutEdgesWithDeadEndNodesAtlas(),
                new RoundaboutValenceCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains(RoundaboutValenceCheck.WRONG_VALENCE_INSTRUCTIONS)));
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

}
