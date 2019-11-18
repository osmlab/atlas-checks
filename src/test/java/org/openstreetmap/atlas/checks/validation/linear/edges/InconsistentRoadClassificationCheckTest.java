package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.checks.flag.FlaggedPolyline;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link InconsistentRoadClassificationCheck} tests
 *
 * @author mgostintsev
 * @author mkalender
 * @author micahnacht
 */
public class InconsistentRoadClassificationCheckTest
{
    @Rule
    public InconsistentRoadClassificationCheckTestRule setup = new InconsistentRoadClassificationCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private Configuration highLongEdgeThreshold = ConfigurationResolver.inlineConfiguration(
            "{\"InconsistentRoadClassificationCheck\": {\"long.edge.threshold\": 99999.0}}");

    @Test
    public void testInconsistentButBypassed()
    {
        this.verifier.actual(this.setup.inconsistentButBypassed(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInconsistentButLong()
    {
        this.verifier.actual(this.setup.inconsistentButLong(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInconsistentButLongAndLink()
    {
        this.verifier.actual(this.setup.inconsistentButLongAndLink(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInconsistentButOneMatchingEdgeLong()
    {
        this.verifier.actual(this.setup.inconsistentButOneMatchingEdgeLong(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInconsistentButOneMatchingEdgeShort()
    {
        this.verifier.actual(this.setup.inconsistentButOneMatchingEdgeShort(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getPolyLines().size()));
    }

    @Test
    public void testInconsistentButTwoMatchingEdges()
    {
        this.verifier.actual(this.setup.inconsistentButTwoMatchingEdges(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInconsistentDifferentLinkLevels()
    {
        this.verifier.actual(this.setup.inconsistentDifferentLinkLevels(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInconsistentEdgesButDifferentDirectionAtlas()
    {
        this.verifier.actual(this.setup.inconsistentEdgesButDifferentDirectionAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testInconsistentEdgesWithSimilarDirectionAtlas()
    {
        this.verifier.actual(this.setup.inconsistentEdgesWithSimilarDirectionAtlas(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
            Assert.assertEquals(2, flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("secondary"))
                    .count());
            Assert.assertEquals(1,
                    flaggedObjects.stream().filter(object -> object instanceof FlaggedPolyline)
                            .filter(object -> object.getProperties().get("highway").equals("trunk"))
                            .count());
        });
    }

    @Test
    public void testInconsistentEdgesWithSimilarDirectionAtlasButRoadsMerge()
    {
        this.verifier.actual(this.setup.inconsistentEdgesWithSimilarDirectionAtlasButMerge(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testInconsistentEdgesWithSimilarDirectionWithLinksAtlas()
    {
        this.verifier.actual(this.setup.inconsistentEdgesWithSimilarDirectionWithLinksAtlas(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
            Assert.assertEquals(1, flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("primary"))
                    .count());
            Assert.assertEquals(1,flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("primary_link"))
                    .count());
            Assert.assertEquals(1,
                    flaggedObjects.stream().filter(object -> object instanceof FlaggedPolyline)
                            .filter(object -> object.getProperties().get("highway").equals("trunk"))
                            .count());
        });
    }

    @Test
    public void testInconsistentFourEdgesWithSimilarDirectionAtlas()
    {
        this.verifier.actual(this.setup.inconsistentFourEdgesWithSimilarDirectionAtlas(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
            Assert.assertEquals(2,flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("secondary"))
                    .count());
            Assert.assertEquals(1,
                    flaggedObjects.stream().filter(object -> object instanceof FlaggedPolyline)
                            .filter(object -> object.getProperties().get("highway").equals("trunk"))
                            .count());
            Assert.assertEquals(1,flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline).filter(object -> object
                            .getProperties().get("highway").equals("secondary_link"))
                    .count());
        });
    }

    @Test
    public void testInconsistentLongLessImportantEdge()
    {
        this.verifier.actual(this.setup.inconsistentLongLessImportantEdge(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testLoopsBack()
    {
        this.verifier.actual(this.setup.loopsBack(), new InconsistentRoadClassificationCheck(
                ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testLoopsBackLink()
    {
        this.verifier.actual(this.setup.loopsBackLink(), new InconsistentRoadClassificationCheck(
                ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testOutEdgesGreaterThanTwo()
    {
        this.verifier.actual(this.setup.outEdgesGreaterThanTwo(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testOverlappingInconsistentEdgeAtlas()
    {
        this.verifier.actual(this.setup.overlappingInconsistentEdge(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testSingleEdgeAtlas()
    {
        this.verifier.actual(this.setup.singleEdgeAtlas(), new InconsistentRoadClassificationCheck(
                ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentButLinkTypeAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentButLinkTypeAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentButLowImportantTypesAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentButLowImportantTypesAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentButRoundaboutAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentButRoundaboutAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentButSimilarTypesAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentButSimilarTypesAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentTypesAtTheEndAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentTypesAtTheEndAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testThreeEdgesInconsistentTypesAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentTypesAtlas(),
                new InconsistentRoadClassificationCheck(this.highLongEdgeThreshold));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            final Set<FlaggedObject> flaggedObjects = flag.getFlaggedObjects();
            Assert.assertEquals(2,flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("tertiary"))
                    .count());
            Assert.assertEquals(1,flaggedObjects.stream()
                    .filter(object -> object instanceof FlaggedPolyline)
                    .filter(object -> object.getProperties().get("highway").equals("secondary"))
                    .count());
        });
    }

    @Test
    public void testThreeEdgesInconsistentTypesWithLinkAtlas()
    {
        this.verifier.actual(this.setup.threeEdgesInconsistentTypesWithLinkAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testTwoEdgesAtlas()
    {
        this.verifier.actual(this.setup.twoEdgesAtlas(), new InconsistentRoadClassificationCheck(
                ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }

    @Test
    public void testTwoEdgesInconsistentTypesAtlas()
    {
        this.verifier.actual(this.setup.twoEdgesInconsistentTypesAtlas(),
                new InconsistentRoadClassificationCheck(
                        ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertTrue(flags.isEmpty()));
    }
}
