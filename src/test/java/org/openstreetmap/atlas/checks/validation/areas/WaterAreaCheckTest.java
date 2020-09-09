package org.openstreetmap.atlas.checks.validation.areas;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for WaterAreaCheck
 *
 * @author Taylor Smock
 */
public class WaterAreaCheckTest
{
    @Rule
    public WaterAreaCheckTestRule setup = new WaterAreaCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * This was a test for a false-false positive. The false positive occurred due to the age of the
     * Belize pbf, where a waterway was drawn in April 2019 was not present.
     */
    @Test
    public void testBrazilRiverFalsePositive()
    {
        this.verifier.actual(this.setup.getBrazilRiverFalsePositive(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMangoCreekAndUnnamedWaterwayBad()
    {
        this.verifier.actual(this.setup.getMangoCreekAtlasBad(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(f -> assertTrue(f.getFlaggedObjects().parallelStream()
                .filter(i -> i.getProperties().containsKey("identifier"))
                .allMatch(i -> Arrays.asList("601378260", "265672061")
                        .contains(i.getProperties().get("identifier")))));
    }

    @Test
    public void testMangoCreekAndUnnamedWaterwayGood()
    {
        this.verifier.actual(this.setup.getMangoCreekAtlasGood(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMangoCreekAndUnnamedWaterwayMissingWaterwayLineBad()
    {
        this.verifier.actual(this.setup.getMangoCreekAtlasBadWaterway(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(f -> assertTrue(f.getFlaggedObjects().parallelStream()
                .allMatch(p -> "601378260".equals(p.getProperties().get("identifier")))));
    }

    @Test
    public void testMopanRiverFalsePositive()
    {
        this.verifier.actual(this.setup.getMopanRiverFalsePositive(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOverlappingPonds()
    {
        this.verifier.actual(this.setup.getOverlappingPonds(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testPondAndPier()
    {
        this.verifier.actual(this.setup.getPondAndPier(),
                new WaterAreaCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
