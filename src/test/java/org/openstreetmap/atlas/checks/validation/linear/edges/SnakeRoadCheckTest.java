package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link SnakeRoadCheck} unit test
 *
 * @author mgostintsev
 */
public class SnakeRoadCheckTest
{
    @Rule
    public SnakeRoadCheckTestRule setup = new SnakeRoadCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testAtlasWithMultipleSnakeRoads()
    {
        this.verifier.actual(this.setup.getSnakeRoadAtlas(), new SnakeRoadCheck(configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(19, flags.size()));
    }

    @Test
    public void testAtlasWithNoSnakeRoads()
    {
        this.verifier.actual(this.setup.getNoSnakeRoadAtlas(), new SnakeRoadCheck(configuration));
        this.verifier.verifyEmpty();
    }

}
