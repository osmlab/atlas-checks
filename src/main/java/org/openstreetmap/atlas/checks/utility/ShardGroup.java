package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;

/**
 * Wrapper classs for list of shards making up a group for processing
 *
 * @author jklamer
 */
public class ShardGroup extends ArrayList<Shard>
{
    private final String name;

    /**
     * Returns the lowest common parent name of all the shards
     *
     * @param shards
     *            the shards
     * @return the result of {@link SlippyTile}.getName() on the Lowest common parent of all shards
     */
    public static String nameForSlippyTiles(final Collection<? extends Shard> shards)
    {
        if (shards.size() == 0)
        {
            throw new CoreException("Unable to make name for empty shard collection");
        }
        if (shards.size() == 1)
        {
            return shards.iterator().next().getName();
        }
        try
        {
            final List<SlippyTile> slippyTiles = shards.stream().map(shard -> (SlippyTile) shard)
                    .collect(Collectors.toList());
            SlippyTile parent = slippyTiles.stream()
                    .min(Comparator.comparingInt(SlippyTile::getZoom)).get().parent();
            while (!slippyTiles.stream().map(SlippyTile::bounds)
                    .allMatch(parent.bounds()::fullyGeometricallyEncloses))
            {
                parent = parent.parent();
            }
            return parent.getName();
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException(
                    " Unable to create compressed name for group of shards that aren't slippy tiles");
        }
    }

    public ShardGroup(final Collection<? extends Shard> collection, final String name)
    {
        super(collection);
        this.name = name;
    }

    public ShardGroup(final Collection<? extends Shard> collection)
    {
        this(collection, nameForSlippyTiles(collection));
    }

    public String getName()
    {
        return this.name;
    }
}
