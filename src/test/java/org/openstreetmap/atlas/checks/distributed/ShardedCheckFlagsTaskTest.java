package org.openstreetmap.atlas.checks.distributed;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.utility.ShardGroup;
import org.openstreetmap.atlas.checks.validation.tag.InvalidLanesTagCheck;

/**
 * Unit tests for {@link ShardedCheckFlagsTask}.
 *
 * @author bbreithaupt
 */
public class ShardedCheckFlagsTaskTest
{
    private static final String COUNTRY = "CAN";
    private static final ShardGroup GROUP = new ShardGroup(Collections.emptySet(), "empty_group");
    private static final List<Check> CHECKS = Collections
            .singletonList(new InvalidLanesTagCheck(ConfigurationResolver.emptyConfiguration()));
    private static final ShardedCheckFlagsTask TASK = new ShardedCheckFlagsTask(COUNTRY, GROUP,
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
        Assert.assertEquals(GROUP, TASK.getShardGroup());
    }

    @Test
    public void getUniqueTaskIdentifier()
    {
        Assert.assertEquals(String.format("%s_%s", COUNTRY, GROUP.getName()),
                TASK.getUniqueTaskIdentifier());
    }
}
