package org.openstreetmap.atlas.checks.base;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.CheckResourceLoaderTestCheck;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test the configuration loading based on default parameters for enabling checks.
 * 
 * @author brian_l_davis
 */
public class CheckResourceLoaderTest
{
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
}
