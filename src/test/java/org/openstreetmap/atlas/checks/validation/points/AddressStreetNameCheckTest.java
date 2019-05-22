package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit test for {@link AddressStreetNameCheck}
 *
 * @author bbreithaupt
 */
public class AddressStreetNameCheckTest
{
    @Rule
    public AddressStreetNameCheckTestRule setup = new AddressStreetNameCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validAddressStreetTagTest()
    {
        this.verifier.actual(this.setup.validAddressStreetTagAtlas(),
                new AddressStreetNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validAddressStreetLocalizedTagTest()
    {
        this.verifier.actual(this.setup.validAddressStreetLocalizedTagAtlas(),
                new AddressStreetNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidAddressStreetTagTest()
    {
        this.verifier.actual(this.setup.invalidAddressStreetTagAtlas(),
                new AddressStreetNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validAddressStreetTagConfigNoEdgeInRangeTest()
    {
        this.verifier.actual(this.setup.validAddressStreetTagAtlas(),
                new AddressStreetNameCheck(ConfigurationResolver
                        .inlineConfiguration("{\"AddressStreetNameCheck.bounds.size\":1.0}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
