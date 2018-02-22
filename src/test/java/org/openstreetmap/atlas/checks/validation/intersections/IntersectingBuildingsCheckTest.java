package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link IntersectingBuildingsCheck} unit test
 *
 * @author mkalender
 */
public class IntersectingBuildingsCheckTest
{
    private static final IntersectingBuildingsCheck CHECK = new IntersectingBuildingsCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public IntersectingBuildingsTestCaseRule setup = new IntersectingBuildingsTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testDuplicateBuildingsAtlas()
    {
        this.verifier.actual(this.setup.duplicateBuildingsAtlas(), CHECK);
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(2, flag.getFlaggedObjects().size());
            Assert.assertTrue(flag.getInstructions().contains("overlapped by another building"));
        });
    }

    @Test
    public void testNeighborBuildingsAtlas()
    {
        this.verifier.actual(this.setup.neighborBuildingsAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNoIntersectingBuildingAtlas()
    {
        this.verifier.actual(this.setup.noIntersectingBuildingAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSeveralDuplicateBuildingsAtlas()
    {
        this.verifier.actual(this.setup.severalDuplicateBuildingsAtlas(), CHECK);
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(3);
        this.verifier.globallyVerify(flags ->
        {
            // First building overlaps with all other buildings
            final CheckFlag firstFlag = flags.get(0);
            Assert.assertEquals(4, firstFlag.getFlaggedObjects().size());

            // Second building overlaps with the third and fourth one (it's overlap with first is
            // already flagged)
            final CheckFlag secondFlag = flags.get(1);
            Assert.assertEquals(3, secondFlag.getFlaggedObjects().size());

            // Third building's overlap with fourth building will be flagged with the third flag
            final CheckFlag thirdFlag = flags.get(2);
            Assert.assertEquals(2, thirdFlag.getFlaggedObjects().size());
        });
    }

    @Test
    public void testSmallIntersectionAreasAtlas()
    {
        this.verifier.actual(this.setup.smallIntersectionAreasAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSmallIntersectionBuildingsAtlas()
    {
        this.verifier.actual(this.setup.smallIntersectionBuildingsAtlas(), CHECK);
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(2, flag.getFlaggedObjects().size());
            Assert.assertTrue(flag.getInstructions().contains("intersects with another building"));
        });
    }

    @Test
    public void testSmallIntersectionBuildingsAtlasWithIncreasedIntersectionLowerLimit()
    {
        this.verifier.actual(this.setup.smallIntersectionBuildingsAtlas(),
                new IntersectingBuildingsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"IntersectingBuildingsCheck\": {\"intersection.lower.limit\": 0.15}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSmallIntersectionBuildingsAtlasWithSmallOverlapLowerLimit()
    {
        this.verifier.actual(this.setup.smallIntersectionBuildingsAtlas(),
                new IntersectingBuildingsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"IntersectingBuildingsCheck\": {\"intersection.lower.limit\": 0.01, \"overlap.lower.limit\": 0.10}}")));
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(2, flag.getFlaggedObjects().size());
            Assert.assertTrue(flag.getInstructions().contains("overlapped by another building"));
        });
    }
}
