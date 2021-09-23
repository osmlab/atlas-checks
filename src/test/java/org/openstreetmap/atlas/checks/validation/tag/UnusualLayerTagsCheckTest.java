package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link UnusualLayerTagsCheck} unit test
 *
 * @author mkalender, bbreithaupt, v-naydinyan
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
    public void falsePositiveHighwayNotOnGroundWithBridge()
    {
        this.verifier.actual(this.setup.getFalsePositiveHighwayNotOnGroundWithBridge(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveHighwayNotOnGroundWithCovered()
    {
        this.verifier.actual(this.setup.getFalsePositiveHighwayNotOnGroundWithCovered(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveHighwayNotOnGroundWithHighwaySteps()
    {
        this.verifier.actual(this.setup.getFalsePositiveHighwayNotOnGroundWithHighwaySteps(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveHighwayNotOnGroundWithServiceParkingAisle()
    {
        this.verifier.actual(this.setup.getFalsePositiveHighwayNotOnGroundWithServiceParkingAisle(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveHighwayNotOnGroundWithTunnel()
    {
        this.verifier.actual(this.setup.getFalsePositiveHighwayNotOnGroundWithTunnel(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLandUseNotOnGroundWithBridge()
    {
        this.verifier.actual(this.setup.getFalsePositiveLandUseNotOnGroundWithBridge(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLandUseNotOnGroundWithCovered()
    {
        this.verifier.actual(this.setup.getFalsePositiveLandUseNotOnGroundWithCovered(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveLandUseNotOnGroundWithTunnel()
    {
        this.verifier.actual(this.setup.getFalsePositiveLandUseNotOnGroundWithTunnel(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveNaturalNotOnGroundWithBridge()
    {
        this.verifier.actual(this.setup.getFalsePositiveNaturalNotOnGroundWithBridge(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveNaturalNotOnGroundWithCovered()
    {
        this.verifier.actual(this.setup.getFalsePositiveNaturalNotOnGroundWithCovered(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveNaturalNotOnGroundWithTunnel()
    {
        this.verifier.actual(this.setup.getFalsePositiveNaturalNotOnGroundWithTunnel(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveWaterwayNotOnGroundWithBridge()
    {
        this.verifier.actual(this.setup.getFalsePositiveWaterwayNotOnGroundWithBridge(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveWaterwayNotOnGroundWithCovered()
    {
        this.verifier.actual(this.setup.getFalsePositiveWaterwayNotOnGroundWithCovered(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveWaterwayNotOnGroundWithLocationUnderground()
    {
        this.verifier.actual(
                this.setup.getFalsePositiveWaterwayNotOnGroundWithLocationUnderground(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveWaterwayNotOnGroundWithTunnel()
    {
        this.verifier.actual(this.setup.getFalsePositiveWaterwayNotOnGroundWithTunnel(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void truePositiveBadLayerValueBridge()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueBridge(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueBridgeAboveRange()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueBridgeAboveRange(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueBridgeBelowRange()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueBridgeBelowRange(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueBridgeWithTunnel()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueBridgeWithTunnel(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueTunnel()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueTunnel(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueTunnelAboveRange()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueTunnelAboveRange(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueTunnelBelowRange()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueTunnelBelowRange(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveBadLayerValueTunnelWithBridge()
    {
        this.verifier.actual(this.setup.getTruePositiveBadLayerValueTunnelWithBridge(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveHighwayNotOnGround()
    {
        this.verifier.actual(this.setup.getTruePositiveHighwayNotOnGround(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveHighwayNotOnGroundWithBridgeNo()
    {
        this.verifier.actual(this.setup.getTruePositiveHighwayNotOnGroundWithBridgeNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveHighwayNotOnGroundWithTunnelNo()
    {
        this.verifier.actual(this.setup.getTruePositiveHighwayNotOnGroundWithTunnelNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLandUseNotOnGround()
    {
        this.verifier.actual(this.setup.getTruePositiveLandUseNotOnGround(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLandUseNotOnGroundWithBridgeNo()
    {
        this.verifier.actual(this.setup.getTruePositiveLandUseNotOnGroundWithBridgeNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLandUseNotOnGroundWithTunnelNo()
    {
        this.verifier.actual(this.setup.getTruePositiveLandUseNotOnGroundWithTunnelNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveLayerTagIsZero()
    {
        this.verifier.actual(this.setup.getTruePositiveLayerTagIsZero(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveNaturalNotOnGround()
    {
        this.verifier.actual(this.setup.getTruePositiveNaturalNotOnGround(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveNaturalNotOnGroundWithBridgeNo()
    {
        this.verifier.actual(this.setup.getTruePositiveNaturalNotOnGroundWithBridgeNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveNaturalNotOnGroundWithTunnelNo()
    {
        this.verifier.actual(this.setup.getTruePositiveNaturalNotOnGroundWithTunnelNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveWaterwayNotOnGround()
    {
        this.verifier.actual(this.setup.getTruePositiveWaterwayNotOnGround(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveWaterwayNotOnGroundWithBridgeNo()
    {
        this.verifier.actual(this.setup.getTruePositiveWaterwayNotOnGroundWithBridgeNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveWaterwayNotOnGroundWithTunnelNo()
    {
        this.verifier.actual(this.setup.getTruePositiveWaterwayNotOnGroundWithTunnelNo(),
                this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
