package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link SelfIntersectingPolylineCheck} unit test
 *
 * @author mgostintsev
 * @author sayas01
 */
public class SelfIntersectingPolyLineCheckTest
{
    @Rule
    public SelfIntersectingPolylineTestCaseRule setup = new SelfIntersectingPolylineTestCaseRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final SelfIntersectingPolylineCheck check = new SelfIntersectingPolylineCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SelfIntersectingPolylineCheck\":{\"tags.filter\":\"highway->!proposed&highway->!construction&highway->!footway&highway->!path\"}}"));
    private final SelfIntersectingPolylineCheck minimumHighwayCheck = new SelfIntersectingPolylineCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SelfIntersectingPolylineCheck\":{\"minimum.highway.type\":\"service\"}}"));

    @Test
    public void testHighPriorityEdgeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidEdgeShapeIntersection(),
                this.minimumHighwayCheck);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidAreaBuildTag()
    {
        this.verifier.actual(this.setup.getInvalidAreaBuildingTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidAreaNonShapeSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidAreaNonShapeSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidAreaShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidAreaShapeIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeGeometryBuildingTag()
    {
        this.verifier.actual(this.setup.getInvalidEdgeGeometryBuildingTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeGeometryHighwayPrimaryTag()
    {
        this.verifier.actual(this.setup.getInvalidEdgeGeometryHighwayPrimaryTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeNonShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidEdgeNonShapeIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidEdgeShapeIntersection()
    {
        this.verifier.actual(this.setup.getInvalidEdgeShapeIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidLineGeometryHighwayFootwayTag()
    {
        this.verifier.actual(this.setup.getInvalidLineGeometryHighwayFootwayTag(), this.check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidLineGeometryWaterwayTag()
    {
        this.verifier.actual(this.setup.getInvalidLineGeometryWaterwayTag(), this.check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testInvalidLineNonShapeSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidLineNonShapeSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testInvalidLineShapePointSelfIntersection()
    {
        this.verifier.actual(this.setup.getInvalidLineShapePointSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testLowPriorityEdgeIntersection()
    {
        this.verifier.actual(this.setup.getLowPriorityInvalidEdgeIntersection(),
                this.minimumHighwayCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidAreaNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidAreaNoSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testValidEdgeNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidEdgeNoSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(0);
    }

    @Test
    public void testValidLineNoSelfIntersection()
    {
        this.verifier.actual(this.setup.getValidLineNoSelfIntersection(), this.check);
        this.verifier.verifyExpectedSize(0);
    }
}
