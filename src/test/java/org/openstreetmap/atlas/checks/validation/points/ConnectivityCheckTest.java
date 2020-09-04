package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link ConnectivityCheck}
 *
 * @author bbreithaupt
 */
public class ConnectivityCheckTest
{
    @Rule
    public ConnectivityCheckTestRule setup = new ConnectivityCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration denylistedHighwayFilterConfig = ConfigurationResolver
            .inlineConfiguration(
                    "{\"ConnectivityCheck\":{\"denylisted.highway.filter\":\"highway->secondary\"}}");

    @Test
    public void highwayFilterOnInvalidDisconnectedEdgeCrossingTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedEdgesAtlas(),
                new ConnectivityCheck(this.denylistedHighwayFilterConfig));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void highwayFilterOnInvalidDisconnectedNodeOnEdgeTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodeOnEdgeAtlas(),
                new ConnectivityCheck(this.denylistedHighwayFilterConfig));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void highwayFilterOnInvalidDisconnectedNodesTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesAtlas(),
                new ConnectivityCheck(this.denylistedHighwayFilterConfig));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidConnectedNodesTest()
    {
        this.verifier.actual(this.setup.invalidConnectedNodesAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedEdgeCrossingTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedEdgeCrossingAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedEdgesTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedEdgesAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedNodeOnEdgeTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodeOnEdgeAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedNodesCrossingLayerTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesCrossingLayerAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedNodesCrossingTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesCrossingAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedNodesOppositeTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesOppositeAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidDisconnectedNodesTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validConnectedEdges3Test()
    {
        this.verifier.actual(this.setup.validConnectedEdges3Atlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validConnectedEdgesOppositeTest()
    {
        this.verifier.actual(this.setup.validConnectedEdgesOppositeAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validConnectedNodesAndSnapLocationsTest()
    {
        this.verifier.actual(this.setup.validConnectedNodesAndSnapLocationsAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validConnectedNodesLinearTest()
    {
        this.verifier.actual(this.setup.validConnectedNodesLinearAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validConnectedNodesTest()
    {
        this.verifier.actual(this.setup.validConnectedNodesAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgeCrossingBarrierTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgeCrossingBarrierAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgeCrossingBridgeTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgeCrossingBridgeAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgeCrossingLayerTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgeCrossingLayerAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgeCrossingPedestrianTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgeCrossingPedestrianAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgeCrossingTunnelTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgeCrossingTunnelAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgesBarrierTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgesBarrierAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedEdgesNoExitTest()
    {
        this.verifier.actual(this.setup.validDisconnectedEdgesNoExitAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesCrossingBridgeTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesCrossingBridgeAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesCrossingLayerTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesCrossingLayerAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesCrossingPedestrianTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesCrossingPedestrianAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesCrossingTunnelTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesCrossingTunnelAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesLayerTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesLayerAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validDisconnectedNodesLevelTest()
    {
        this.verifier.actual(this.setup.validDisconnectedNodesLevelAtlas(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validSyntheticNodeCheckTest()
    {
        this.verifier.actual(this.setup.validSyntheticNodeCheck(),
                new ConnectivityCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
