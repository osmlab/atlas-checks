package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.tags.AddressStreetTag;

/**
 * Tests for {@link AbbreviatedAddressStreetCheck}.
 *
 * @author vlemberg
 */

public class AbbreviatedAddressStreetCheckTest
{
    @Rule
    public AbbreviatedAddressStreetCheckTestRule setup = new AbbreviatedAddressStreetCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testInvalidRoadTypeNumericStreet()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypeNumericStreet(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag ->
        {
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(1, fixSuggestion.getTags().size());
            Assert.assertEquals("1St Street", fixSuggestion.getTags().get(AddressStreetTag.KEY));
        });
    }

    @Test
    public void testInvalidRoadTypePoint()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypePoint(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypePoint2()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypePoint2(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypeSuffix()
    {
        // check when road type is followed by directional suffix e.g. N, S, E, W, NE, SE, SW, NW
        this.verifier.actual(this.setup.getInvalidRoadTypeSuffix(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidRoadTypeWay()
    {
        this.verifier.actual(this.setup.getInvalidRoadTypeWay(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testValidRoadType()
    {
        this.verifier.actual(this.setup.getValidRoadType(),
                new AbbreviatedAddressStreetCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
