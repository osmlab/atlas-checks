package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for SpikyBuildingCheck
 *
 * @author nachtm
 */
public class SpikyBuildingCheckTest
{
    @Rule
    public SpikyBuildingCheckTestRule setup = new SpikyBuildingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void badCase()
    {
        this.verifier.actual(this.setup.badCase(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void badCase2()
    {
        this.verifier.actual(this.setup.badCase2(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void circleBuilding()
    {
        this.verifier.actual(this.setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void circleBuildingFlaggedWithLargerTotalAngleRequirement()
    {
        this.verifier.actual(this.setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"curve.degrees.minimum.total_heading_change\":179.0}}")));
        this.verifier.verifyExpectedSize(3);
    }

    @Test
    public void circleBuildingFlaggedWithMorePoints()
    {
        this.verifier.actual(this.setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"curve.points.minimum\":10}}")));
        this.verifier.verifyExpectedSize(3);

    }

    @Test
    public void circleBuildingFlaggedWithStricterAngleThreshold()
    {
        this.verifier.actual(this.setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"curve.degrees.maximum.single_heading_change\":3.0}}")));
        this.verifier.verifyExpectedSize(3);
    }

    @Test
    public void doesNotFindNormalBuilding()
    {
        this.verifier.actual(this.setup.getNormalBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void doesNotFindNormalBuildingRound()
    {
        this.verifier.actual(this.setup.getNormalRound(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void findsSpikyBuildings()
    {
        this.verifier.actual(this.setup.getSpikyBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void findsSpikyBuildingsRoundNumbers()
    {
        this.verifier.actual(this.setup.getRoundNumbersSpiky(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void smallConsecutiveCurves()
    {
        this.verifier.actual(this.setup.twoShortConsecutiveCurvesBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPoints().size()));
    }

    @Test
    public void spikyBuildingNotFlaggedSmallThreshold()
    {
        this.verifier.actual(this.setup.getSpikyBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"spiky.angle.maximum\":0.1}}")));
        this.verifier.verifyEmpty();
    }
}
