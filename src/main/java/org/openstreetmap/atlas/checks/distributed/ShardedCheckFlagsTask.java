package org.openstreetmap.atlas.checks.distributed;

import java.util.List;

import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.utility.ShardGroup;
import org.openstreetmap.atlas.event.EventService;

public class ShardedCheckFlagsTask
{
    private final String country;
    private final ShardGroup shardGroup;
    private final List<Check> checks;

    public ShardedCheckFlagsTask(final String country, final ShardGroup shardGroup,
            final List<Check> checks)
    {
        this.country = country;
        this.shardGroup = shardGroup;
        this.checks = checks;
    }

    public String getCountry()
    {
        return this.country;
    }

    public String getUniqueTaskIdentifier()
    {
        return this.country + "_" + this.shardGroup.getName();
    }

    public EventService getEventService()
    {
        return EventService.get(this.getUniqueTaskIdentifier());
    }

    public ShardGroup getShardGroup()
    {
        return this.shardGroup;
    }

    public List<Check> getChecks()
    {
        return this.checks;
    }
}
