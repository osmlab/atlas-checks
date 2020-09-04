package org.openstreetmap.atlas.checks.base;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.checks.BaseTestCheck;
import org.openstreetmap.atlas.checks.base.checks.BaseTestRule;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test the ability to filter checks based on tags within features.
 *
 * @author cuthbertm
 */
public class TagTest
{
    /**
     * Test rule that contains the atlas to test against.
     */
    @Rule
    public BaseTestRule setup = new BaseTestRule();

    /**
     * Test combination tags. In the two cases first, filter by any features that contain the tag
     * and value "permitlist=true" but does not contain the value "denylist=false". We specifically
     * used the filter "!true" even though we could have used "false" to test negation in filters.
     * Second, Test the OR functionality, process all features that contain "permitlist=true" or
     * "highway=trunk".
     */
    @Test
    public void testCombinationTags()
    {
        final String config = "{\"BaseTestCheck\":{\"tags.filter\":\"permitlist->true&denylist->!true\"}}";
        this.testConfiguration(config, 1);
        final String config2 = "{\"BaseTestCheck\":{\"tags.filter\":\"permitlist->true|highway->trunk\"}}";
        this.testConfiguration(config2, 5);
    }

    /**
     * Test that when setting the filter explicitly with no value that it would essentially be
     * ignored.
     */
    @Test
    public void testNoRestrictions()
    {
        final String config = "{\"BaseTestCheck\":{\"tags.permitlist.filter\":\"\"}}";
        this.testConfiguration(config, 8);
    }

    /**
     * Test to make sure that we can filter when setting a specific tag and value to filter by. In
     * this case making sure that all features with "highway=trunk" are processed and everything
     * else is ignored. Also testing the filter "test->*" processed all features with the "test" tag
     * regardless of value.
     */
    @Test
    public void testTagFilters()
    {
        final String config = "{\"BaseTestCheck\":{\"tags.filter\":\"highway->trunk\"}}";
        this.testConfiguration(config, 3);
        final String config2 = "{\"BaseTestCheck\":{\"tags.filter\":\"test->*\"}}";
        this.testConfiguration(config2, 1);
    }

    /**
     * Private function used by all test cases to easily test the configuration instead of rewriting
     * the code constantly.
     * 
     * @param config
     *            The {@link Configuration} is string form that is being test.
     * @param numberOfFlags
     *            The number of expected flags to be processed based on the provided configuration
     */
    private void testConfiguration(final String config, final int numberOfFlags)
    {
        final Atlas atlas = this.setup.getAtlas();
        final Configuration configuration = ConfigurationResolver.inlineConfiguration(config);
        final List<CheckFlag> flags = Iterables
                .asList(new BaseTestCheck(configuration).flags(atlas));
        // will return the number of flags for the number of objects because no restrictions
        Assert.assertEquals(numberOfFlags, flags.size());
    }
}
