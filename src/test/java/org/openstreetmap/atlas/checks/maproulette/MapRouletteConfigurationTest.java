package org.openstreetmap.atlas.checks.maproulette;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for MapRouletteConfiguration
 *
 * @author nachtm
 */
public class MapRouletteConfigurationTest
{
    private static final String SERVER = "server";
    private static final int PORT = 123;
    private static final String PROJECT_NAME = "project";
    private static final String API_KEY = "key";
    private static final String CONFIG = "server:123:project:key";
    private static final String BAD_CONFIG = "server:123:project";

    @Test
    public void testParse()
    {
        final MapRouletteConfiguration configuration = MapRouletteConfiguration.parse(CONFIG);
        Assert.assertNotNull(configuration);
        Assert.assertEquals(SERVER, configuration.getServer());
        Assert.assertEquals(PORT, configuration.getPort());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectName());
        Assert.assertEquals(API_KEY, configuration.getApiKey());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectConfiguration().getName());
    }

    @Test
    public void testBadParse()
    {
        final MapRouletteConfiguration configuration = MapRouletteConfiguration.parse(BAD_CONFIG);
        Assert.assertNull(configuration);
    }
}
