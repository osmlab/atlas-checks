package org.openstreetmap.atlas.checks.distributed;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.tag.InvalidLanesTagCheck;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;

/**
 * Unit tests for {@link ShardedCheckFlagsTask}.
 *
 * @author bbreithaupt
 */
public class ShardedCheckFlagsTaskTest
{
    private static final String COUNTRY = "CAN";
    private static final Shard SHARD = new SlippyTile(0, 0, 0);
    private static final List<Check> CHECKS = Collections
            .singletonList(new InvalidLanesTagCheck(ConfigurationResolver.emptyConfiguration()));
    private static final ShardedCheckFlagsTask TASK = new ShardedCheckFlagsTask(COUNTRY, SHARD,
            CHECKS);

    @Test
    public void getChecks()
    {
        Assert.assertEquals(CHECKS, TASK.getChecks());
    }

    @Test
    public void getCountry()
    {
        Assert.assertEquals(COUNTRY, TASK.getCountry());
    }

    @Test
    public void getShardGroup()
    {
        Assert.assertEquals(SHARD, TASK.getShard());
    }

    @Test
    public void getUniqueTaskIdentifier()
    {
        Assert.assertEquals(String.format("%s_%s", COUNTRY, SHARD.getName()),
                TASK.getUniqueTaskIdentifier());
    }
}
