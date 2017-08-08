package org.openstreetmap.atlas.checks.distributed;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;

/**
 * @author brian_l_davis
 */
public class AtlasFilePathResolverTest
{
    @Test
    public void testSchemaPathing()
    {
        final AtlasFilePathResolver pathResolver = new AtlasFilePathResolver(ConfigurationResolver
                .inlineConfiguration("{\"AtlasFilePathResolver.schema.path.templates\": {"
                        + "\"http\":\"%s/%s\", " + "\"hdfs\":\"%s\"}}"));

        Assert.assertEquals("http://192.0.0.1/path/AIA",
                pathResolver.resolvePath("http://192.0.0.1/path", "AIA"));

        Assert.assertEquals("hdfs://home/user/path",
                pathResolver.resolvePath("hdfs://home/user/path", "AIA"));

        Assert.assertEquals("/path/AIA", pathResolver.resolvePath("/path", "AIA"));
    }
}
