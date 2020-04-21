package org.openstreetmap.atlas.checks.flag;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.utility.tags.SyntheticHighlightPointTag;
import org.openstreetmap.atlas.geography.Location;

/**
 * Tests for {@link FlaggedPoint}
 *
 * @author danielbaah
 */
public class FlaggedPointTest
{
    @Test
    public void flaggedPointSyntheticTagTest()
    {
        final FlaggedPoint flaggedPoint = new FlaggedPoint(Location.EIFFEL_TOWER);
        final Map<String, String> flaggedPointProperties = flaggedPoint.getProperties();

        Assert.assertFalse(flaggedPointProperties.isEmpty());
        Assert.assertTrue(flaggedPointProperties.containsKey(SyntheticHighlightPointTag.KEY));
        Assert.assertEquals("yes", flaggedPointProperties.get(SyntheticHighlightPointTag.KEY));
    }
}
