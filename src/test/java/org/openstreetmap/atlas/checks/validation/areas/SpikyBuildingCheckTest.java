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
    public void findsSpikyBuildings()
    {
        verifier.actual(setup.getSpikyBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyExpectedSize(1);
    }

    @Test
    public void findsSpikyBuildingsRoundNumbers()
    {
        verifier.actual(setup.getRoundNumbersSpiky(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyExpectedSize(1);
    }

    @Test
    public void doesNotFindNormalBuilding()
    {
        verifier.actual(setup.getNormalBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void doesNotFindNormalBuildingRound()
    {
        verifier.actual(setup.getNormalRound(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void badCase()
    {
        verifier.actual(setup.badCase(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void badCase2()
    {
        verifier.actual(setup.badCase2(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void circleBuilding()
    {
        verifier.actual(setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void circleBuildingFlaggedWithMorePoints()
    {
        verifier.actual(setup.circleBuilding(), new SpikyBuildingCheck(ConfigurationResolver
                .inlineConfiguration("{\"SpikyBuildingCheck\":{\"curve.points.minimum\":10}}")));
        verifier.verifyExpectedSize(3);

    }

    @Test
    public void circleBuildingFlaggedWithStricterAngleThreshold()
    {
        verifier.actual(setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"curve.degrees.maximum.single_heading_change\":3.0}}")));
        verifier.verifyExpectedSize(3);
    }

    @Test
    public void circleBuildingFlaggedWithLargerTotalAngleRequirement()
    {
        verifier.actual(setup.circleBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SpikyBuildingCheck\":{\"curve.degrees.minimum.total_heading_change\":179.0}}")));
        verifier.verifyExpectedSize(3);
    }

    @Test
    public void spikyBuildingNotFlaggedSmallThreshold()
    {
        verifier.actual(setup.getSpikyBuilding(), new SpikyBuildingCheck(ConfigurationResolver
                .inlineConfiguration("{\"SpikyBuildingCheck\":{\"spiky.angle.maximum\":0.1}}")));
        verifier.verifyEmpty();
    }

    @Test
    public void smallConsecutiveCurves()
    {
        verifier.actual(setup.twoShortConsecutiveCurvesBuilding(),
                new SpikyBuildingCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.verifyExpectedSize(1);
        verifier.verify(flag -> Assert.assertEquals(2, flag.getPoints().size()));
    }
}
