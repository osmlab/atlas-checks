package org.openstreetmap.atlas.checks.utility;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlag;

/**
 * Test for {@link UniqueCheckFlagContainer}
 *
 * @author jklamer
 */
public class UniqueCheckFlagContainerTest
{
    private static final String source1 = "source1";
    private static final String source2 = "source2";
    @Rule
    public UniqueCheckFlagContainerTestRule setup = new UniqueCheckFlagContainerTestRule();
    private final CheckFlag flag1 = new CheckFlag("example-flag-1");
    private final CheckFlag flag2 = new CheckFlag("example-flag-2");
    private final CheckFlag flag3 = new CheckFlag("example-flag-3");
    private final CheckFlag sameID_flag1 = new CheckFlag("example-flag-1");
    private final CheckFlag sameObject_flag2 = new CheckFlag("example-flag-4");

    @Test
    public void testStreaming()
    {
        final UniqueCheckFlagContainer container = new UniqueCheckFlagContainer();
        container.add(source1, flag1);
        container.add(source1, flag2);
        container.add(source1, flag3);
        container.add(source2, flag1);
        container.add(source2, flag2);
        container.add(source2, flag3);

        final Set<CheckFlag> source1Flags = new HashSet<>(Arrays.asList(flag1, flag2, flag3));
        final Set<CheckFlag> source2Flags = new HashSet<>(Arrays.asList(flag1, flag2, flag3));

        container.reconstructEvents().forEach(flagEvent ->
        {
            if (flagEvent.getCheckName().equals(source1))
            {
                source1Flags.remove(flagEvent.getCheckFlag());
            }
            else if (flagEvent.getCheckName().equals(source2))
            {
                source2Flags.remove(flagEvent.getCheckFlag());
            }
        });

        Assert.assertTrue(source1Flags.isEmpty());
        Assert.assertTrue(source2Flags.isEmpty());
    }

    @Test
    public void testUniqueness()
    {
        // Add Object to flags
        this.flag2.addObject(this.setup.atlas().node(1000000L));
        this.flag3.addObject(this.setup.atlas().edge(1000000L));
        this.sameObject_flag2.addObject(this.setup.atlas().node(1000000L));

        final UniqueCheckFlagContainer container = new UniqueCheckFlagContainer();
        // shouldn't deduplicate
        container.add(source1, flag1);
        container.add(source1, flag2);
        Assert.assertEquals(2L, container.stream().count());

        // should deduplicate
        container.add(source1, sameID_flag1);
        Assert.assertEquals(2L, container.stream().count());

        // shouldn't deduplicate
        container.add(source2, flag1);
        Assert.assertEquals(3L, container.stream().count());

        // should deduplicate
        container.add(source2, flag1);
        container.add(source2, flag1);
        container.add(source2, flag1);
        container.add(source2, flag1);
        Assert.assertEquals(3L, container.stream().count());

        // Shouldn't deduplicate
        container.add(source1, this.flag3);
        Assert.assertEquals(4L, container.stream().count());

        // Should deduplicate
        container.add(source1, this.sameObject_flag2);
        Assert.assertEquals(4L, container.stream().count());
    }
}
