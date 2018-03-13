package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link UnusualLayerTagsCheck} unit test
 *
 * @author mkalender
 */
public class UnusualLayerTagsCheckTest
{
    private static final UnusualLayerTagsCheck check = new UnusualLayerTagsCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public UnusualLayerTagsCheckTestRule setup = new UnusualLayerTagsCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void noEdgeAtlas()
    {
        this.verifier.actual(this.setup.noEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.invalidLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testInvalidJunctionLayerTagEdge()
    {
        this.verifier.actual(this.setup.invalidLayerTagJunctionEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testInvalidLayerTagEdge()
    {
        this.verifier.actual(this.setup.invalidLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testInvalidTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.invalidLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testMinusFiveTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusFiveLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinusFourTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusFourLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinusInfinityBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusInfinityLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testMinusInfinityLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusInfinityLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testMinusInfinityTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusInfinityLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testMinusOneBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusOneLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testMinusOneLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusOneLayerTagEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinusOneTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusOneLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinusSixTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusSixLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testMinusThreeTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusThreeLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinusTwoTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.minusTwoLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMissingBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.missingLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMissingJunctionLayerTagEdge()
    {
        this.verifier.actual(this.setup.missingLayerTagJunctionEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMissingLayerTagEdge()
    {
        this.verifier.actual(this.setup.missingLayerTagEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMissingTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.missingLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testNullBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.nullLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testNullJunctionLayerTagEdge()
    {
        this.verifier.actual(this.setup.nullLayerTagJunctionEdgeAtlas(), check);
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.JUNCTION_INSTRUCTION));
        });
    }

    @Test
    public void testNullLayerTagEdge()
    {
        this.verifier.actual(this.setup.nullLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testNullTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.nullLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testPlusFiveBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusFiveLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPlusFourBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusFourLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPlusInfinityBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusInfinityLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testPlusInfinityLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusInfinityLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testPlusInfinityTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusInfinityLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testPlusOneBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusOneLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPlusOneLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusOneLayerTagEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPlusOneTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusOneLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testPlusSixBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusSixLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testPlusThreeBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusThreeLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPlusTwoBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.plusTwoLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testWhitespaceBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.whitespaceLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testWhitespaceJunctionLayerTagEdge()
    {
        this.verifier.actual(this.setup.whitespaceLayerTagJunctionEdgeAtlas(), check);
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.JUNCTION_INSTRUCTION));
        });
    }

    @Test
    public void testWhitespaceLayerTagEdge()
    {
        this.verifier.actual(this.setup.whitespaceLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testWhitespaceTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.whitespaceLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testZeroBridgeLayerTagEdge()
    {
        this.verifier.actual(this.setup.zeroLayerTagBridgeEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.BRIDGE_INSTRUCTION));
        });
    }

    @Test
    public void testZeroJunctionLayerTagEdge()
    {
        this.verifier.actual(this.setup.zeroLayerTagJunctionEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testZeroLayerTagEdge()
    {
        this.verifier.actual(this.setup.zeroLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains(UnusualLayerTagsCheck.INVALID_LAYER_INSTRUCTION));
        });
    }

    @Test
    public void testZeroTunnelLayerTagEdge()
    {
        this.verifier.actual(this.setup.zeroLayerTagTunnelEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.TUNNEL_INSTRUCTION));
        });
    }

    @Test
    public void testValidLayerTagRoundaboutEdgeAtlas()
    {
        this.verifier.actual(this.setup.validLayerTagRoundaboutEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMissingBridgeTagJunctionLayerEdgeAtlas()
    {
        this.verifier.actual(this.setup.missingBridgeTagJunctionLayerEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.JUNCTION_INSTRUCTION));
        });
    }

    @Test
    public void testInvalidBridgeTunnelTagLayerEdgeAtlas()
    {
        this.verifier.actual(this.setup.whitespaceBridgeRoundaboutLayerTagEdgeAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains(UnusualLayerTagsCheck.JUNCTION_INSTRUCTION));
        });
    }

    @Test
    public void testValidBuildingPassageTunnelLayerEdgeAtlas()
    {
        this.verifier.actual(this.setup.validBuildingPassageTunnelLayerEdgeAtlas(), check);
        this.verifier.verifyEmpty();
    }
}
