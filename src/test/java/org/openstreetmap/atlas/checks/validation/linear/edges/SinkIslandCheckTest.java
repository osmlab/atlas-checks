package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author matthieun
 * @author gpogulsky
 */
public class SinkIslandCheckTest
{
    @Rule
    public SinkIslandCheckTestRule setup = new SinkIslandCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testSingleEdgeAtlas()
    {
        this.verifier.actual(this.setup.getSingleEdgeAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSinkDetection()
    {
        this.verifier.actual(this.setup.getTestAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSingleEdgeWithAmenity()
    {
        this.verifier.actual(this.setup.getSingleEdgeWithAmenityAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
