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

    // Start Highway Tests
    @Test
    public void accessStartInHighwayEdges()
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

    // private In Highway Tests
    @Test
    public void accessPrivateInHighwayEdges()
    {
        this.verifier.actual(this.setup.accessPrivateInHighwayEdges(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void accessprivateInHighwayLines()
    {
        this.verifier.actual(this.setup.accessPrivateInHighwayLines(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    // Private In Highway With Gates Tests
    @Test
    public void accessPrivateGateInHighwayEdges()
    {
        this.verifier.actual(this.setup.accessPrivateGateInHighwayEdges(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void accessPrivateGateInHighwayLines()
    {
        this.verifier.actual(this.setup.accessPrivateGateInHighwayLines(),
                new HighwayAccessTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
