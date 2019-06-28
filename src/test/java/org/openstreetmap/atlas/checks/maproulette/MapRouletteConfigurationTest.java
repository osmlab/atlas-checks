package org.openstreetmap.atlas.checks.maproulette;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * Unit tests for MapRouletteConfiguration
 *
 * @author nachtm
 * @author bbreithaupt
 */
public class MapRouletteConfigurationTest
{
    private static final String SERVER = "server";
    private static final String SCHEME = "http";
    private static final String SCHEME2 = "https";
    private static final int PORT = 123;
    private static final String PROJECT_NAME = "project";
    private static final String API_KEY = "key";
    private static final String CONFIG = "server:123:project:key";
    private static final String CONFIG2 = "https://server:123:project:key";
    private static final String BAD_CONFIG = "server:123:project";

    @Test
    public void testBadParse()
    {
        try
        {
            final MapRouletteConfiguration configuration = MapRouletteConfiguration
                    .parse(BAD_CONFIG);
        }
        catch (final Exception exception)
        {
            if (!(exception instanceof CoreException))
            {
                Assert.fail();
            }
        }
    }

    @Test
    public void testFullURLParse()
    {
        final MapRouletteConfiguration configuration = MapRouletteConfiguration.parse(CONFIG2);
        Assert.assertNotNull(configuration);
        Assert.assertEquals(SCHEME2, configuration.getScheme());
        Assert.assertEquals(SERVER, configuration.getServer());
        Assert.assertEquals(PORT, configuration.getPort());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectName());
        Assert.assertEquals(API_KEY, configuration.getApiKey());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectConfiguration().getName());
    }

    @Test
    public void testParse()
    {
        final MapRouletteConfiguration configuration = MapRouletteConfiguration.parse(CONFIG);
        Assert.assertNotNull(configuration);
        Assert.assertEquals(SCHEME, configuration.getScheme());
        Assert.assertEquals(SERVER, configuration.getServer());
        Assert.assertEquals(PORT, configuration.getPort());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectName());
        Assert.assertEquals(API_KEY, configuration.getApiKey());
        Assert.assertEquals(PROJECT_NAME, configuration.getProjectConfiguration().getName());
    }
}
