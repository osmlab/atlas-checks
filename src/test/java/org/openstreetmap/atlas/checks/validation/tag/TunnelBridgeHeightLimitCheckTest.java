package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.collect.Iterables;

/**
 * Test cases for {@link TunnelBridgeHeightLimitCheck}
 *
 * @author ladwlo
 */
public class TunnelBridgeHeightLimitCheckTest
{

    @Rule
    public TunnelBridgeHeightLimitCheckTestRule setup = new TunnelBridgeHeightLimitCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver
            .inlineConfiguration("{\"TunnelBridgeHeightLimitCheck\":{}}");

    @Test
    public void bidirectionalTunnelWithoutMaxHeightIsFlaggedOnce()
    {
        this.verifier.actual(this.setup.getBidirectionalTunnelWithoutMaxHeight(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void bridgeWithoutCrossingRoadsIsIgnored()
    {
        this.verifier.actual(this.setup.getBridgeWithNoCrossingRoads(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void coveredRoadWithoutMaxHeightSplitIntoTwoEdgesIsFlaggedOnce()
    {
        this.verifier.actual(this.setup.getCoveredRoadWithoutMaxHeightSplitIntoTwoEdges(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void lowClassTunnelWithoutMaxHeightIsIgnored()
    {
        this.verifier.actual(this.setup.getLowClassTunnelWithoutMaxHeight(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void roadWithoutMaxHeightPassingUnderBridgeIsFlagged()
    {
        this.verifier.actual(this.setup.getRoadsPassingUnderBridge(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            // task is created for the expected bridge object
            Assert.assertEquals("1000000001", flags.get(0).getIdentifier());
            // only one edge is flagged
            Assert.assertEquals(1, flags.get(0).getFlaggedObjects().size());
            // the flagged edge has the expected OSM ID
            Assert.assertEquals("4000", Iterables.getOnlyElement(flags.get(0).getFlaggedObjects())
                    .getProperties().get("osmIdentifier"));
        });
    }

    @Test
    public void tunnelAndCoveredRoadWithMaxHeightAreIgnored()
    {
        this.verifier.actual(this.setup.getTunnelAndCoveredRoadWithMaxHeight(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void uncoveredRoadWithoutMaxHeightIsIgnored()
    {
        this.verifier.actual(this.setup.getUncoveredRoadWithoutMaxHeight(),
                new TunnelBridgeHeightLimitCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
