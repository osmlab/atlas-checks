package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cameronfrenette
 */
public class BigNodeBadDataCheckTest
{
    private static final Logger logger = LoggerFactory.getLogger(BigNodeBadDataCheck.class);

    @Rule
    public BigNodeBadDataCheckTestRule setup = new BigNodeBadDataCheckTestRule();

    private final Configuration configLowPathsThreshold = ConfigurationResolver
            .inlineConfiguration("{\"BigNodeBadDataCheck.max.number.paths.threshold\": 3}");
    private final Configuration configLowJunctionsThreshold = ConfigurationResolver
            .inlineConfiguration(
                    "{\"BigNodeBadDataCheck.max.number.junction.edges.threshold\": 3}");
    private final Configuration configLowPathsThresholdWithHighwayFilter = ConfigurationResolver
            .inlineConfiguration("{\"BigNodeBadDataCheck\": {\"max.number.paths.threshold\": 3,"
                    + "\"highway.type\": {\"minimum\": \"tertiary\",\"maximum\": \"motorway\"}}}");
    private final Configuration configNormalThreshold = ConfigurationResolver
            .inlineConfiguration("{\"BigNodeBadDataCheck.max.number.paths.threshold\": 1000}");

    @Test
    public void testBigNodeAtlas()
    {
        Assert.assertTrue(
                this.runTest(this.setup.getBigNodeAtlas(), this.configNormalThreshold, 1));
    }

    @Test
    public void testCheckBasedOnJunctionEdges()
    {
        Assert.assertTrue(this.runTest(this.setup.getAtlas(), this.configLowJunctionsThreshold, 3));
    }

    @Test
    public void testCheckBasedOnPaths()
    {
        Assert.assertTrue(this.runTest(this.setup.getAtlas(), this.configLowPathsThreshold, 6));
    }

    @Test
    public void testConnectedEdgesHighwayFiltering()
    {
        Assert.assertTrue(this.runTest(this.setup.getAtlas(),
                this.configLowPathsThresholdWithHighwayFilter, 4));
    }

    private boolean runTest(final Atlas atlas, final Configuration config,
            final int expectedFlagCount)
    {
        final List<CheckFlag> flags = new ArrayList<>();
        final BigNodeBadDataCheck checker = new BigNodeBadDataCheck(config);
        final BigNodeFinder bigNodeFinder = new BigNodeFinder();
        final Iterable<BigNode> bigNodes = bigNodeFinder.find(atlas);

        bigNodes.forEach(bigNode ->
        {
            final Optional<CheckFlag> checkerFlags = checker.flag(bigNode);
            checkerFlags.map(flags::add);
        });

        logger.info("flags.size: {}, expectedFlagCount: {}", flags.size(), expectedFlagCount);
        return flags.size() == expectedFlagCount;
    }

}
