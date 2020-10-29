package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
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

    private static void verifyObjectsAndSuggestions(final CheckFlag flag, final int objectCount,
            final int fixCount)
    {
        Assert.assertEquals(objectCount, flag.getFlaggedObjects().size());
        Assert.assertEquals(fixCount, flag.getFixSuggestions().size());
        final List<Long> objectIds = flag.getFlaggedObjects().stream()
                .filter(object -> object.getProperties().containsKey("identifier"))
                .map(object -> Long.valueOf(object.getProperties().get("identifier")))
                .collect(Collectors.toList());
        flag.getFixSuggestions().forEach(
                suggestion -> Assert.assertTrue(objectIds.contains(suggestion.getIdentifier())));
    }

    @Test
    public void bridgeLayersTest()
    {
        this.verifier.actual(this.setup.getBridgeLayers(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(3);
    }

    @Test
    public void ignoreConstructionTest()
    {
        this.verifier.actual(this.setup.getIgnoreConstruction(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void invalidIntersectionNoHighwayTest()
    {
        this.verifier.actual(this.setup.getInvalidIntersectionNoHighway(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1, 1));
    }

    @Test
    public void invalidIntersectionNoRailwayTest()
    {
        this.verifier.actual(this.setup.getInvalidIntersectionNoRailway(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1, 1));
    }

    @Test
    public void invalidObjectWithTagTest()
    {
        this.verifier.actual(this.setup.getInvalidObjectsWithTag(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(5);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1, 1));
    }

    @Test
    public void nodeAtIntersectionTest()
    {
        this.verifier.actual(this.setup.getNoIntersectionNode(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void validIntersectionLayerConfigDefaultTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionLayers(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver
                        .inlineConfiguration("{  \"LevelCrossingOnRailwayCheck\": {"
                                + "    \"enabled\": true," + "    \"layer.default\": 1" + " }}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 2, 0));
    }

    @Test
    public void validIntersectionLayerTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionLayers(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void validIntersectionLayerZeroTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionLayerZero(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void validIntersectionNoLayerConfigRailFilterTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionNoLayer(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LevelCrossingOnRailwayCheck\": {" + "    \"enabled\": true,"
                                + "    \"railway.filter\": \"railway->light_rail\"" + " }}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1, 1));
    }

    @Test
    public void validIntersectionNoLayerTest()
    {
        this.verifier.actual(this.setup.getValidIntersectionNoLayer(),
                new LevelCrossingOnRailwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(0);
    }

}
