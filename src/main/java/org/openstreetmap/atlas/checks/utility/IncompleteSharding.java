package org.openstreetmap.atlas.checks.utility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * A sharding interface with just the shards that are available for a certain run.
 *
 * @author jklamer
 */
public class IncompleteSharding implements Sharding
{
    private static final long serialVersionUID = 7177712260432658102L;
    private final RTree<? extends Shard> shards;
    private final Set<Shard> shardSet = new HashSet<>();

    public IncompleteSharding(final Iterable<? extends Shard> shards)
    {
        this.shards = RTree.forLocated(shards);
        shards.forEach(this.shardSet::add);
    }

    @Override
    public Iterable<Shard> neighbors(final Shard shard)
    {
        if (this.shardSet.contains(shard))
        {
            return this.shards.get(shard.bounds().expand(Distance.ONE_METER)).stream()
                    .filter(aShard -> !aShard.equals(shard)).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Shard shardForName(final String name)
    {
        final SlippyTile result = SlippyTile.forName(name);

        if (!this.shardSet.contains(result)){
            throw new CoreException("This sharding does not include tile {}", name);
        }

        return result;
    }

    @Override
    public Iterable<Shard> shards(final GeometricSurface surface)
    {
        return this.shards.get(surface.bounds()).stream()
                .filter(shard -> surface.overlaps(shard.bounds())).collect(Collectors.toList());
    }

    @Override
    public Iterable<Shard> shardsCovering(final Location location)
    {
        return this.shards.get(location.bounds()).stream()
                .filter(shard -> shard.bounds().fullyGeometricallyEncloses(location))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Shard> shardsIntersecting(final PolyLine polyLine)
    {
        return this.shards.get(polyLine.bounds()).stream()
                .filter(shard -> shard.bounds().overlaps(polyLine)).collect(Collectors.toList());
    }
}
