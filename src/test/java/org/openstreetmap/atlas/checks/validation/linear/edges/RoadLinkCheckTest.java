package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;

/**
 * @author cuthbertm
 */
public class RoadLinkCheckTest
{
    @Test
    public void checkTest()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        final Map<String, String> edgeTags = new HashMap<>();
        edgeTags.put("highway", "primary");
        final Map<String, String> invalidLinkTags = new HashMap<>();
        invalidLinkTags.put("highway", "tertiary_link");
        final Map<String, String> correctLinkTags = new HashMap<>();
        correctLinkTags.put("highway", "primary_link");

        // add nodes
        builder.addNode(0, Location.TEST_6, tags);
        builder.addNode(1, Location.TEST_1, tags);
        builder.addNode(2, Location.TEST_7, tags);
        builder.addNode(3, Location.COLOSSEUM, tags);

        // Add edges - one testing distance, the second testing class
        builder.addEdge(1, new Segment(Location.TEST_6, Location.TEST_1), invalidLinkTags);
        builder.addEdge(2, new Segment(Location.TEST_1, Location.TEST_7), edgeTags);
        builder.addEdge(3, new Segment(Location.TEST_7, Location.COLOSSEUM), correctLinkTags);

        final Atlas atlas = builder.get();

        final Iterable<CheckFlag> flags = new RoadLinkCheck(ConfigurationResolver
                .resourceConfiguration("RoadLinkCheckTest.json", this.getClass())).flags(atlas);
        Assert.assertEquals(1, Iterables.size(flags));
    }
}
