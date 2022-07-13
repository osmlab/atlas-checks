package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link LoneBuildingNodeCheck}
 *
 * @author vladlemberg
 */
public class LoneBuildingNodeCheckTest
{
    @Rule
    public LoneBuildingNodeCheckTestRule setup = new LoneBuildingNodeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * Test 1: Building Node enclosed in building
     */
    @Test
    public void enclosedBuildingNode()
    {
        this.verifier.actual(this.setup.enclosedBuildingNodeAtlas(),
                new LoneBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    /**
     * Test 2: Building Node is not geometrically enclosed in building
     */
    @Test
    public void loneBuildingNode()
    {
        this.verifier.actual(this.setup.loneBuildingNodeAtlas(),
                new LoneBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
