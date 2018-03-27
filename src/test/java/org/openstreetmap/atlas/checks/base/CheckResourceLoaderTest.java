package org.openstreetmap.atlas.checks.base;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.CheckResourceLoaderTestCheck;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test the configuration loading based on default parameters for enabling checks.
 * 
 * @author brian_l_davis
 */
public class CheckResourceLoaderTest
{
    /**
     * Test the check loading using country keyword specific configurations. Assert that each
     * country gets its own version of the check.
     */
    @Test
    public void testCountryKeywordCheckLoading()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true,\"var1\":1,\"override.ABC.var1\":2}}";
        final String country1 = "ABC";
        final String country2 = "XYZ";
        final List<String> countries = Arrays.asList(country1, country2);
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);
        final Set<Check> loadedCountry1Checks = checkResourceLoader.loadChecksForCountry(country1);
        final Set<Check> loadedCountry2Checks = checkResourceLoader.loadChecksForCountry(country2);

        // assert both countries loaded with different check sets
        Assert.assertNotNull(loadedCountry1Checks);
        Assert.assertNotNull(loadedCountry2Checks);
        Assert.assertEquals(1, loadedCountry1Checks.size());
        Assert.assertEquals(1, loadedCountry2Checks.size());
        Assert.assertFalse(Iterables.equals(loadedCountry1Checks, loadedCountry2Checks));

        // assert both configurations overrode properly and thus initialized properly
        final long country1Var1 = checkResourceLoader.getConfigurationForCountry(country1)
                .get("CheckResourceLoaderTestCheck.var1").value();
        final long country2Var2 = checkResourceLoader.getConfigurationForCountry(country2)
                .get("CheckResourceLoaderTestCheck.var1").value();
        Assert.assertEquals(country1Var1, 2);
        Assert.assertEquals(country2Var2, 1);
    }

    /**
     * Test to make sure that if the default behavior (set in configuration) is that checks are
     * disabled, that no checks are loaded when not setting any explicitly
     */
    @Test
    public void testDisabledByDefault()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"]}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);

        final Set<Check> loaded = new CheckResourceLoader(configuration).loadChecks();
        Assert.assertTrue(loaded.isEmpty());
    }

    /**
     * Test to make sure that by explicitly setting the enabled function for a check that it is in
     * actual fact enabled.
     */
    @Test
    public void testEnabledByConfiguration()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck.enabled\": true}";

        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final Set<Check> loaded = new CheckResourceLoader(configuration).loadChecks();
        Assert.assertEquals(1, loaded.size());
    }

    /**
     * Test to make sure that if the default behaviour (set in configuration) is that checks are
     * enabled, that the checks are loaded when not setting any explicitly.
     */
    @Test
    public void testEnabledByPredicate()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"]}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);

        final Set<Check> loaded = new CheckResourceLoader(configuration)
                .loadChecks(CheckResourceLoaderTestCheck.class::equals);
        Assert.assertEquals(1, loaded.size());
    }

    /**
     * Test Grouped country overrides
     */
    @Test
    public void testGroupedCountryCheckLoading()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"groups\":{\"beginningAlphabet\":[\"ABC\",\"DEF\"],\"alphabetEnds\":[\"ABC\",\"XYZ\"]},\"CheckResourceLoaderTestCheck\":{\"enabled\": true,\"var1\":1,\"var2\":\"Hi\",\"override.beginningAlphabet.var1\":2,\"override.alphabetEnds.var2\":\"Bye\"}}";
        final String country1 = "ABC";
        final String country2 = "DEF";
        final String country3 = "XYZ";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        // assert first group override worked for all 3 countries
        final long country1Var1 = checkResourceLoader.getConfigurationForCountry(country1)
                .get("CheckResourceLoaderTestCheck.var1").value();
        final long country2Var1 = checkResourceLoader.getConfigurationForCountry(country2)
                .get("CheckResourceLoaderTestCheck.var1").value();
        final long country3Var1 = checkResourceLoader.getConfigurationForCountry(country3)
                .get("CheckResourceLoaderTestCheck.var1").value();

        Assert.assertEquals(2, country1Var1);
        Assert.assertEquals(2, country2Var1);
        Assert.assertEquals(1, country3Var1);

        // assert second group override worked for all 3 countries
        final String country1Var2 = checkResourceLoader.getConfigurationForCountry(country1)
                .get("CheckResourceLoaderTestCheck.var2").value();
        final String country2Var2 = checkResourceLoader.getConfigurationForCountry(country2)
                .get("CheckResourceLoaderTestCheck.var2").value();
        final String country3Var2 = checkResourceLoader.getConfigurationForCountry(country3)
                .get("CheckResourceLoaderTestCheck.var2").value();

        Assert.assertEquals("Bye", country1Var2);
        Assert.assertEquals("Hi", country2Var2);
        Assert.assertEquals("Bye", country3Var2);
    }
}
