package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author cuthbertm, gpogulsky
 */
public class FloatingEdgeCheckTest
{
    @Rule
    public FloatingEdgeCheckTestRule setup = new FloatingEdgeCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final FloatingEdgeCheck check = new FloatingEdgeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"FloatingEdgeCheck\":{\"length\":{\"maximum.kilometers\":16.093,\"minimum.meters\": 1.0}}}"));
    private final FloatingEdgeCheck minimumHighwayCheck = new FloatingEdgeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"FloatingEdgeCheck\":{\"highway.minimum\": \"PRIMARY_LINK\",\"length\":{\"maximum.kilometers\":16.093,\"minimum.meters\": 1.0}}}"));

    @Test
    public void testAirportIntersectingEdge()
    {
        this.verifier.actual(this.setup.airportAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testBidirectionalFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingBidirectionalEdgeAtlas(), this.check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testConnectedEdge()
    {
        this.verifier.actual(this.setup.connectedEdgeAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingEdgeAtlas(), this.check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInlineConfigFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingEdgeAtlas(), this.minimumHighwayCheck);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testMixedAtlas()
    {
        this.verifier.actual(this.setup.mixedAtlas(), this.check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSyntheticBorderEdge()
    {
        this.verifier.actual(this.setup.syntheticBorderAtlas(), this.check);
        this.verifier.verifyEmpty();
    }
}
