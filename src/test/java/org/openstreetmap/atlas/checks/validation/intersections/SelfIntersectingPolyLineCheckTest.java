package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link SelfIntersectingPolylineCheck} unit test
 *
 * @author mgostintsev
 */
public class SelfIntersectingPolyLineCheckTest
{
    private final SelfIntersectingPolylineCheck check = new SelfIntersectingPolylineCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SelfIntersectingPolylineCheck\":{\"tags.filter\":\"highway->!proposed&highway->!construction&highway->!footway&highway->!path\"}}"));

    @Rule
    public SelfIntersectingPolylineTestCaseRule setup = new SelfIntersectingPolylineTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testValidLineNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidLineNoSelfIntersection(), check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidLineNonShapeSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidLineNonShapeSelfIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidLineShapePointSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidLineShapePointSelfIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidLineGeometryWaterwayTag()
    {
        this.verifier.actual(this.setup.getInvalidLineGeometryWaterwayTag(), check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidLineGeometryHighwayFootwayTag()
    {
        this.verifier.actual(this.setup.getInvalidLineGeometryHighwayFootwayTag(), check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testValidEdgeNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidEdgeNoSelfIntersection(), check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidEdgeNonShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidEdgeNonShapeIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidEdgeShapeIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeGeometryHighwayPrimaryTag()
    {
        this.verifier.actual(this.setup.getInvalidEdgeGeometryHighwayPrimaryTag(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeGeometryBuildingTag()
    {
        this.verifier.actual(this.setup.getInvalidEdgeGeometryBuildingTag(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testValidAreaNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidAreaNoSelfIntersection(), check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidAreaNonShapeSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidAreaNonShapeSelfIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidAreaShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidAreaShapeIntersection(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidAreaBuildTag()
    {
        this.verifier.actual(this.setup.getInvalidAreaBuildingTag(), check);
        this.verifier.verifyExpectedSize(1);
    }
}
