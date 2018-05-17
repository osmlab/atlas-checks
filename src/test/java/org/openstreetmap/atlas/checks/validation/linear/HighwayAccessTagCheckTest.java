package org.openstreetmap.atlas.checks.validation.linear;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link HighwayAccessTagCheck}
 *
 * @author bbreithaupt
 */

public class HighwayAccessTagCheckTest
{
    // For now we will assume that there is a class called MyUnitTestRule with a test atlas inside
    // called "testAtlas"
    @Rule
    public HighwayAccessTagCheckTestRule setup = new HighwayAccessTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    // In Highway Tests
    @Test
    public void accessNoInHighwayEdges()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdges(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayLines()
    {
        this.verifier.actual(this.setup.accessNoInHighwayLines(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgeLineEdge()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgeLineEdge(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayLineEdgeLine()
    {
        this.verifier.actual(this.setup.accessNoInHighwayLineEdgeLine(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesTrack()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesTrack(),
                new HighwayAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayAccessTagCheck\":{\"minimum.highway.type\":\"residential\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesSameFeature()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesSameFeature(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesSameFeatureSquare()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesSameFeatureSquare(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesLanduseMilitary()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesLanduseMilitary(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesLanduseMilitaryRelation()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesLanduseMilitaryRelation(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesInRelation()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesInRelation(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    // config tests
    @Test
    public void accessNoInHighwayEdgesVehicleNo()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesVehicleNo(),
                new HighwayAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayAccessTagCheck\":{\"tags.filter\":\"vehicle->!no\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesPublicTransportYes()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesPublicTransportYes(),
                new HighwayAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayAccessTagCheck\":{\"tags.filter\":\"public_transport->!yes\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
