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
            "{\"OverlappingAOIPolygonCheck\":{\"aoi.tags.filters\": [\"amenity->GRAVE_YARD|landuse->CEMETERY\"],\"intersect.minimum.limit\":0.01}}");

    @Test
    public void sameAOIsNoOverlapTest()
    {
        this.verifier.actual(this.setup.sameAOIsNoOverlapAtlas(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void sameAOIsTest()
    {
        this.verifier.actual(this.setup.sameAOIsAtlas(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void similarAOIsTest()
    {
        this.verifier.actual(this.setup.similarAOIsAtlas(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void differentAOIsTest()
    {
        this.verifier.actual(this.setup.differentAOIsAtlas(),
                new OverlappingAOIPolygonCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
