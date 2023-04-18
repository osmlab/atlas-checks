package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;

/**
 * Improper and Unknown Road Name check unit test
 *
 * @author mgostintsev
 */
public class ImproperAndUnknownRoadNameCheckTest
{
    @Rule
    public ImproperAndUnknownRoadNameCheckTestRule setup = new ImproperAndUnknownRoadNameCheckTestRule();

    @Test
    public void testAgainstFalsePositives()
    {
        for (final CheckFlag flag : new ImproperAndUnknownRoadNameCheck(
                ConfigurationResolver.emptyConfiguration()).flags(this.setup.testAtlas()))
        {
            final String identifier = flag.getIdentifier();
            Assert.assertTrue(!this.setup.validEdgeIdentifiers().contains(identifier));
        }
    }

    @Test
    public void testConfigInvalid()
    {
        int flagCount = 0;
        for (final CheckFlag flag : new ImproperAndUnknownRoadNameCheck(
                ConfigurationResolver.inlineConfiguration(
                        "{\"ImproperAndUnknownRoadNameCheck\": {\"names.improper\":[\"ConfigTest\"]}}"))
                .flags(this.setup.configAtlas()))
        {
            flagCount++;
        }
        Assert.assertEquals(1, flagCount);
    }

    @Test
    public void testConfigValid()
    {
        int flagCount = 0;
        for (final CheckFlag flag : new ImproperAndUnknownRoadNameCheck(
                ConfigurationResolver.emptyConfiguration()).flags(this.setup.configAtlas()))
        {
            flagCount++;
        }
        Assert.assertEquals(0, flagCount);
    }

    @Test
    public void testInvalidEdgeIdentifier()
    {
        int flagCount = 0;
        for (final CheckFlag flag : new ImproperAndUnknownRoadNameCheck(
                ConfigurationResolver.emptyConfiguration())
                .flags(this.setup.inValidEdgeIdentifier()))
        {
            flagCount++;
        }
        Assert.assertEquals(0, flagCount);
    }

    @Test
    public void testInvalidNames()
    {
        int flagCount = 0;
        for (final CheckFlag flag : new ImproperAndUnknownRoadNameCheck(
                ConfigurationResolver.emptyConfiguration()).flags(this.setup.testAtlas()))
        {
            final String identifier = flag.getIdentifier();
            Assert.assertTrue(this.setup.improperAndUnknownEdgeIdentifiers().contains(identifier));
            flagCount++;
        }
        Assert.assertEquals(9, flagCount);
    }
}
