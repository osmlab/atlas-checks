package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link OverlappingAOIPolygonCheck}
 *
 * @author bbreithaupt
 */
public class OverlappingAOIPolygonCheckTest
{
    @Rule
    public OverlappingAOIPolygonCheckTestRule setup = new OverlappingAOIPolygonCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"OverlappingAOIPolygonCheck\":{\"aoi.tags.filter\": [\"amenity->GRAVE_YARD|landuse->CEMETERY\"],\"intersect.minimum.limit\":0.01}}");

    @Test
    public void sameAOIsNoOverlap()
    {
        this.verifier.actual(this.setup.sameAOIsNoOverlap(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void sameAOIs()
    {
        this.verifier.actual(this.setup.sameAOIs(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void similarAOIs()
    {
        this.verifier.actual(this.setup.similarAOIs(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void differentAOIs()
    {
        this.verifier.actual(this.setup.differentAOIs(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
