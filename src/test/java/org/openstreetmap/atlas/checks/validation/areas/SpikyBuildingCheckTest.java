package org.openstreetmap.atlas.checks.validation.areas;

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

    // We should ignore edges with only three vertices
    @Test
    public void ignoresSpikyButSmall()
    {
        verifier.actual(setup.getSpikyButSmall(),
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
}
