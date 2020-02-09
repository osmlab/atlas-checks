package org.openstreetmap.atlas.checks.distributed;

import java.io.Serializable;
import java.util.List;

import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.event.EventService;
import org.openstreetmap.atlas.geography.sharding.Shard;

/**
 * Meta data holder for sharded flag generation task
 *
 * @author jklamer
 */
public class ShardedCheckFlagsTask implements Serializable
{
    private final List<Check> checks;
    private final String country;
    private final Shard shard;

    public ShardedCheckFlagsTask(final String country, final Shard shard, final List<Check> checks)
    {
        this.country = country;
        this.shard = shard;
        this.checks = checks;
    }

    public List<Check> getChecks()
    {
        return this.checks;
    }

    public String getCountry()
    {
        return this.country;
    }

    public EventService getEventService()
    {
        return EventService.get(this.getUniqueTaskIdentifier());
    }

    public Shard getShard()
    {
        return this.shard;
    }

    public String getUniqueTaskIdentifier()
    {
        return this.country + "_" + this.shard.getName();
    }
}
