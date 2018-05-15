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
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
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

    // Start Highway Tests
    @Test
    public void accessNoStartHighwayEdges()
    {
        this.verifier.actual(this.setup.accessNoStartHighwayEdges(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoStartHighwayLines()
    {
        this.verifier.actual(this.setup.accessNoStartHighwayLines(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoStartHighwayEdgeLineEdge()
    {
        this.verifier.actual(this.setup.accessNoStartHighwayEdgeLineEdge(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoStartHighwayLineEdgeLine()
    {
        this.verifier.actual(this.setup.accessNoStartHighwayLineEdgeLine(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesNonHighwayConnected()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesNonHighwayConnected(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoStartHighwayEdgesSameFeature()
    {
        this.verifier.actual(this.setup.accessNoStartHighwayEdgesSameFeature(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    // config tests
    @Test
    public void accessNoInHighwayEdgesVehicleNo()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesVehicleNo(),
                new HighwayAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayAccessTagCheck\":{\"doNotFlag.value\":{\"no\":{\"keys\":[\"vehicle\"]}}}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessNoInHighwayEdgesPublicTransportYes()
    {
        this.verifier.actual(this.setup.accessNoInHighwayEdgesPublicTransportYes(),
                new HighwayAccessTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayAccessTagCheck\":{\"doNotFlag.value\":{\"yes\":{\"keys\":[\"public_transport\"]}}}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
