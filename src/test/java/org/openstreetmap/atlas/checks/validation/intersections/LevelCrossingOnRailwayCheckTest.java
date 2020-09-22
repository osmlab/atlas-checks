package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link LevelCrossingOnRailwayCheck}.
 *
 * @author atiannicelli
 */
public class LevelCrossingOnRailwayCheckTest
{
    @Rule
    public LevelCrossingOnRailwayCheckTestRule setup = new LevelCrossingOnRailwayCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void bridgeLayersTest()
    {
        this.verifier.actual(this.setup.getBridgeLayers(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(3, flags.size()));
    }

    @Test
    public void invalidObjectWithTagTest()
    {
        this.verifier.actual(this.setup.invalidObjectsWithTag(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(5, flags.size()));
    }

    @Test
    public void nodeAtIntersectionTest()
    {
        this.verifier.actual(this.setup.noIntersectionNode(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void validIntersectionLayersTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionsLayers(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validIntersectionNoLayerTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionsNoLayer(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
