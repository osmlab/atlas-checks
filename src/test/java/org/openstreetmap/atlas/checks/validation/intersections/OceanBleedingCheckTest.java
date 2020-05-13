package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link OceanBleedingCheck}
 *
 * @author seancoulter
 */
public class OceanBleedingCheckTest
{
    @Rule
    public OceanBleedingCheckTestRule setup = new OceanBleedingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidBuildingBleedingIntoOcean()
    {
        this.verifier.actual(this.setup.getInvalidBuildingBleedingIntoOcean(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidFloatingStreetInOcean()
    {
        this.verifier.actual(this.setup.getInvalidFloatingStreetInOcean(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidRailwayBleedingIntoOcean()
    {
        this.verifier.actual(this.setup.getInvalidRailwayBleedingIntoOcean(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidStreetBleedingIntoOcean()
    {
        this.verifier.actual(this.setup.getInvalidStreetBleedingIntoOcean(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidStreetCrossingCoastlineArea()
    {
        this.verifier.actual(this.setup.getInvalidStreetCrossingCoastlineArea(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidStreetCrossingCoastlineLine()
    {
        this.verifier.actual(this.setup.getInvalidStreetCrossingCoastlineLine(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void validBridgeOverOceanBoundary()
    {
        this.verifier.actual(this.setup.getValidBridgeOverOceanBoundary(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validBuildingEnclosedByCoastlineIsland()
    {
        this.verifier.actual(this.setup.getValidBuildingEnclosedByCoastlineIsland(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validPathIntoOcean()
    {
        this.verifier.actual(this.setup.getPathBleedingIntoOcean(),
                new OceanBleedingCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"OceanBleedingCheck\": {\"highway.minimum\": \"service\"}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validRailwayBleedingIntoWaterbodyNonOcean()
    {
        this.verifier.actual(this.setup.getValidRailwayBleedingIntoWaterbodyNonOcean(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validStreetConnectedToFerryTerminal()
    {
        this.verifier.actual(this.setup.getValidStreetConnectedToFerryTerminal(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validStreetEnclosedByCoastlineIsland()
    {
        this.verifier.actual(this.setup.getValidStreetEnclosedByCoastlineIsland(),
                new OceanBleedingCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

}
