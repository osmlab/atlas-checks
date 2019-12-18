package org.openstreetmap.atlas.checks.validation.points;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author mgostintsev
 */
public class DuplicateLocationInPolyLineCheckTest
{
    @Test
    public void testCheck()
    {
        final AtlasBuilder builder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<String, String>();
        final Location location1 = Location.TEST_6;
        final Location location2 = Location.TEST_2;
        final Location location3 = Location.TEST_1;
        final Location location4 = Location.TEST_6;
        final List<Location> locations = new ArrayList<Location>();
        locations.add(location1);
        locations.add(location2);
        locations.add(location3);
        locations.add(location4);

        final PolyLine polyLine = new PolyLine(locations);
        builder.addNode(1, location1, tags);
        builder.addNode(2, location2, tags);
        builder.addNode(3, location3, tags);
        builder.addNode(4, location4, tags);

        builder.addEdge(1, polyLine, tags);

        final Atlas atlas = builder.get();

        final List<CheckFlag> flags = Iterables.asList(
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration())
                        .flags(atlas));
        Assert.assertEquals(1, flags.size());
    }
}
