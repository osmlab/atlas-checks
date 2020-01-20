package org.openstreetmap.atlas.checks.validation.points;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;

/**
 * Test to check whether it picks up the duplicate nodes correctly
 *
 * @author cuthbertm
 */
public class DuplicateNodeCheckTest
{
    @Test
    public void testCheck()
    {
        final Rectangle bounds = Rectangle.TEST_RECTANGLE;
        final Set<Location> nodes = new HashSet<>();
        while (nodes.size() < 10)
        {
            nodes.add(Location.random(bounds));
        }

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        int identifier = 0;
        for (final Location node : nodes)
        {
            // for first node make it a duplicate
            if (identifier == 0)
            {
                builder.addNode(identifier++, node, RandomTagsSupplier.randomTags(5));
            }
            builder.addNode(identifier++, node, RandomTagsSupplier.randomTags(5));
        }

        final Iterator<Location> locationIterator = nodes.iterator();
        final Location start = locationIterator.next();

        identifier = 0;
        while (locationIterator.hasNext())
        {
            if (identifier == 0)
            {
                builder.addEdge(identifier++, new Segment(start, locationIterator.next()),
                        RandomTagsSupplier.randomTags(5));
            }
            builder.addEdge(identifier++, new Segment(start, locationIterator.next()),
                    RandomTagsSupplier.randomTags(5));
        }

        final Atlas atlas = builder.get();

        final Iterable<CheckFlag> flags = new DuplicateNodeCheck(
                ConfigurationResolver.emptyConfiguration()).flags(atlas);

        Assert.assertEquals(2, Iterables.size(flags));
    }
}
