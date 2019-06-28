package org.openstreetmap.atlas.checks.base;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.PierTestCheck;
import org.openstreetmap.atlas.checks.base.checks.PierTestRule;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Basic test to make sure that we skip the pier edges by default, and ability to test them if
 * required
 *
 * @author cuthbertm
 * @author brian_l_davis
 */
public class PierTest
{
    /**
     * The Test rule that contains the atlas to test against
     */
    @Rule
    public PierTestRule setup = new PierTestRule();

    /**
     * Test the pier override value that will allow piers to be processed.
     */
    @Test
    public void testPierOverrideSkip()
    {
        final String configSource = "{\"PierTestCheck.accept.piers\": true}";
        final PierTestCheck check = new PierTestCheck(
                ConfigurationResolver.inlineConfiguration(configSource));
        final Set<CheckFlag> flags = Iterables.stream(check.flags(this.setup.getAtlas()))
                .collectToSet();
        Assert.assertEquals(3, flags.size());
    }

    /**
     * Test default behavior to make sure the pier override value when explicitly set to false
     * maintains the behavior of not allowing piers to be processed
     */
    @Test
    public void testPierSkip()
    {
        final String configSource = "{\"PierTestCheck.accept.piers\": false}";
        final PierTestCheck check = new PierTestCheck(
                ConfigurationResolver.inlineConfiguration(configSource));
        final Set<CheckFlag> flags = Iterables.stream(check.flags(this.setup.getAtlas()))
                .collectToSet();
        Assert.assertEquals(2, flags.size());
        flags.forEach(flag -> Assert.assertNotEquals(100, flag.getIdentifier()));
    }
}
