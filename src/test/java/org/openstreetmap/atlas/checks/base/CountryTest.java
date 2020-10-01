package org.openstreetmap.atlas.checks.base;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.BaseTestCheck;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test the countries.permitlist and countries.denylist functionality. This allows users to filter
 * checks based on countries, by either allowing only countries in the permitlist or not allowing
 * any countries in the denylist
 * 
 * @author cuthbertm
 */
public class CountryTest
{
    /**
     * Test to make sure both the keys "countries" and "countries.permitlist" work for the
     * permitlist option
     */
    @Test
    public void testBackwardsCompatibility()
    {
        final String countryConfig = "{\"BaseTestCheck\":{\"countries\":[]}}";
        this.testConfiguration(countryConfig, "AIA", true);
    }

    /**
     * Two tests: 1. Test that if containing permitlist and denylist, that only countries in the
     * permitlist are used and all countries in the denylist are ignored. 2. Test that if a country
     * is both in the permitlist and the denylist that the country value in the permitlist takes
     * precedence
     */
    @Test
    public void testCombinationCountries()
    {
        final String countryConfig = "{\"BaseTestCheck\":{\"countries.permitlist\":[\"IRN\",\"IRQ\"],\"countries.denylist\":[\"AIA\",\"DOM\"]}}";
        this.testConfiguration(countryConfig, "IRN", true);
        this.testConfiguration(countryConfig, "IRQ", true);
        this.testConfiguration(countryConfig, "AIA", false);
        this.testConfiguration(countryConfig, "DOM", false);
        final String countryConfig2 = "{\"BaseTestCheck\":{\"countries.permitlist\":[\"IRN\"],\"countries.denylist\":[\"IRN\"]}}";
        this.testConfiguration(countryConfig2, "IRN", true);
    }

    /**
     * Test that all countries included in the denylist are ignored, and any countries not included
     * in the denylist are used.
     */
    @Test
    public void testDenylistCountries()
    {
        final String countryConfig = "{\"BaseTestCheck\":{\"countries.denylist\":[\"AIA\",\"DOM\"]}}";
        this.testConfiguration(countryConfig, "AIA", false);
        this.testConfiguration(countryConfig, "DOM", false);
        this.testConfiguration(countryConfig, "IRN", true);
    }

    /**
     * Test that if you supply an empty country permitlist, that the option is essentially ignored
     * and all countries are allowed.
     */
    @Test
    public void testNoCountries()
    {
        final String countryConfig = "{\"BaseTestCheck\":{\"countries.permitlist\":[]}}";
        this.testConfiguration(countryConfig, "AIA", true);
    }

    /**
     * Test that only countries included in the permitlist are used, and any countries not included
     * in the permitlist are ignored.
     */
    @Test
    public void testPermitlistCountries()
    {
        final String countryConfig = "{\"BaseTestCheck\":{\"countries.permitlist\":[\"AIA\",\"DOM\"]}}";
        this.testConfiguration(countryConfig, "AIA", true);
        this.testConfiguration(countryConfig, "DOM", true);
        this.testConfiguration(countryConfig, "IRN", false);
    }

    /**
     * Private function that does the check for all the unit tests
     * 
     * @param config
     *            A stringified version of the configuration that will be resolved using the
     *            {@link ConfigurationResolver}
     * @param testCountry
     *            The country that is being tested
     * @param test
     *            Whether the country being tested should be included or excluded when running the
     *            checks
     */
    private void testConfiguration(final String config, final String testCountry,
            final boolean test)
    {
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(config);
        final BaseTestCheck testCheck = new BaseTestCheck(configuration);

        // If no countries in the configuration than it is assumed that a flag will be produced. The
        // BaseTestCheck will always produce a flag if everything else gets through
        if (test)
        {
            Assert.assertTrue(testCheck.validCheckForCountry(testCountry));
        }
        else
        {
            Assert.assertFalse(testCheck.validCheckForCountry(testCountry));
        }
    }
}
