package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link PedestrianAreaOverlappingEdgeCheck}
 *
 * @author sayas01
 */
public class PedestrianAreaOverlappingEdgeCheckTest
{
    @Rule
    public PedestrianAreaOverlappingEdgeCheckTestRule setup = new PedestrianAreaOverlappingEdgeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testCorrectlySnappedEdge()
    {
        this.verifier.actual(this.setup.getCorrectlySnappedAtlas(),
                new PedestrianAreaOverlappingEdgeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testEdgesWithDifferentElevation()
    {
        this.verifier.actual(this.setup.getEdgesWithDifferentElevation(),
                new PedestrianAreaOverlappingEdgeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInCorrectlySnappedEdge()
    {
        this.verifier.actual(this.setup.getInCorrectlySnappedAtlas(),
                new PedestrianAreaOverlappingEdgeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testIntersectingEdgeEndingInsideArea()
    {
        this.verifier.actual(this.setup.getIntersectingEdgeEndingInsideArea(),
                new PedestrianAreaOverlappingEdgeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }
}
