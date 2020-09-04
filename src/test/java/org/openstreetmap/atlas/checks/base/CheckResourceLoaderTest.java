package org.openstreetmap.atlas.checks.base;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.CheckResourceLoaderTestCheck;
import org.openstreetmap.atlas.checks.base.checks.ContextAwareTestCheck;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test the configuration loading based on default parameters for enabling checks.
 * 
 * @author brian_l_davis
 * @author nachtm
 */
public class CheckResourceLoaderTest
{
    @Test
    public void testCheckCountryDenylist()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"countries.denylist\":[\"ABC\"]}}";

        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(0, checkResourceLoader.loadChecksForCountry("ABC").size());
        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("DEF").size());
    }

    @Test
    public void testCheckCountryPermitlist()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"countries\":[\"ABC\"]}}";

        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("ABC").size());
        Assert.assertEquals(0, checkResourceLoader.loadChecksForCountry("DEF").size());
    }

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

    @Test
    public void testDenylistCountrySpecific()
    {
        final String configSource = "{\"CheckResourceLoader.checks.denylist\": [\"CheckResourceLoaderTestCheck\"], \"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"override.ABC.enabled\": false}, \"BaseTestCheck\":{\"enabled\": false, \"override.ABC.enabled\": true}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        // ABC contains nothing, since the permitlist and the enabled countries have no overlap
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry("DEF").isEmpty());

        // DEF contains only CheckResourceLoaderTestCheck, since that is the only overlap between
        // enabled countries and the permitlist
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry("ABC").stream()
                .allMatch(check -> check.getCheckName().startsWith("Base")));
    }

    @Test
    public void testDenylistGeneral()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"], \"CheckResourceLoader.checks.denylist\": [\"CheckResourceLoaderTestCheck\"], \"CheckResourceLoaderTestCheck\":{\"enabled\": true}, \"BaseTestCheck\":{\"enabled\": false}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(0, checkResourceLoader.loadChecks().size());
    }

    @Test
    public void testDenylistNoOp()
    {
        // A denylist containing no checks shouldn't impact the behavior
        final String configSource = "{\"CheckResourceLoader.checks.denylist\": [], \"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"override.ABC.enabled\": false}, \"BaseTestCheck\":{\"enabled\": false, \"override.ABC.enabled\": true}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("ABC").size());
        Assert.assertEquals(1,
                checkResourceLoader.loadChecksForCountry("ABC").stream().map(Check::getCheckName)
                        .filter(name -> name.startsWith("Base")).distinct().count());

        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("DEF").size());
        Assert.assertEquals(1,
                checkResourceLoader.loadChecksForCountry("DEF").stream().map(Check::getCheckName)
                        .filter(name -> name.startsWith("CheckResource")).distinct().count());
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

    @Test
    public void testGroupedAndCountryCheckDisabling()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"groups\":{\"alphabetEnds\":[\"ABC\",\"XYZ\"]},\"CheckResourceLoaderTestCheck\":{\"enabled\": true,\"var1\":1,\"var2\":\"Hi\",\"override.DEF.enabled\":false,\"override.alphabetEnds.enabled\":false}}";
        final String country1 = "ABC";
        final String country2 = "DEF";
        final String country3 = "XYZ";
        final String country4 = "JKL";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertTrue(checkResourceLoader.loadChecksForCountry(country1).isEmpty());
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry(country2).isEmpty());
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry(country3).isEmpty());
        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry(country4).size());
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

    @Test
    public void testPermitlistCountrySpecific()
    {
        final String configSource = "{\"CheckResourceLoader.checks.permitlist\": [\"CheckResourceLoaderTestCheck\"], \"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"override.ABC.enabled\": false}, \"BaseTestCheck\":{\"enabled\": false, \"override.ABC.enabled\": true}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        // ABC contains nothing, since the permitlist and the enabled countries have no overlap
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry("ABC").isEmpty());

        // DEF contains only CheckResourceLoaderTestCheck, since that is the only overlap between
        // enabled countries and the permitlist
        Assert.assertTrue(checkResourceLoader.loadChecksForCountry("DEF").stream()
                .allMatch(check -> check.getCheckName().startsWith("CheckResource")));
    }

    @Test
    public void testPermitlistGeneral()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"], \"CheckResourceLoader.checks.permitlist\": [\"CheckResourceLoaderTestCheck\"], \"CheckResourceLoaderTestCheck\":{\"enabled\": true}, \"BaseTestCheck\":{\"enabled\": false}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(1, checkResourceLoader.loadChecks().size());
        Assert.assertTrue(checkResourceLoader.loadChecks().stream()
                .noneMatch(check -> check.getCheckName().startsWith("Base")));
    }

    @Test
    public void testPermitlistNoOp()
    {
        // A permitlist containing all checks shouldn't impact the behavior
        final String configSource = "{\"CheckResourceLoader.checks.permitlist\": [\"CheckResourceLoaderTestCheck\",\"BaseTestCheck\"], \"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"],\"CheckResourceLoaderTestCheck\":{\"enabled\": true, \"override.ABC.enabled\": false}, \"BaseTestCheck\":{\"enabled\": false, \"override.ABC.enabled\": true}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("ABC").size());
        Assert.assertEquals(1,
                checkResourceLoader.loadChecksForCountry("ABC").stream().map(Check::getCheckName)
                        .filter(name -> name.startsWith("Base")).distinct().count());

        Assert.assertEquals(1, checkResourceLoader.loadChecksForCountry("DEF").size());
        Assert.assertEquals(1,
                checkResourceLoader.loadChecksForCountry("DEF").stream().map(Check::getCheckName)
                        .filter(name -> name.startsWith("CheckResource")).distinct().count());
    }

    @Test
    public void testSubclassWithSpecialConstructor()
    {
        final String configSource = "{\"CheckResourceLoader.scanUrls\": [\"org.openstreetmap.atlas.checks.base.checks\"], \"BaseTestCheck\":{\"enabled\":true}, \"ContextAwareTestCheck\":{\"enabled\":true}}";
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(configSource);
        final CheckResourceLoader checkResourceLoader = new CheckResourceLoader(configuration);

        final Class<?>[][] constructorArgumentTypes = new Class<?>[][] {
                { Configuration.class, Integer.TYPE }, { Configuration.class }, {} };
        final Object[][] constructorArguments = new Object[][] { { configuration, 12 },
                { configuration }, {} };

        final Set<Check> checks = checkResourceLoader
                .loadChecksUsingConstructors(constructorArgumentTypes, constructorArguments);
        Assert.assertEquals(2, checks.size());
        checks.forEach(check ->
        {
            if (check instanceof ContextAwareTestCheck)
            {
                Assert.assertEquals(12, ((ContextAwareTestCheck) check).getData());
            }
        });
    }
}
