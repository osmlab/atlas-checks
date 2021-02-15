package org.openstreetmap.atlas.checks.utility;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;

/**
 * Test for {@link UniqueCheckFlagContainer}
 *
 * @author jklamer
 * @author bbreithaupt
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
    private final CheckFlag sameObjectFlag2 = new CheckFlag("example-flag-4");

    @Test
    public void testCheckFlagEvent()
    {
        final CheckFlagEvent event = new CheckFlagEvent(source1, this.flag1.makeComplete());
        final UniqueCheckFlagContainer container = new UniqueCheckFlagContainer(event);

        Assert.assertEquals(event.toString(), container.getEvent().toString());
    }

    @Test
    public void testGetters()
    {
        this.flag1.addObject(this.setup.atlas().node(1000000L));
        final UniqueCheckFlagContainer container = new UniqueCheckFlagContainer(source1,
                this.flag1.getUniqueIdentifiers(), this.flag1);

        Assert.assertEquals(source1, container.getCheckName());
        Assert.assertEquals(Collections.singleton("Node1000000"), container.getUniqueIdentifiers());
        Assert.assertEquals(this.flag1, container.getCheckFlag());
    }

    @Test
    public void testUniqueness()
    {
        // Add Object to flags
        this.flag2.addObject(this.setup.atlas().node(1000000L));
        this.flag3.addObject(this.setup.atlas().edge(1000000L));
        this.sameObjectFlag2.addObject(this.setup.atlas().node(1000000L));

        final UniqueCheckFlagContainer container1 = new UniqueCheckFlagContainer(source2,
                this.flag2.getUniqueIdentifiers(), this.flag2);
        final UniqueCheckFlagContainer container2 = new UniqueCheckFlagContainer(source2,
                this.flag3.getUniqueIdentifiers(), this.flag3);
        final UniqueCheckFlagContainer container3 = new UniqueCheckFlagContainer(source2,
                this.sameObjectFlag2.getUniqueIdentifiers(), this.sameObjectFlag2);

        Assert.assertEquals(container1, container3);
        Assert.assertNotEquals(container1, container2);
    }
}
