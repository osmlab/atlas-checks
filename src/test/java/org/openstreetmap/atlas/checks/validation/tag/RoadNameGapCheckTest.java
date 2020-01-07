package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link RoadNameGapCheck} unit tests.
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheckTest
{
    @Rule
    public RoadNameGapCheckTestRule setup = new RoadNameGapCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"RoadNameGapCheck\":{\"valid.highway.tag\":[\"primary\",\"tertiary\",\"trunk\",\"motorway\",\"secondary\"]}}");

    @Test
    public void testForEdgeWithDifferentNameTag()
    {
        this.verifier.actual(this.setup.getEdgeWithDifferentNameTag(),
                new RoadNameGapCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testForEdgeWithDifferentNameTagButSameOsmId()
    {
        this.verifier.actual(this.setup.getEdgeWithDifferentNameTagButSameOsmId(),
                new RoadNameGapCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testForEdgeWithNoNameTag()
    {
        this.verifier.actual(this.setup.getEdgeWithNoNameTag(),
                new RoadNameGapCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testForInvalidHighWayTag()
    {
        this.verifier.actual(this.setup.isInvalidHighWayTag(),
                new RoadNameGapCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testForIsJunctionNotRoundAbout()
    {
        this.verifier.actual(this.setup.isJunctionNotRoundAbout(),
                new RoadNameGapCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

}
