package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;

/**
 * Test for {@link ShardGrouper}
 *
 * @author jklamer
 */
public class ShardGrouperTest
{
    private final Random random = new Random();

    @Test(expected = CoreException.class)
    public void testMaxLoad()
    {
        new ShardGrouper(SlippyTile.allTiles(1), 1, Distance.ONE_METER).getGroups();
    }

    @Test
    public void testRandomShards()
    {
        final Queue<SlippyTile> shards = new LinkedList<>();
        SlippyTile.allTiles(3).forEach(shards::offer);
        final Rectangle slippyTileMaxLeft = Rectangle.forCorners(
                new Location(Latitude.degrees(-85.0511288), Longitude.MINIMUM),
                new Location(Latitude.degrees(85.0511288), Longitude.ZERO));
        final Rectangle slippyTileMaxRight = Rectangle.forCorners(
                new Location(Latitude.degrees(-85.0511288), Longitude.ZERO),
                new Location(Latitude.degrees(85.0511288), Longitude.dm7(Longitude.MAXIMUM_DM7)));
        final AtomicLong atomicLong = new AtomicLong();
        this.random.ints(64, 6, 10).forEach(zoomLevel ->
        {
            final SlippyTile original = shards.poll();
            SlippyTile.allTiles(zoomLevel, original.bounds().contract(Distance.ONE_METER))
                    .forEach(smallerShard ->
                    {
                        shards.offer(smallerShard);
                        atomicLong.incrementAndGet();
                    });
        });

        final List<ShardGroup> groups = new ShardGrouper(shards, 60, Distance.TEN_MILES)
                .getGroups();
        Assert.assertEquals(groups.size(),
                groups.stream().map(ShardGroup::getName).distinct().count());
        Assert.assertEquals(atomicLong.get(), groups.stream().mapToInt(ArrayList::size).sum());
        System.out.println(Rectangle.MAXIMUM.surface());
        System.out.println(groups.stream().flatMap(ArrayList::stream).map(Shard::bounds)
                .map(Rectangle::surface).reduce(Surface::add).get().asMeterSquared());
        Assert.assertEquals(
                slippyTileMaxLeft.surface().add(slippyTileMaxRight.surface()).asMeterSquared(),
                groups.stream().flatMap(ArrayList::stream).map(Shard::bounds)
                        .map(Rectangle::surface).reduce(Surface::add).get().asMeterSquared(),
                0.0);
    }
}
