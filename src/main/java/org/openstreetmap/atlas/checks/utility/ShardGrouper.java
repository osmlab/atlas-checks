package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.ShardBucketCollection;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Class for the grouping shards into groups while guaranteeing no more than a set amount are loaded
 * during the groups execution on the executor
 *
 * @author jklamer
 */
public class ShardGrouper extends ShardBucketCollection<Shard, HashSet<Shard>>
{
    private static final long serialVersionUID = 2227293668916525220L;
    private final IncompleteSharding myShards;
    private final int maxShardLoad;
    private final int zoomLevel;
    private final Distance expandDistance;
    private List<ShardGroup> resultingGroups;

    public ShardGrouper(final Iterable<? extends Shard> shards, final int maxShardLoad,
            final Distance expandDistance)
    {
        super(boundsForShards(shards).expand(expandDistance), 0);
        this.myShards = new IncompleteSharding(shards);
        this.zoomLevel = 0;
        this.expandDistance = expandDistance;
        this.maxShardLoad = maxShardLoad;
        shards.forEach(this::add);
    }

    private ShardGrouper(final Iterable<Shard> shards, final int maxShardLoad,
            final Distance expandDistance, final Integer zoomLevel,
            final IncompleteSharding myShards)
    {
        super(boundsForShards(shards).expand(expandDistance), zoomLevel);
        this.myShards = myShards;
        this.zoomLevel = zoomLevel;
        this.expandDistance = expandDistance;
        this.maxShardLoad = maxShardLoad;
        shards.forEach(this::add);
    }

    @Override
    protected boolean allowMultipleBucketInsertion()
    {
        return false;
    }

    @Override
    protected HashSet<Shard> initializeBucketCollection()
    {
        return new HashSet<>();
    }

    @Override
    protected Shard resolveShard(final Shard item, final List<? extends Shard> possibleBuckets)
    {
        final Rectangle shardBounds = item.bounds();
        for (final Shard bucket : possibleBuckets)
        {
            final Rectangle bucketBounds = bucket.bounds();
            if (bucketBounds.fullyGeometricallyEncloses(shardBounds))
            {
                return bucket;
            }
        }
        throw new CoreException("This grouper works with slippy tile shards only");
    }

    public static Rectangle boundsForShards(final Iterable<? extends Shard> shards)
    {
        return Rectangle.forLocated(shards);
    }

    /**
     * Return the shard groupings for this run. All groups should have a unique name
     * 
     * @return shards groups
     */
    public List<ShardGroup> getGroups()
    {
        if (Objects.isNull(this.resultingGroups))
        {
            this.resultingGroups = new ArrayList<>();
            this.getAllShardBucketCollectionPairs().forEach((shard, group) ->
            {
                final Long shardsThatWouldLoad = Iterables.size(
                        this.myShards.shards(boundsForShards(group).expand(this.expandDistance)));
                if (shardsThatWouldLoad.intValue() > this.maxShardLoad)
                {
                    if (group.size() == 1)
                    {
                        throw new CoreException("Unable to ensure that at max {} shards are loaded",
                                this.maxShardLoad);
                    }
                    this.resultingGroups.addAll(new ShardGrouper(group, this.maxShardLoad,
                            this.expandDistance, this.zoomLevel + 1, this.myShards).getGroups());
                }
                else
                {
                    this.resultingGroups.add(new ShardGroup(group, shard.getName()));
                }
            });
        }
        return this.resultingGroups;
    }

    public String printGroups(final String country)
    {
        if (Objects.isNull(this.resultingGroups))
        {
            this.getGroups();
        }
        this.resultingGroups.sort((list1, list2) -> list2.size() - list1.size());
        int groupNumber = 0;
        final StringBuilder builder = new StringBuilder();
        for (final List<Shard> group : this.resultingGroups)
        {
            builder.append(System.lineSeparator());
            builder.append(country);
            builder.append(" Group ");
            builder.append(++groupNumber);
            builder.append(":");
            builder.append(String.join(",",
                    group.stream().map(Shard::getName).collect(Collectors.toList())));
        }
        return builder.toString();
    }

    public List<GeoJsonObject> asGeojsonFeatureCollections()
    {
        if (Objects.isNull(this.resultingGroups))
        {
            this.getGroups();
        }
        final List<GeoJsonObject> groups = new ArrayList<>();
        this.resultingGroups.forEach(group ->
        {
            final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
            for (final Shard shard : group)
            {
                outersToInners.put(shard.bounds(), new ArrayList<>());
            }
            groups.add(new MultiPolygon(outersToInners).asGeoJsonFeatureCollection());
        });
        return groups;
    }

}
