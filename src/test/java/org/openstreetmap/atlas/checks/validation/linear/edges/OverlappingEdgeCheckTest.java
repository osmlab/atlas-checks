package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * @author brian_l_davis
 * @author sayana_saithu
 * @author bbreithaupt
 */
public class OverlappingEdgeCheckTest
{
    private static final OverlappingEdgeCheck check = new OverlappingEdgeCheck(
            ConfigurationResolver.emptyConfiguration());

    private static final OverlappingEdgeCheck checkPedestrianTrue = new OverlappingEdgeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"OverlappingEdgeCheck\": {\"highway.priority.minimum\":\"residential\",\"pedestrian.areas.filter\":false}}"));

    @Rule
    public OverlappingEdgeCheckTestRule setup = new OverlappingEdgeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static void verifyFlaggedGeometry(final CheckFlag check, final Atlas atlas)
    {
        Assert.assertEquals(atlas.numberOfEdges() - 1, check.getPolyLines().size());
    }

    @Test
    public void testDifferentLevelOverlappingEdgesAtlas()
    {
        this.verifier.actual(this.setup.getDifferentLevelOverlappingEdgesAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMultipleOverlappingAtlas()
    {
        final Atlas atlas = this.setup.getMultipleOverlappingAtlas();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(check -> verifyFlaggedGeometry(check, atlas));
    }

    @Test
    public void testNonOverlappingAtlas()
    {
        this.verifier.actual(this.setup.getNonOverlappingAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOverlappingHeadAtlas()
    {
        final Atlas atlas = this.setup.getOverlappingHeadAtlas();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(
                check -> Assert.assertEquals(atlas.numberOfEdges(), check.getPolyLines().size()));
    }

    @Test
    public void testOverlappingMiddleAtlas()
    {
        final Atlas atlas = this.setup.getOverlappingMiddleAtlas();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(check -> verifyFlaggedGeometry(check, atlas));
    }

    @Test
    public void testOverlappingTailAtlas()
    {
        final Atlas atlas = this.setup.getOverlappingTailAtlas();
        this.verifier.actual(atlas, check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(
                check -> Assert.assertEquals(atlas.numberOfEdges(), check.getPolyLines().size()));
    }

    @Test
    public void testPedestrianAreaOverlapEdgeAtlas()
    {
        this.verifier.actual(this.setup.getPedestrianAreaOverlapEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPedestrianAreaOverlapEdgeConfigAtlas()
    {
        this.verifier.actual(this.setup.getPedestrianAreaOverlapEdgeAtlas(), checkPedestrianTrue);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testPedestrianAreaOverlapPedestrianAreaAtlas()
    {
        this.verifier.actual(this.setup.getPedestrianAreaOverlapPedestrianAreaAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPedestrianAreaOverlapPedestrianAreaClosedWayAtlas()
    {
        this.verifier.actual(this.setup.getPedestrianAreaOverlapPedestrianAreaClosedWayAtlas(),
                check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPedestrianAreaOverlapPedestrianEdgeAtlas()
    {
        this.verifier.actual(this.setup.getPedestrianAreaOverlapPedestrianEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPierOverlapPierAtlas()
    {
        this.verifier.actual(this.setup.getPierOverlapPierAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSameLevelOverlappingEdgesAtlas()
    {
        this.verifier.actual(this.setup.getSameLevelOverlappingEdgesAtlas(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testServiceAreaOverlapEdgeAtlas()
    {
        this.verifier.actual(this.setup.getServiceAreaOverlapEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testServiceAreaOverlapEdgeConfigAtlas()
    {
        this.verifier.actual(this.setup.getServiceAreaOverlapEdgeAtlas(), checkPedestrianTrue);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testServiceAreaOverlapServiceAreaAtlas()
    {
        this.verifier.actual(this.setup.getServiceAreaOverlapServiceAreaAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSingleEdgeAtlas()
    {
        this.verifier.actual(this.setup.getSingleEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSingleOverlappingWayAtlas()
    {
        this.verifier.actual(this.setup.getSingleOverlappingWayAtlas(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(check -> check.getInstructions().contains("wraps back on itself"));
    }
}
