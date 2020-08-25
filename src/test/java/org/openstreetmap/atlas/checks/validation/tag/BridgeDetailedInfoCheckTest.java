package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link BridgeDetailedInfoCheck}
 *
 * @author ladwlo
 */
public class BridgeDetailedInfoCheckTest
{

    @Rule
    public BridgeDetailedInfoCheckTestRule setup = new BridgeDetailedInfoCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"BridgeDetailedInfoCheck\":{\"bridge.length.minimum.meters\":500.0}}");

    @Test
    public void longEdgeThatIsNotABridgeIsIgnored()
    {
        this.verifier.actual(this.setup.getLongEdgeThatIsNotABridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void longGenericMajorHighwayBridgeIsFlagged()
    {
        this.verifier.actual(this.setup.longGenericMajorHighwayBridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void longGenericMinorHighwayBridgeIsIgnored()
    {
        this.verifier.actual(this.setup.longGenericMinorHighwayBridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void longGenericNonRailwayOrHighwayBridgeIsIgnored()
    {
        this.verifier.actual(this.setup.longGenericBridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void longGenericRailwayBridgeIsFlagged()
    {
        this.verifier.actual(this.setup.longGenericRailwayBridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void longRailwayBridgeWithStructureIsAccepted()
    {
        this.verifier.actual(this.setup.longRailwayBridgeWithStructure(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void longRailwayBridgeWithTypeIsAccepted()
    {
        this.verifier.actual(this.setup.longRailwayBridgeWithType(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void masterAndReversedEdgesAreOnlyFlaggedOnce()
    {
        this.verifier.actual(this.setup.masterAndReversedEdges(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void shortGenericHighwayBridgeIsIgnored()
    {
        this.verifier.actual(this.setup.shortGenericHighwayBridge(),
                new BridgeDetailedInfoCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
