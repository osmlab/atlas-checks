package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheckTest
{
    
    @Rule
    public BoundaryIntersectionCheckTestRule setup = new BoundaryIntersectionCheckTestRule();
    
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();
    
    @Test
    public void testInvalidThreeCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectOneOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }
    
    @Test
    public void testInvalidTwoCrossingBoundariesWithOnlyWayTags()
    {
        this.verifier.actual(this.setup.crossingBoundariesWithOnlyTagsOnWays(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(6, flag.getFlaggedObjects().size());
            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
        });
    }
    
    @Test
    public void testInvalidTwoCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectEachOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(6, flag.getFlaggedObjects().size());
            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
        });
    }
    
    @Test
    public void testInvalidTwoCrossingItemsWithEdgesAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectEachOtherWithEdges(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(6, flag.getFlaggedObjects().size());
            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
        });
    }
    
    @Test
    public void testTouchingObjects()
    {
        this.verifier.actual(this.setup.boundariesTouchEachOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidCrossingObjectsOneMissingBoundarySpecificTag()
    {
        this.verifier.actual(this.setup.crossingOneMissingBoundarySpecificTag(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidCrossingObjectsOneMissingType()
    {
        this.verifier.actual(this.setup.crossingOneWithWrongType(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidCrossingObjectsWithDifferentTypes()
    {
        this.verifier.actual(this.setup.crossingBoundariesWithDifferentTypes(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidNonCrossingObjects()
    {
        this.verifier.actual(this.setup.nonCrossingBoundariesTwoSeparate(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidNonCrossingObjectsOneContainOther()
    {
        this.verifier.actual(this.setup.nonCrossingOneContainOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
    @Test
    public void testValidNonCrossingObjectsWithEdges()
    {
        this.verifier.actual(this.setup.nonCrossingBoundariesTwoSeparateWithEdges(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
    
}
