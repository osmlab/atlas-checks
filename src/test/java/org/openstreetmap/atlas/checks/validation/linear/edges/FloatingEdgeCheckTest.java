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
    private FloatingEdgeCheck check = new FloatingEdgeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"FloatingEdgeCheck\":{\"length\":{\"maximum.kilometers\":16.093,\"minimum.meters\": 1.0}}}"));
    private FloatingEdgeCheck minimumHighwayCheck = new FloatingEdgeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"FloatingEdgeCheck\":{\"highway.minimum\": \"PRIMARY_LINK\",\"length\":{\"maximum.kilometers\":16.093,\"minimum.meters\": 1.0}}}"));

    @Test
    public void testBidirectionalFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingBidirectionalEdgeAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testConnectedEdge()
    {
        this.verifier.actual(this.setup.connectedEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingEdgeAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testMixedAtlas()
    {
        this.verifier.actual(this.setup.mixedAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSyntheticBorderEdge()
    {
        this.verifier.actual(this.setup.syntheticBorderAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInlineConfigFloatingEdge()
    {
        this.verifier.actual(this.setup.floatingEdgeAtlas(), minimumHighwayCheck);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
