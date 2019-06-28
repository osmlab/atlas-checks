package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link InvalidAccessTagCheck}
 *
 * @author bbreithaupt
 */

public class InvalidAccessTagCheckTest
{
    @Rule
    public InvalidAccessTagCheckTestRule setup = new InvalidAccessTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void accessNoInHighwayEdgeLineEdge()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgeLineEdge(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    // In Highway Tests
    @Test
    public void accessNoInHighwayEdges()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdges(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesInRelation()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesInRelation(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesLanduseMilitary()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesLanduseMilitary(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesLanduseMilitaryRelation()
    {
        this.verifier.actual(this.setup.getAccessNoInHighwayEdgesLanduseMilitaryRelation(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesPublicTransportYes()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesPublicTransportYes(),
                new InvalidAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidAccessTagCheck\":{\"tags.filter\":\"public_transport->!yes\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesSameFeature()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesSameFeature(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesSameFeatureSquare()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesSameFeatureSquare(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesTrack()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesTrack(),
                new InvalidAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidAccessTagCheck\":{\"minimum.highway.type\":\"residential\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    // Config Tests
    @Test
    public void accessNoInHighwayEdgesVehicleNo()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesVehicleNo(),
                new InvalidAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidAccessTagCheck\":{\"tags.filter\":\"vehicle->!no\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayLineEdgeLine()
    {
        this.verifier.actual(this.setup.accessNoInHighwayLineEdgeLine(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessNoInHighwayLines()
    {
        this.verifier.actual(this.setup.accessNoInHighwayLines(),
                new InvalidAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
